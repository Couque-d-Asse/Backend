package kr.ddm.civic.civicdraft.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.beans.factory.annotation.Autowired;
import kr.ddm.civic.civicdraft.service.CivicDraftService;
import kr.ddm.civic.civicdraft.dto.CivicDraftRequest;

@RestController
@RequestMapping("/api/civicdraft")
public class CivicDraftController {

    @Autowired
    private CivicDraftService civicDraftService;

    @PostMapping(value = "/draft/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamDraft(@RequestBody CivicDraftRequest request) {
        SseEmitter emitter = new SseEmitter();
        civicDraftService.processRequestSse(request, emitter);
        return emitter;
    }
}
