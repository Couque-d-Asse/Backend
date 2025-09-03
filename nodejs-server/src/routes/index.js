// /api 라우터 진입점
const express = require('express');
const router = express.Router();

// 시스템 관련 라우터 연결
const systemsRouter = require('./systems');
router.use('/', systemsRouter);

module.exports = router;
