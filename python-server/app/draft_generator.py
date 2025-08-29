# draft_generator.py
# 민원 초안 생성 기능을 담당하는 서비스 모듈

import os
from gpt_service import call_gpt


def generate_draft(summary, title, model="gpt-4.1-nano"):
    """
    OpenAI GPT 채팅 API 기반 민원 초안 생성 함수.
    summary: 민원 요약
    title: 민원 제목
    """
    steps = [
        ("OPENAI API 키 확인", 10),
        ("입력값 파싱", 20),
        ("프롬프트 생성", 40),
        ("GPT 호출", 80),
        ("완료", 100)
    ]
    def print_progress(step, percent):
        print(f"[{percent:3}%] {step}...")

    print_progress(*steps[0])
    openai_api_key = os.getenv("OPENAI_API_KEY")
    if not openai_api_key:
        raise ValueError("OPENAI_API_KEY 환경변수가 설정되어 있지 않습니다.")

    print_progress(*steps[1])
    if not summary or not title:
        print("summary 또는 title이 비어있음")
        return ""

    print_progress(*steps[2])
    prompt = f"""
민원 요약: {summary}
민원 제목: {title}
위 정보를 바탕으로 민원 본문을 작성해줘. 어떤 사진 첨부가 필요한지 안내해줘. 관련 법률정보도 함께 추천해줘.
"""

    messages = [
        {"role": "system", "content": "너는 민원 초안 작성 전문가야."},
        {"role": "user", "content": prompt}
    ]

    print_progress(*steps[3])
    result = call_gpt(messages, model=model, max_completion_tokens=1200)
    print("GPT 실행 결과:", result)
    print_progress(*steps[4])
    return result
