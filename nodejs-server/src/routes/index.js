// /api 라우터 진입점 (모든 API 엔드포인트 집결)
const express = require('express');
const router = express.Router();

// 북마클릿 코드 생성 API 라우터 연결 (/api/bookmarklet)
const bookmarkletRouter = require('./bookmarklet');
router.use('/bookmarklet', bookmarkletRouter);

module.exports = router;
