
package kr.ddm.civic.civicdraft.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 민원 초안 생성 요청 DTO (Python CivicAssistRequest와 동일)
 */
@Schema(description = "민원 초안 생성 요청 DTO. Python CivicAssistRequest와 동일하게 summary, title만 포함.")
public class DraftRequest {
    @Schema(description = "민원 요약")
    private String summary;
    @Schema(description = "민원 제목")
    private String title;

    public DraftRequest() {}
    public DraftRequest(String summary, String title) {
        this.summary = summary;
        this.title = title;
    }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    @Override
    public String toString() {
        return "DraftRequest{" +
                "summary='" + summary + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}