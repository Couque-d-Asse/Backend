// 통합 북마클릿 서버 진입점
const express = require('express');
const cors = require('cors');
const path = require('path');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 3000;

app.use(cors());
app.use(express.json());

// 정적 파일 제공 (PWA, 이미지)
app.use('/public', express.static(path.join(__dirname, '../public')));

// API 라우터 연결
const apiRouter = require('./routes');
app.use('/api', apiRouter);

// 헬스체크
app.get('/health', (req, res) => {
  res.json({ status: 'ok', uptimeSec: Math.floor(process.uptime()) });
});

// 에러 핸들러
app.use((err, req, res, next) => {
  console.error(err);
  res.status(err.status || 500).json({ success: false, message: err.message || 'Internal Server Error' });
});

app.listen(PORT, () => {
  console.log(`Nodejs 통합 서버가 ${PORT}번 포트에서 실행 중입니다.`);
});
