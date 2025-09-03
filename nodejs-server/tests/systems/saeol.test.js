// saeol generator 기본 테스트
const saeol = require('../../src/systems/saeol');

test('saeol generator returns success', async () => {
  const result = await saeol({ system: 'saeol', title: '테스트', content: '내용', options: {} });
  expect(result.success).toBe(true);
  expect(result.system).toBe('saeol');
});
