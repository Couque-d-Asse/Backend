// 요청 데이터 기본 검증 유틸
function validateGenerateInput(input) {
  if (!input.system || typeof input.system !== 'string') return false;
  if (!input.content || typeof input.content !== 'string') return false;
  // title, options는 선택
  return true;
}

module.exports = { validateGenerateInput };
