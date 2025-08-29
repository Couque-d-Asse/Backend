# legal_basis.py
# 법률정보 생성 및 요약 파이썬 모듈
from typing import List, Dict
import requests
from gpt_service import call_gpt

def build_legal_info_summary(legal_basis: List[Dict]) -> str:
    if not legal_basis:
        return "관련 법률정보 없음"
    lines = []
    for basis in legal_basis:
        lines.append(f"{basis.get('law_name')} {basis.get('article')} - {basis.get('reason')}")
    return "\n".join(lines)

def extract_keywords(summary, title):
    """
    GPT를 활용해 민원 요약/제목에서 법률 관련 키워드를 추출
    """
    prompt = f"""
아래 민원 요약과 제목을 참고하여, 관련 법률/행정/교통/도시/안전 등 민원 이슈와 직접적으로 연관된 핵심 키워드를 3~7개 한글로 뽑아줘.
- 키워드는 반드시 실제 법령 검색에 활용될 수 있는 단어로만 선정
- 불필요한 일반 단어(예: '민원', '요청', '문제', '해결' 등)는 제외
- 각 키워드는 명확하고 구체적으로 작성
- 결과는 쉼표로 구분된 키워드 문자열만 반환(예시: 교통, 도로, 신호, 주차, 안전, 도시계획)

민원 요약: {summary}
민원 제목: {title}
"""
    messages = [
        {"role": "system", "content": "너는 법률 키워드 추출 전문가야. 반드시 핵심 키워드만 반환하고, 불필요한 단어는 제외해."},
        {"role": "user", "content": prompt}
    ]
    result = call_gpt(messages, model="gpt-4.1-nano", max_completion_tokens=100)
    if not result:
        result = ""
    keywords = [kw.strip() for kw in result.split(",") if kw.strip()]
    print(f"[extract_keywords] GPT result: {result}")
    print(f"[extract_keywords] Parsed keywords: {keywords}")
    return keywords or [summary, title]


def fetch_law_candidates(keywords, oc="jsshin8128"):
    import urllib.parse
    url = "https://www.law.go.kr/DRF/lawSearch.do"
    candidates = []
    for kw in keywords:
        params = {
            "OC": oc,
            "target": "law",
            "type": "json",
            "query": kw,
            "page": 1
        }
        encoded_params = urllib.parse.urlencode(params, doseq=True)
        try:
            response = requests.get(f"{url}?{encoded_params}")
            response.raise_for_status()
            data = response.json()
            law_items = data.get("lawSearch", {}).get("law", [])
            for item in law_items:
                candidates.append({
                    "law_id": item.get("법령ID"),
                    "mst": item.get("법령일련번호"),
                    "title": item.get("법령명한글"),
                    "type": item.get("법령구분명"),
                    "enforced_on": item.get("시행일자"),
                    "promulgated_on": item.get("공포일자"),
                    "ministry": item.get("소관부처명"),
                    "detail_url": f"https://www.law.go.kr/DRF/lawService.do?OC={oc}&target=law&MST={item.get('법령일련번호')}&type=HTML"
                })
        except Exception as e:
            print(f"법령 검색 오류({kw}):", e)
    return candidates

def build_legal_basis(summary: str, title: str, max_count: int = 5) -> List[Dict]:
    """
    GPT 프롬프트에서 후처리(중복 제거, 중요도/최신순 정렬, max_count 제한 등)까지 직접 수행하도록 강화
    """
    prompt = f"""
너는 대한민국 법령 전문가이자 민원 자동화 시스템의 핵심 엔진이다.
아래 민원 요약과 제목을 보고, 실제 존재하는 법령명(예: 도로교통법, 교통안전법, 자동차관리법, 건축법, 도시계획법, 환경정책기본법, 개인정보보호법, 산업안전보건법 등), 조문번호, 시행일, 근거 설명을 반드시 정확하게 표기해라.

- 반드시 국가법령정보센터에 등재된 실제 법령명만 사용한다.
- 허위 인용, 존재하지 않는 법령명/조문번호/시행일은 절대 금지한다.
- 각 법령별로 조문번호(예: 제1조, 제5조 등), 시행일, 근거 설명을 포함한다.
- 근거 설명은 민원 이슈와의 직접적 관련성을 구체적으로 서술한다.
- 결과는 아래 예시처럼 JSON 배열로만 반환한다(불필요한 텍스트 금지).
- 법령명, 조문번호, 시행일, 근거 설명, 공식 URL(https://www.law.go.kr/LSW/lsSc.do?menuId=1&query=법령명)을 포함한다.
- 반드시 {max_count}개 이하로 추천한다.
- 중복 법령명/조문번호는 제거하고, 최신 시행일 기준으로 정렬한다.
- 민원 이슈와 가장 직접적으로 관련된 법령부터 중요도 순으로 정렬한다.

민원 요약: {summary}
민원 제목: {title}

예시:
[
  {{
    "law_name": "도로교통법",
    "article": "제5조",
    "enforced_on": "2023-07-01",
    "reason": "신호등 설치 및 운영 기준이 명시되어 있어 교통 신호 개선 민원과 직접적으로 관련됨.",
    "url": "https://www.law.go.kr/LSW/lsSc.do?menuId=1&query=도로교통법"
  }},
  {{
    "law_name": "교통안전법",
    "article": "제10조",
    "enforced_on": "2022-01-01",
    "reason": "교통안전시설의 설치 및 관리에 관한 규정이 민원 내용과 연관됨.",
    "url": "https://www.law.go.kr/LSW/lsSc.do?menuId=1&query=교통안전법"
  }}
]
"""
    messages = [
        {"role": "system", "content": "너는 대한민국 법령 전문가이자 민원 자동화 시스템의 핵심 엔진이다."},
        {"role": "user", "content": prompt}
    ]
    result = call_gpt(messages, model="gpt-4.1-nano", max_completion_tokens=800)
    import json
    try:
        return json.loads(result or "")
    except Exception as e:
        print("GPT 법령 JSON 파싱 오류:", e)
        print("GPT 원본 응답:", result)
        return []
