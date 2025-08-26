package kr.ddm.civic.civicdraft.controller;

import kr.ddm.civic.civicdraft.dto.ComplaintRequest;
import kr.ddm.civic.civicdraft.dto.BookmarkletResponse;
import kr.ddm.civic.civicdraft.service.BookmarkletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/bookmarklet")
@RequiredArgsConstructor
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:8080" })
public class BookmarkletController {

    private final BookmarkletService bookmarkletService;

    @PostMapping("/generate")
    public ResponseEntity<BookmarkletResponse> generateBookmarklet(@RequestBody ComplaintRequest request) {
        log.info("북마클릿 생성 요청 수신");
        BookmarkletResponse response = bookmarkletService.generateBookmarklet(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Spring Boot 서버가 정상 작동중입니다.");
    }
}