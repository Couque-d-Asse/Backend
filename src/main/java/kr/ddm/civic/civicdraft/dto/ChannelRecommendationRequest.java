package kr.ddm.civic.civicdraft.dto;

import java.util.List;
import java.util.Map;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 채널 추천 요청 DTO (Python 서버로 전달)
 */
@Schema(description = "채널 추천 요청 DTO. issue(요약), channels(목록) 포함.")
public class ChannelRecommendationRequest {
    @Schema(description = "민원 요약/입력 모델")
    private Issue issue;
    @Schema(description = "추천 대상 채널 목록")
    private List<Map<String, Object>> channels;

    public ChannelRecommendationRequest() {}
    public ChannelRecommendationRequest(Issue issue, List<Map<String, Object>> channels) {
        this.issue = issue;
        this.channels = channels;
    }
    public Issue getIssue() { return issue; }
    public void setIssue(Issue issue) { this.issue = issue; }
    public List<Map<String, Object>> getChannels() { return channels; }
    public void setChannels(List<Map<String, Object>> channels) { this.channels = channels; }
}