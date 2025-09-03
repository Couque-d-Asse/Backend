// 시스템 관련 API 라우터
const express = require('express');
const router = express.Router();

const { getSystems, getGenerator } = require('../systems');
const generatorService = require('../services/generatorService');

// 시스템 목록 조회
router.get('/systems', (req, res) => {
  res.json(getSystems());
});

// 북마클릿 생성 요청
router.post('/generate', async (req, res, next) => {
  try {
    const { system, title, content, options } = req.body;
    const generator = getGenerator(system);
    if (!generator) {
      return res.status(400).json({ success: false, message: '지원하지 않는 system입니다.' });
    }
    const result = await generatorService(generator, { system, title, content, options });
    res.json(result);
  } catch (err) {
    next(err);
  }
});

module.exports = router;
