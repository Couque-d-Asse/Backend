package kr.ddm.civic.civicdraft.model;

import jakarta.persistence.*;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 민원 초안 DB 엔티티
 */
@Schema(description = "민원 초안 DB 엔티티. 본문 및 공개/비공개 여부 저장.")
@Entity
public class CivicDraft {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "DB PK", example = "1")
    private Long id;
    @Column(columnDefinition = "TEXT")
    @Schema(description = "초안 본문", example = "안녕하십니까.\n저희 동네 이문로 123 앞 도로에 최근 신호등 고장이 발생하여 차량 통행이 매우 위험한 상황입니다.\n특히 출퇴근 시간대에 교통사고 위험이 높아 주민들의 불안이 커지고 있습니다.\n빠른 신호등 수리와 안전 점검을 요청드립니다.")
    private String body;

    @Column(name = "public_visibility", length = 16)
    @Schema(description = "공개/비공개", example = "public")
    private String publicVisibility;

        @Column(nullable = false)
        @Schema(description = "첨부 사진 경로 또는 메타데이터", example = "")
        private String photos = "";

    public String getPublicVisibility() { return publicVisibility; }
    public void setPublicVisibility(String publicVisibility) { this.publicVisibility = publicVisibility; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

        public String getPhotos() { return photos; }
        public void setPhotos(String photos) { this.photos = photos == null ? "" : photos; }
}
