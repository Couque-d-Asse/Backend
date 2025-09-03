// 커스텀 에러 클래스 및 Express 에러 미들웨어
class AppError extends Error {
  constructor(message, status = 500, code = 'ERR_UNKNOWN') {
    super(message);
    this.status = status;
    this.code = code;
  }
}

function errorMiddleware(err, req, res, next) {
  res.status(err.status || 500).json({
    success: false,
    message: err.message || 'Internal Server Error',
    code: err.code || 'ERR_UNKNOWN'
  });
}

module.exports = { AppError, errorMiddleware };
