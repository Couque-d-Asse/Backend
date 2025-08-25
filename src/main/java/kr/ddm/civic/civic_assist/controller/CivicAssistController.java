package kr.ddm.civic.civic_assist.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import kr.ddm.civic.civic_assist.service.CivicAssistService;
import kr.ddm.civic.civic_assist.dto.CivicAssistRequest;
import kr.ddm.civic.civic_assist.dto.CivicAssistResponse;

@RestController
@RequestMapping("/api/civic-assist")
public class CivicAssistController {

    @Autowired
    private CivicAssistService civicAssistService;

    @PostMapping("/draft")
    public CivicAssistResponse createDraft(@RequestBody CivicAssistRequest request) {
        return civicAssistService.processRequest(request);
    }
}
