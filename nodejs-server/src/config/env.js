// 환경변수 관리 및 서버 설정값 모듈화
require('dotenv').config();

// 서버 포트 (기본: 3000)
const PORT = process.env.PORT || 3000;
// 실행 환경 (기본: development)
const NODE_ENV = process.env.NODE_ENV || 'development';

module.exports = { PORT, NODE_ENV, REDIS_URL };
