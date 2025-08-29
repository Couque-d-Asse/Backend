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
from photo_guide_service import extract_photo_guide
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
    """민원 초안 생성 요청 모델 (요약+제목+법률 근거)"""
    summary: str = "이문로 일대는 올해 서울에서 새 아파트가 가장 많이 늘어나는 지역이다. 지난 1월 입주한 ‘래미안라그란데’(이문1구역·3069가구)를 시작으로 7월 ‘휘경자이디센시아’(휘경3구역·1806가구), 11월 ‘이문아이파크자이’(이문3구역·4321가구)가 줄줄이 준공할 예정이다. 이주를 진행 중인 이문4구역(3628가구 예정)까지 포함하면 이문·휘경뉴타운 일대에 4~5년 새 1만3000가구가 들어선다. 문제는 이를 받아낼 도로 용량과 우회도로가 턱없이 부족하다는 것이다. 이문·휘경뉴타운을 관통하는 이문로는 편도 2차로(왕복 4차로)에 불과하다. 회기역까지 이어지던 편도 3차로(왕복 6차로)가 좁아져 상습적으로 병목현상이 발생한다. 이마저도 곳곳에 비보호 좌회전 구간이 있어서 직진 차로로 이용할 수 있는 구간이 제한적이다. 우회도로도 마땅치 않다. 그나마 한국외국어대 앞에서 동부간선도로 방향으로 우회할 수 있는 휘경로도 사정은 마찬가지다. 신호가 짧은 데다 도로가 더욱 좁아져서다. 이문동 B공인중개사무소 관계자는 “차 네다섯 대만 지나가도 신호가 끊겨 정체가 해소되지 않는다”며 “이문아이파크자이 지하를 지나는 도로를 공사 중이지만 이문4구역 사업이 끝나야 완전 개통하기 때문에 당분간 교통 문제 해결은 요원하다”고 지적했다."
    title: str = "이문로 교통 병목 및 도로 확장 민원"
    legal_basis: List[Dict] = [
        {
            "law_name": "도로교통법",
            "article": "제5조",
            "enforced_on": "2022-04-05",
            "reason": "도로의 확장 및 교통환경 개선을 위한 도로구획과 확장 관련 규정이 명시되어 있으며, 민원 내용과 직결됨.",
            "url": "https://www.law.go.kr/LSW/lsSc.do?menuId=0&query=도로교통법"
        }
    ]

class CivicAssistResponse(BaseModel):
    """민원 초안 생성 응답 모델"""
    channel: str
    title: str
    body: str

class Issue(BaseModel):
    """민원 요약 모델 (추천용)"""
    summary: str = "이문로 일대 교통 병목 및 도로 확장 필요"

class Channel(BaseModel):
    """추천 채널 모델"""
    id: str = "channel1"
    title: str = "서울시 교통행정과"

class RecommendRequest(BaseModel):
    """추천 요청 모델"""
    issue: Issue = Issue()
    channels: List[Channel] = [Channel()]


 # =====================
 # 초안 생성 API (SSE)
 # =====================
@app.post("/process/stream")
def process_stream(request: CivicAssistRequest):
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
def photo_guide_api(request: CivicAssistRequest):
    """
    민원 초안에서 사진 첨부 안내 항목만 리스트로 반환하는 API
    """
    draft_text = generate_draft(request.summary, request.title, request.legal_basis) or ""
    photo_guide = extract_photo_guide(draft_text)
    return {"photo_guide": photo_guide}

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
