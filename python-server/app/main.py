from fastapi import FastAPI, Request
from pydantic import BaseModel
from typing import Any, List, Dict

app = FastAPI()

class CivicAssistRequest(BaseModel):
    userText: str
    photos: bool
    videos: bool
    locationText: str
    legalCandidatesJson: str

class CivicAssistResponse(BaseModel):
    channel: str
    title: str
    body: str
    bulletedRequests: List[str]
    channelFields: Dict[str, Any]
    legalBasis: List[Dict[str, Any]]
    confidence: float
    missingFields: List[str]
    safetyFlags: Dict[str, Any]

@app.post("/process")
def process(request: CivicAssistRequest):
    # TODO: LangChain 연동 및 실제 처리
    return CivicAssistResponse(
        channel="saeol",
        title="테스트 제목",
        body="테스트 본문",
        bulletedRequests=["요청1", "요청2"],
        channelFields={"public_visibility": "private", "sms_notify": True, "category": "교통/도로", "address_text": request.locationText},
        legalBasis=[],
        confidence=0.9,
        missingFields=[],
        safetyFlags={"contains_pii": False, "defamation_risk": "low"}
    )
