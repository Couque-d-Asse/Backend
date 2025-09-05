// 통합 북마클릿 서버 진입점 (Node.js API Entry Point)
const express = require('express');
const cors = require('cors');
const path = require('path');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 3000;

// Swagger(OpenAPI) 문서 자동화
// 설치 필요: npm install swagger-jsdoc swagger-ui-express
const swaggerUi = require('swagger-ui-express');
const swaggerJSDoc = require('swagger-jsdoc');

const swaggerSpec = swaggerJSDoc({
  definition: {
    openapi: '3.0.0',
    info: {
      title: '북마클릿 통합 API',
      version: '1.0.0',
      description: '북마클릿 코드 생성 API 명세 (확장성 중심)'
    },
    servers: [
      { url: 'http://localhost:' + PORT }
    ]
  },
  apis: ['./src/routes/*.js'], // 라우터 파일에 Swagger 주석 작성 가능
});
app.use('/api-docs', swaggerUi.serve, swaggerUi.setup(swaggerSpec));

// CORS 허용 (API 외부 연동 가능)
app.use(cors());
// JSON 파싱 미들웨어
app.use(express.json());

// 정적 파일 제공 (PWA, 이미지 등) - 프론트 분리 시 불필요
app.use('/public', express.static(path.join(__dirname, '../public')));

// API 라우터 연결 (북마클릿 관련 모든 API)
const apiRouter = require('./routes');
app.use('/api', apiRouter);

// 헬스체크 엔드포인트 (서버 상태 확인용)
app.get('/health', (req, res) => {
  res.json({ status: 'ok', uptimeSec: Math.floor(process.uptime()) });
});

// 글로벌 에러 핸들러 (API 오류 응답)
app.use((err, req, res, next) => {
  console.error(err);
  res.status(err.status || 500).json({ success: false, message: err.message || 'Internal Server Error' });
});

// 서버 실행
app.listen(PORT, () => {
  console.log(`Nodejs 통합 서버가 ${PORT}번 포트에서 실행 중입니다.`);
  console.log(`Swagger UI: http://localhost:${PORT}/api-docs`);
});
