# python-server (민원 초안/추천/법률정보 AI 서버)

## 폴더 구조
```
python-server/
  app/
    main.py                # FastAPI 엔트리포인트 및 API 라우팅
    services/              # 비즈니스 로직 (초안, 채널, 법률, 사진 안내 등)
      draft_generator.py
      photo_guide_service.py
      legal_basis.py
      channel_recommendation_service.py
      gpt_service.py
    models/                # 데이터 모델
      civicdraft.py        # 민원 초안 관련
      channel.py           # 채널 추천 관련
    __init__.py
  requirements.txt
  README.md
  .env
```

## 역할 및 주요 기능
- **main.py** : FastAPI 엔트리포인트, API 라우팅, 예외 처리
- **models/** : CivicDraft, Channel 등 데이터 모델 관리
- **services/** : GPT 기반 초안 생성, 채널 추천, 법률 근거, 사진 안내 등 비즈니스 로직

## 주요 API
- `/process/stream` : 민원 요약/제목/법률 근거 기반 GPT 초안 실시간 스트림 반환
- `/api/photo-guide` : 초안에서 사진 첨부 안내 항목만 리스트로 추출
- `/api/recommend` : 민원 요약과 채널 목록을 받아 AI가 적합한 채널 후보 추천
- `/api/legal-basis/candidates` : 민원 요약/제목을 받아 GPT 기반 법률 후보 리스트와 근거 요약 반환

## 환경설정
- `.env` 파일에 `OPENAI_API_KEY` 등 환경변수 필요
- `requirements.txt` 패키지 설치 필요

## 실행 방법
```bash
uvicorn main:app --host 0.0.0.0 --port 8000
```

## 연동 구조
- 프론트엔드에서 초안 결과를 편집 후, 자바(Spring) CivicDraft 저장 API로 POST
- 모든 비즈니스/AI 로직은 파이썬 서버에서 처리, DB 저장/알림은 자바(Spring)에서 담당
- API 명세 및 예시는 Swagger/OpenAPI 또는 Postman으로 확인 가능
