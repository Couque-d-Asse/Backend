package kr.ddm.civic.civicdraft.dto;

/**
 * 민원 초안 저장 응답 DTO
 * - id: DB에 저장된 민원 초안 PK
 * - body: 저장된 민원 초안 본문
 * - publicVisibility: 저장된 공개/비공개 여부
 */
public class CivicDraftResponse {
    /** DB에 저장된 민원 초안 PK */
    private Long id;
    /** 저장된 민원 초안 본문 */
    private String body;
    /** 저장된 공개/비공개 여부 */
    private String publicVisibility;

    public CivicDraftResponse(Long id, String body, String publicVisibility) {
        this.id = id;
        this.body = body;
        this.publicVisibility = publicVisibility;
    }

    public Long getId() { return id; }
    public String getBody() { return body; }
    public String getPublicVisibility() { return publicVisibility; }
}
