from fastapi import FastAPI, Request, APIRouter, Body
from fastapi.responses import StreamingResponse, JSONResponse
from pydantic import BaseModel
from typing import Any, List, Dict
import time
import traceback
from dotenv import load_dotenv
import os
# python-server 루트의 .env 파일을 명시적으로 로드
load_dotenv(os.path.join(os.path.dirname(os.path.dirname(__file__)), '.env'))
from gpt_service import call_gpt
from draft_generator import generate_draft
from legal_basis import build_legal_basis, build_legal_info_summary, extract_keywords
from channel_recommendation_service import recommend_channel

app = FastAPI()
router = APIRouter()

# =====================
# Exception Handling
# =====================
@app.exception_handler(Exception)
async def global_exception_handler(request: Request, exc: Exception):
    print("=== FastAPI 글로벌 예외 발생 ===")
    print("Request:", request)
    print("Exception:", exc)
    traceback.print_exc()
    return JSONResponse(
        status_code=500,
        content={"detail": str(exc)}
    )

# =====================
# Data Models
# =====================
class CivicAssistRequest(BaseModel):
    """민원 초안 생성 요청 모델 (요약+제목)"""
    summary: str           # 민원 요약
    title: str             # 민원 제목

class CivicAssistResponse(BaseModel):
    """민원 초안 생성 응답 모델"""
    channel: str
    title: str
    body: str

class Issue(BaseModel):
    """민원 요약 모델 (추천용)"""
    summary: str

class Channel(BaseModel):
    """추천 채널 모델"""
    id: str
    title: str

class RecommendRequest(BaseModel):
    """추천 요청 모델"""
    issue: Issue
    channels: List[Channel]


# =====================
# 초안 생성 API (SSE)
# =====================
@app.post("/process/stream")
def process_stream(request: CivicAssistRequest):
    """
    민원 초안 생성 (SSE)
    - 요약+제목 기반 초안 결과를 실시간 chunk 단위로 반환
    """
    gpt_result = generate_draft(request.summary, request.title) or ""
    import re
    def split_chunks(text):
        for s in re.split(r'(?<=[.!?]) +|\n', text):
            if s.strip():
                yield s.strip()
    def event_stream():
        for chunk in split_chunks(gpt_result):
            yield f"data: {chunk}\n\n"
            time.sleep(0.2)
        yield "data: [END]\n\n"
    return StreamingResponse(event_stream(), media_type="text/event-stream")

# =====================
# 채널 추천 API
# =====================
@app.post("/api/recommend")
def recommend_api(request: RecommendRequest):
    """
    채널 추천 API
    - 민원 요약과 채널 목록을 받아 AI가 적합한 채널 후보 추천
    """
    channels = [{"id": ch.id, "title": ch.title} for ch in request.channels]
    ai_result = recommend_channel(request.issue.summary, channels)
    import json
    try:
        result = json.loads(ai_result or "")
    except Exception as e:
        print("AI 응답 파싱 오류:", e)
        result = {"options": [], "recommendedChannel": None}
    return result

# =====================
# 법률 근거 API
# =====================
@router.post("/legal-basis/candidates")
def get_legal_basis_candidates(
    summary: str = Body(..., embed=True),
    title: str = Body(..., embed=True),
    max_count: int = Body(7, embed=True)
):
    """
    민원 요약과 제목을 받아 GPT 기반 관련 법률 후보 리스트와 근거 요약을 반환하는 API
    max_count: 반환할 최대 법률 근거 개수 (기본 7개)
    """
    # 1. 키워드 추출 (참고용)
    keywords = extract_keywords(summary, title)
    # 2. GPT 기반 법률 후보 및 근거 생성
    candidates = build_legal_basis(summary, title, max_count=max_count)
    # 3. 요약 텍스트 생성
    legal_info = build_legal_info_summary(candidates)
    return {
        "keywords": keywords,
        "candidates": candidates,
        "basis": candidates,
        "summary": legal_info
    }

app.include_router(router, prefix="/api")
