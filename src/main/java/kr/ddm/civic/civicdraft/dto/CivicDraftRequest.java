package kr.ddm.civic.civicdraft.dto;

/**
 * 민원 초안 생성 요청 DTO
 * 사용자가 입력하는 최소 정보만 포함
 */
public class CivicDraftRequest {
    /**
     * 민원 내용 (필수)
     */
    private String userText;

    /**
     * 위치 정보 (선택)
     */
    private String locationText;

    /**
     * 사진 첨부 여부 (선택)
     */
    private boolean photos;

    /**
     * 동영상 첨부 여부 (선택)
     */
    private boolean videos;

    // Getter/Setter
    public String getUserText() { return userText; }
    public void setUserText(String userText) { this.userText = userText; }

    public String getLocationText() { return locationText; }
    public void setLocationText(String locationText) { this.locationText = locationText; }

    public boolean isPhotos() { return photos; }
    public void setPhotos(boolean photos) { this.photos = photos; }

    public boolean isVideos() { return videos; }
    public void setVideos(boolean videos) { this.videos = videos; }
}
