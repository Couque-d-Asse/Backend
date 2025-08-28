package kr.ddm.civic.civicdraft.controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.beans.factory.annotation.Autowired;
import kr.ddm.civic.civicdraft.service.CivicDraftService;
import kr.ddm.civic.civicdraft.dto.DraftRequest;

@RestController
@RequestMapping("/api/civicdraft")
public class CivicDraftController {

    @Autowired
    private CivicDraftService civicDraftService;


    /**
     * 초안 생성 SSE 스트림 (실시간 민원 본문 생성)
     * 프론트엔드는 chunk 단위로 실시간 본문을 받아 UI에 반영
     */
    @Operation(
        summary = "실시간 초안 생성 SSE 스트림",
        description = "민원 요약/제목 기반으로 실시간 본문 chunk를 반환합니다.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "SSE 스트림 응답 (text/event-stream)",
                content = @Content(mediaType = "text/event-stream",
                    schema = @Schema(type = "string", example = "data: 민원 chunk...\ndata: [END]"))
            )
        }
    )
    @PostMapping(value = "/draft/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamDraft(@RequestBody DraftRequest request) {
        SseEmitter emitter = new SseEmitter(10 * 60 * 1000L); // 10분 타임아웃
        civicDraftService.processRequestSse(request, emitter);
        return emitter;
    }
}
