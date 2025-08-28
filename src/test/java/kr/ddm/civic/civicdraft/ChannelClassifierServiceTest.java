package kr.ddm.civic.civicdraft;

import kr.ddm.civic.civicdraft.dto.Issue;
import kr.ddm.civic.civicdraft.dto.RecommendationResponse;
import kr.ddm.civic.civicdraft.service.ChannelClassifierService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ChannelClassifierServiceTest {
    @Autowired
    private ChannelClassifierService service;

    @Test
    void testIllegalParkingWithPhotos() {
    Issue issue = new Issue();
    issue.setSummary("이문로 불법주정차, 사진 2장 첨부");
    RecommendationResponse resp = service.recommend(issue);
    assertEquals("safety_report", resp.getRecommendedChannel());
    assertTrue(resp.getOptions().stream().anyMatch(opt -> opt.getId().equals("safety_report") && opt.isHighlight()));
    }
    // ...추가 테스트 케이스는 요구사항에 따라 작성...
}
