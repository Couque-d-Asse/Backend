# draft_generator.py
# 민원 초안 생성 기능을 담당하는 서비스 모듈

import os
from gpt_service import call_gpt


def generate_draft(summary, title, legal_basis=None, model="gpt-4.1-nano"):
    """
    OpenAI GPT 채팅 API 기반 민원 초안 생성 함수.
    summary: 민원 요약
    title: 민원 제목
    legal_basis: 선택된 법률 근거 리스트 (옵션)
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
    basis_text = ""
    if legal_basis:
        basis_lines = [f"- {b.get('law_name')} {b.get('article')}: {b.get('reason')}" for b in legal_basis]
        basis_text = "[선택된 법률 근거]\n" + "\n".join(basis_lines) + "\n"
    prompt = f"""
너는 대한민국 민원 초안 작성 전문가이자 교통·도시·법률 컨설턴트다.
아래 민원 요약과 제목, 그리고 법률 근거를 참고하여 실제 민원인이 제출할 수 있는 수준의 구체적이고 실질적인 민원 초안을 작성해줘.

[민원 작성 가이드]
- 현장 상황(도로 용량, 병목, 신호, 우회도로 등)을 구체적으로 반영
- 문제점(교통체증, 도로 협소, 신호 짧음, 비보호 좌회전 등)을 명확히 진술
- 개선 요청(도로 확장, 신호주기 조정, 우회도로 확보 등)을 구체적으로 제안
- 법률 근거는 각 주장 뒤에 각주처럼 표기(예: 도로교통법 제5조)
- 민원인의 입장에서 실제로 제출 가능한 문장으로 작성
- 필요한 사진 첨부 안내(예: 병목 구간, 신호등 위치 등)
- 행정기관에 요청할 사항(예: 도로 확장 계획, 신호주기 조정, 우회도로 개설 등)을 명확히 제시
- 결론에는 민원인의 기대 효과(교통 개선, 안전 확보 등)를 간단히 요약

민원 요약: {summary}
민원 제목: {title}
{basis_text}
위 정보를 바탕으로 실제 민원 초안을 1) 본문(구체적 진술+개선 요청+근거), 2) 사진 첨부 안내, 3) 결론(기대 효과)로 나눠서 작성해줘.
"""

    messages = [
        {"role": "system", "content": "너는 대한민국 민원 초안 작성 전문가이자 교통·도시·법률 컨설턴트다."},
        {"role": "user", "content": prompt}
    ]

    print_progress(*steps[3])
    result = call_gpt(messages, model=model, max_completion_tokens=1200)
    print("GPT 실행 결과:", result)
    print_progress(*steps[4])
    return result
