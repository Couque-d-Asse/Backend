package kr.ddm.civic.civicdraft.service;

import kr.ddm.civic.civicdraft.dto.ComplaintRequest;
import kr.ddm.civic.civicdraft.dto.BookmarkletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

@Slf4j
@Service
public class BookmarkletService {

    @Value("${nodejs.server.url}")
    private String nodejsServerUrl;

    private final RestTemplate restTemplate;

    public BookmarkletService() {
        this.restTemplate = new RestTemplate();
    }

    public BookmarkletResponse generateBookmarklet(ComplaintRequest request) {
        try {
            String url = nodejsServerUrl + "/api/generate-bookmarklet";
            log.info("Node.js 서버로 요청 전송: {}", url);
            log.debug("요청 데이터: {}", request);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<ComplaintRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<BookmarkletResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    BookmarkletResponse.class);

            log.info("북마클릿 생성 성공: templateId={}",
                    response.getBody() != null ? response.getBody().getTemplateId() : "null");

            return response.getBody();

        } catch (RestClientException e) {
            log.error("Node.js 서버 통신 오류: ", e);

            BookmarkletResponse errorResponse = new BookmarkletResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Node.js 서버 연결 실패: " + e.getMessage());
            return errorResponse;
        }
    }
}