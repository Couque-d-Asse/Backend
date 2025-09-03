// 환경변수 관리
require('dotenv').config();

const PORT = process.env.PORT || 3000;
const NODE_ENV = process.env.NODE_ENV || 'development';
const REDIS_URL = process.env.REDIS_URL || '';

module.exports = { PORT, NODE_ENV, REDIS_URL };
