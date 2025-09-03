from pydantic import BaseModel
from typing import List

class Issue(BaseModel):
    """민원 요약 모델 (채널 추천용)"""
    summary: str

class Channel(BaseModel):
    """추천 채널 모델"""
    id: str
    title: str

class RecommendRequest(BaseModel):
    """추천 요청 모델"""
    issue: Issue
    channels: List[Channel]
