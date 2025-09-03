package kr.ddm.civic.civicdraft.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import kr.ddm.civic.civicdraft.model.CivicDraft;
import kr.ddm.civic.civicdraft.repository.CivicDraftRepository;

/**
 * 민원 초안 생성 서비스
 * - DB 저장(공개일 경우)
 */
@Service
public class CivicDraftService {

    @Autowired
    private CivicDraftRepository civicDraftRepository;

    /**
     * 민원 초안 DB 저장
     */
    public CivicDraft saveDraft(String body, String publicVisibility) {
        CivicDraft entity = new CivicDraft();
        entity.setBody(body);
        entity.setPublicVisibility(publicVisibility);
        return civicDraftRepository.save(entity);
    }

    /**
     * CivicDraftRequest를 받아 CivicDraftResponse 반환
     */
    public kr.ddm.civic.civicdraft.dto.CivicDraftResponse saveDraft(kr.ddm.civic.civicdraft.dto.CivicDraftRequest request) {
        CivicDraft entity = saveDraft(request.getBody(), request.getPublicVisibility());
        return new kr.ddm.civic.civicdraft.dto.CivicDraftResponse(
            entity.getId(), entity.getBody(), entity.getPublicVisibility()
        );
    }
}
