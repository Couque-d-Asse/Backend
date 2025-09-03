// 시스템 레지스트리 및 generator 관리
const ddm = require('./ddm');
const safety = require('./safety');
const saeol = require('./saeol');

const systems = [
  { key: 'ddm', displayName: '두드림', generator: ddm },
  { key: 'safety', displayName: '세이프티', generator: safety },
  { key: 'saeol', displayName: '새올', generator: saeol }
];

function getSystems() {
  return systems.map(({ key, displayName }) => ({ key, displayName }));
}

function getGenerator(key) {
  return systems.find(s => s.key === key)?.generator;
}

module.exports = { getSystems, getGenerator };
