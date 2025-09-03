# Nodejs 통합 북마클릿 서버

## 목적
- 시스템별 북마클릿 생성 API를 하나의 Node 서버에서 통합 제공
- Spring 등 외부 서비스와 연동 가능

## 폴더 구조
```
nodejs-server/
  package.json
  src/
    server.js
    routes/
    systems/
    services/
    utils/
    config/
  public/
    ddmmarklet/
    safetymarklet/
    saeolmarklet/
  tests/
```

## 실행 방법
```bash
npm install
npm run dev
```

## 주요 엔드포인트
- GET /health
- GET /api/systems
- POST /api/generate

## 환경변수
- .env.example 참고
