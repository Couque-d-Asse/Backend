// 간단한 로깅 유틸
function log(...args) {
  console.log('[LOG]', ...args);
}

function error(...args) {
  console.error('[ERROR]', ...args);
}

module.exports = { log, error };
