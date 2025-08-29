# channel_recommendation_service.py
# 민원 채널 추천 기능을 담당하는 서비스 모듈

from .gpt_service import call_gpt
import os


def recommend_channel(issue_summary, channels, model="gpt-5-nano"):
    """
    OpenAI GPT 채팅 API 기반 민원 채널 추천 함수.
    issue_summary: 민원 요약 텍스트
    channels: [{"id": ..., "title": ...}, ...] 형태의 채널 목록
    """
    steps = [
        ("OPENAI API 키 확인", 10),
        ("채널 목록 파싱", 30),
        ("프롬프트 생성", 50),
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
    channel_list_str = ', '.join([f'{ch["id"]}: {ch["title"]}' for ch in channels])

    print_progress(*steps[2])
    prompt = f"""
민원 내용: {issue_summary}
채널 목록:
{channel_list_str}

아래 [민원 채널 후보 선정 기준]을 반드시 준수하여, 민원 요약에 대해 0~3개 채널을 후보로 선정하고 각 후보별로 추천 이유를 설명해줘. 반드시 명시적 표현만 사용하고, 추정은 금지한다.

[민원 채널 후보 선정 기준]

공통 원칙:
- 문장에 명시된 의도만 사용한다(추정 금지)
- 하나의 민원 요약에 0~3개 채널 모두 가능하다(배타적 선택 아님)
- “불편”·“정체” 같은 상태 묘사만 있고 요청/행위가 없으면 후보 제외
- 지역 키워드는 보조 정보일 뿐 채널 결정의 직접 근거가 아니다

각 채널별 포함/배제 트리거:

1) safety_report (안전신문고) — 즉시 조치형 “현장 위험/불법/고장”
포함 트리거: 불법주정차/이중주차/단속, 고장/파손, 위험 요소, 즉시성/증거
배제 트리거: 절차/문서만 요청, 정책·운영 개선 제안만 있고 포함 트리거 없음

2) mayor_board (구청장에게 바란다) — 운영/구조·정책 개선 제안
포함 트리거: 신호 운영, 좌회전 체계, 차로/교차로 구조, 노면/표지 개선, 체계·정책
배제 트리거: 즉시 단속/고장 조치만 요청, 정보공개/허가 등 절차·문서만 요청

3) saeol (새올전자민원창구) — 서식/절차/문서 요청
포함 트리거: 정보공개, 허가·인가·점용, 계획·협의 문서, 공문성/서식성 표현
배제 트리거: 즉시 단속/현장 조치만 요청, 운영/구조 개선만 제안

다중 후보 처리 규칙:
- 서로 다른 성격이 함께 있으면 해당 채널을 모두 후보에 포함
- 동일 문장 내 배제 트리거가 붙은 채널은 포함하지 않음

모호/불충분 판단:
- “정체가 심하다/불편하다” 만 있고 요청·행위(단속/개선/문서)가 없으면 어떤 채널도 추가하지 않음
- 장소·시간·상황이 전혀 없더라도, 위 트리거에 명시적 표현이 있으면 해당 채널 포함

지역 키워드(이문로/휘경로/회기역 등)는 보조 설명/템플릿 개인화에만 사용, 채널 후보 결정은 트리거로만 판단

표현 변형(동의어/패턴) 인식 가이드:
- 불법주정차: “불법 주·정차”, “주차금지 위반”, “코너 주차”, “버스정류소 정차”
- 단속 요청: “단속해주세요/단속 바람/집중 단속”
- 고장/파손: “먹통/깜빡임/꺼짐/부서짐/훼손”
- 신호 운영: “현시/사이클/그린타임/연동/보행신호 길게/짧게”
- 좌회전: “좌회전 분리/화살표 신설/비보호 해제”
- 절차/문서: “공개 청구/자료 요청/허가 문의/점용 신청/협의서 열람”

결과는 반드시 JSON 형식으로 반환해줘. 예시:
{
  "options": [
    { "id": "mayor_board", "title": "구청장에게 바란다", "reason": "신호주기 조정 요청이 명시적으로 포함되어 정책 개선 제안에 해당함" }
  ],
  "recommendedChannel": "mayor_board"
}
"""

    messages = [
        {"role": "system", "content": "너는 민원 채널 추천 전문가야."},
        {"role": "user", "content": prompt}
    ]

    print_progress(*steps[3])
    result = call_gpt(messages, model=model, temperature=0.2, max_tokens=1200)
    print("채널 추천 결과:", result)
    print_progress(*steps[4])
    return result
