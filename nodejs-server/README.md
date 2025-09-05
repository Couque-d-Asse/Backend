
# 북마클릿 통합 API 명세

## 1. 시스템 목록 조회
- **GET** `/api/systems`
- 설명: 지원하는 북마클릿 시스템(key, displayName) 목록 반환

### 응답 예시
```json
[
  { "key": "ddm", "displayName": "동대문구청" }
  // 확장 시 safety, saeol 등 추가
]
```

---

## 2. 북마클릿 코드 생성 (통합)
- **POST** `/api/bookmarklet/generate`
- 설명: 템플릿/사용자 정보 기반 북마클릿 코드 생성 (system 값으로 채널 선택)

### 요청 예시
```json
{
  "template": {
    "cvplSj": "민원 제목",
    "cvplCn": "민원 내용",
    "othbcAt": "Y",
    "smsAt": "N"
  },
  "userInfo": {
    "userId": "아이디",
    "userPw": "비밀번호",
    "phoneNumber": "010-1234-5678",
    "email": "user@email.com"
  },
  "system": "ddm" // (생략 시 기본: ddm, 확장 시 safety/saeol 등)
}
```

### 응답 예시
```json
{
  "success": true,
  "system": "ddm",
  "bookmarkletCode": "javascript:(function(){...})",
  "message": "동대문구청 북마클릿 코드 생성 성공"
}
```

---

## 3. 프론트 연동/테스트 방법
1. `/api/bookmarklet/generate` API에 POST 요청 → `bookmarkletCode` 필드 반환
2. 반환된 코드를 북마클릿으로 등록하거나, 브라우저 주소창에 붙여 실행
3. 템플릿/사용자 정보는 실제 민원 양식/로그인 정보에 맞게 전달

### fetch 예시
```js
fetch('/api/bookmarklet/generate', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ template, userInfo, system: 'ddm' })
})
.then(res => res.json())
.then(data => {
  // data.bookmarkletCode 활용
});
```

---
