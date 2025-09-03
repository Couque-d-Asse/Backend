package kr.ddm.civic.civicdraft.dto;

/**
 * 민원 초안 저장 요청 DTO
 * - body: 민원 초안 본문
 * - publicVisibility: 공개/비공개 여부 ("public" 또는 "private")
 */
public class CivicDraftRequest {
    /** 민원 초안 본문 */
    private String body;
    /** 공개/비공개 여부 ("public" 또는 "private") */
    private String publicVisibility;

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getPublicVisibility() { return publicVisibility; }
    public void setPublicVisibility(String publicVisibility) { this.publicVisibility = publicVisibility; }
}
