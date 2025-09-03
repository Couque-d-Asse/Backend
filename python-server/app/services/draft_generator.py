# draft_generator.py
# 민원 초안 생성 기능을 담당하는 서비스 모듈

import os
from .gpt_service import call_gpt

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
- 모든 주장은 구체적 수치, 현장명, 법적 근거와 함께 작성(예: '이문로 00-00 교차로', '왕복 4차로', '도로교통법 제5조')
- 개선 요청은 실제 행정기관에서 처리 가능한 수준으로 현실적·구체적으로 작성(예: '신호주기 30초 → 45초로 조정 요청')
- 문제점(교통체증, 도로 협소, 신호 짧음, 비보호 좌회전 등)을 명확히 진술
- 법률 근거는 각 주장 뒤에 각주처럼 표기(예: 도로교통법 제5조)
- 민원인의 입장에서 실제로 제출 가능한 문장으로 작성
- 행정기관에 요청할 사항(예: 도로 확장 계획, 신호주기 조정, 우회도로 개설 등)을 명확히 제시
- 사진 첨부 안내는 담당자가 현장 상황을 빠르게 파악할 수 있도록, 위치·시점·필요 이유를 명확히 제시(예: '병목 구간 현장 사진(정체 상황 확인용)')
- 결론은 기대 효과를 한 문장으로 요약(예: '교통 흐름 개선 및 주민 안전 확보 기대')
- 전체 초안은 번호, 제목, 리스트, 각주 등으로 체계적으로 구분
- 중복·모호·감정적·불필요한 표현 금지, 간결하고 논리적으로 작성

특히 아래와 같이 사진 첨부 안내 항목을 반드시 명확하게 구분된 리스트(예시처럼)로 작성해줘. 불필요한 설명 없이 항목만 나열해.
예시:
2. 사진 첨부 안내
- 병목 구간(이문로 00-00 교차로)의 정체 현장 사진
- 신호등 위치와 신호 시간 현황 사진
- 도로 폭 협소 구간 사진
- 예상 우회도로 계획 구역 사진(가능하면 지도 포함)

민원 요약: {summary}
민원 제목: {title}
{basis_text}
위 정보를 바탕으로 실제 민원 초안을 1) 본문(구체적 진술+개선 요청+근거), 2) 사진 첨부 안내(반드시 리스트로), 3) 결론(기대 효과)로 나눠서 작성해줘.
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
