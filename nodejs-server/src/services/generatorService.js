// 시스템 generator 호출 및 결과 표준화
// generator: 시스템별 함수, input: { system, title, content, options }

module.exports = async function generatorService(generator, input) {
  // 간단한 입력 검증 (필요시 확장)
  if (!input.system || !input.content) {
    return { success: false, system: input.system, message: '필수 입력값 누락' };
  }
  try {
    const result = await generator(input);
    return result;
  } catch (err) {
    return { success: false, system: input.system, message: err.message || 'generator 오류' };
  }
};
