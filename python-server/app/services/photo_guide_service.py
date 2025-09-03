# photo_guide_service.py
# 민원 초안에서 사진 첨부 안내만 추출하는 서비스 모듈
import re

def extract_photo_guide(draft_text):
    """
    민원 초안 텍스트에서 사진 첨부 안내 부분만 추출하여 리스트로 반환
    """
    # '2. 사진 첨부 안내' ~ '3. 결론' 또는 문서 끝까지 텍스트 추출
    match = re.search(r"2[.] 사진 첨부 안내\s*([\s\S]*?)(?:3[.] 결론|$)", draft_text)
    if not match:
        return []
    guide_text = match.group(1)
    # 리스트 항목만 추출 (예: - 항목, • 항목 등)
    photo_items = []
    for line in guide_text.splitlines():
        line = line.strip()
        if line.startswith('-') or line.startswith('•') or line.startswith('·'):
            photo_items.append(line.lstrip('-•· ').strip())
    return photo_items
