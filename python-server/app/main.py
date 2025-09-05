from fastapi import FastAPI, Request, APIRouter, Body
from fastapi.responses import StreamingResponse, JSONResponse
from typing import List, Dict
from pydantic import BaseModel
import time
import traceback
from dotenv import load_dotenv
import os
# python-server 루트의 .env 파일을 명시적으로 로드
load_dotenv(os.path.join(os.path.dirname(os.path.dirname(__file__)), '.env'))
 
from services.draft_generator import generate_draft
from services.photo_guide_service import extract_photo_guide
from services.legal_basis import build_legal_basis, build_legal_info_summary, extract_keywords
from services.channel_recommendation_service import recommend_channel


app = FastAPI()

# CORS 미들웨어 추가 (프론트에서 API 호출 허용)
from fastapi.middleware.cors import CORSMiddleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 개발 시 전체 허용, 운영 시 도메인 제한 권장
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)
router = APIRouter()

# FastAPI 글로벌 예외 처리
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

# 데이터 모델 import (models 폴더)
from models.civicdraft import CivicDraftRequest, CivicDraftResponse
from models.channel import Issue, Channel, RecommendRequest

# 민원 초안 생성 API (SSE)
@app.post("/process/stream")
def process_stream(request: CivicDraftRequest):
    """
    민원 초안 생성 (SSE)
    - 요약+제목+법률 근거 기반 초안 결과를 실시간 chunk 단위로 반환
    """
    gpt_result = generate_draft(request.summary, request.title, request.legal_basis) or ""
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

# 사진 첨부 안내 추출 API
@app.post("/api/photo-guide")
def photo_guide_api(request: CivicDraftRequest):
    """
    민원 초안에서 사진 첨부 안내 항목만 리스트로 반환하는 API
    """
    draft_text = generate_draft(request.summary, request.title, request.legal_basis) or ""
    photo_guide = extract_photo_guide(draft_text)
    return {"photo_guide": photo_guide}

# 채널 추천 API
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

# 법률 근거 후보/요약 API
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
