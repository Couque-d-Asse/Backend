package kr.ddm.civic.civicdraft.dto;

import java.util.List;

/**
 * 민원 분류 추천용 입력 모델
 */
public class Issue {
    private String title;
    private String description;
    private String location; // 선택
    private List<String> tags; // 선택
    private int evidenceCount; // 선택

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public int getEvidenceCount() { return evidenceCount; }
    public void setEvidenceCount(int evidenceCount) { this.evidenceCount = evidenceCount; }
}
