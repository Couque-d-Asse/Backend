from pydantic import BaseModel
from typing import List, Dict

class CivicDraftRequest(BaseModel):
    """
    민원 초안 저장 요청 모델 (자바 CivicDraftRequest DTO와 동일)
    - summary: 민원 요약
    - title: 민원 제목
    - legal_basis: 선택된 법률 근거 리스트
    """
    summary: str
    title: str
    legal_basis: List[Dict] = []

class CivicDraftResponse(BaseModel):
    """
    민원 초안 저장 응답 모델 (자바 CivicDraftResponse DTO와 동일)
    - channel: 추천 채널
    - title: 민원 제목
    - body: 민원 초안 본문
    """
    channel: str
    title: str
    body: str
