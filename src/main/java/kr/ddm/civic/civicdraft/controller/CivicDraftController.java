package kr.ddm.civic.civicdraft.controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/civicdraft")
public class CivicDraftController {

    private final kr.ddm.civic.civicdraft.service.CivicDraftService civicDraftService;

    public CivicDraftController(kr.ddm.civic.civicdraft.service.CivicDraftService civicDraftService) {
        this.civicDraftService = civicDraftService;
    }

    /**
     * 민원 초안 저장 엔드포인트
     */
    @PostMapping
    public kr.ddm.civic.civicdraft.dto.CivicDraftResponse saveDraft(@RequestBody kr.ddm.civic.civicdraft.dto.CivicDraftRequest request) {
        return civicDraftService.saveDraft(request);
    }
}
