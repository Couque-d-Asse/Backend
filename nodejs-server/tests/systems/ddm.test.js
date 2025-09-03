// ddm generator 기본 테스트
const ddm = require('../../src/systems/ddm');

test('ddm generator returns success', async () => {
  const result = await ddm({ system: 'ddm', title: '테스트', content: '내용', options: {} });
  expect(result.success).toBe(true);
  expect(result.system).toBe('ddm');
});
