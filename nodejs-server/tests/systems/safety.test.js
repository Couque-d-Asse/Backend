// safety generator 기본 테스트
const safety = require('../../src/systems/safety');

test('safety generator returns success', async () => {
  const result = await safety({ system: 'safety', title: '테스트', content: '내용', options: {} });
  expect(result.success).toBe(true);
  expect(result.system).toBe('safety');
});
