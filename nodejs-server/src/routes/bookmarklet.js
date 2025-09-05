// /api/bookmarklet 라우터 (북마클릿 코드 생성 API, 확장성 중심)
const express = require('express');
const router = express.Router();

// 시스템별 북마클릿 코드 생성 함수 (확장성 고려)
const ddm = require('../systems/ddm');         // 동대문구청
// const safety = require('../systems/safety'); // 안전신문고 (확장 시 주석 해제)
// const saeol = require('../systems/saeol');   // 새올 (확장 시 주석 해제)

/**
 * @swagger
 * /api/bookmarklet/generate:
 *   post:
 *     summary: 북마클릿 코드 생성 (확장성 중심)
 *     description: |
 *       템플릿/사용자 정보 기반 북마클릿 실행 코드 생성. system 값에 따라 채널 분기(ddm, safety, saeol 등)
 *       - system 값에 따라 각 시스템 함수 호출
 *         - 'ddm'   : 동대문구청 (기본)
 *         - 'safety': 안전신문고 (확장 시 활성화)
 *         - 'saeol' : 새올 (확장 시 활성화)
 *       예시: { template, userInfo, system: 'safety' } → safety 함수 호출
 *     tags:
 *       - Bookmarklet
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             properties:
 *               template:
 *                 type: object
 *                 description: 민원 템플릿 정보
 *               userInfo:
 *                 type: object
 *                 description: 사용자 정보
 *               system:
 *                 type: string
 *                 description: 시스템 구분(ddm/safety/saeol), 기본값 ddm
 *             required:
 *               - template
 *               - userInfo
 *     responses:
 *       200:
 *         description: 북마클릿 코드 생성 성공
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 success:
 *                   type: boolean
 *                 system:
 *                   type: string
 *                 bookmarkletCode:
 *                   type: string
 *                 message:
 *                   type: string
 *       400:
 *         description: 지원하지 않는 system 값 등 오류
 */
router.post('/generate', async (req, res) => {
  try {
    const { template, userInfo, system = 'ddm' } = req.body;
    let result;
    if (system === 'ddm') {
      // 동대문구청 북마클릿 코드 생성
      result = await ddm({ template, userInfo });
    }
    // else if (system === 'safety') {
    //   // 안전신문고 북마클릿 코드 생성 (확장 시 주석 해제)
    //   result = await safety({ template, userInfo });
    // }
    // else if (system === 'saeol') {
    //   // 새올 북마클릿 코드 생성 (확장 시 주석 해제)
    //   result = await saeol({ template, userInfo });
    // }
    else {
      // 지원하지 않는 system 값 처리
      result = { success: false, message: '지원하지 않는 system 값입니다.' };
    }
    res.json(result);
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
});

module.exports = router;
