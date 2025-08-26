package kr.ddm.civic.civicdraft.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintRequest {
    private String reportType; // 신고유형 (01: 시설물안전)
    private String title; // 제목
    private String contents; // 내용
    private String phone; // 전화번호
    private String shareYn; // 공유여부 (Y/N)
    private String category; // 구분 (1: 일반시민)
    private String name; // 이름
    private String emailId; // 이메일 앞자리
    private String emailDomain; // 이메일 도메인
    private boolean agreePersonalInfo; // 개인정보 동의
    private String location; // 위치 정보 (선택)
}