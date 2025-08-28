package kr.ddm.civic.civicdraft.model;

import jakarta.persistence.*;

@Entity
public class CivicDraft {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(columnDefinition = "TEXT")
    private String body; // 초안 본문

    @Column(name = "public_visibility", length = 16)
    private String publicVisibility; // 공개/비공개

    public String getPublicVisibility() { return publicVisibility; }
    public void setPublicVisibility(String publicVisibility) { this.publicVisibility = publicVisibility; }

    // getter/setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
}
