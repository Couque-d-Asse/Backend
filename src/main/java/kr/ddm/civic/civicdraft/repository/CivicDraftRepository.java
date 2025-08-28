package kr.ddm.civic.civicdraft.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import kr.ddm.civic.civicdraft.model.CivicDraft;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 민원 초안 DB 저장소
 */
@Schema(description = "민원 초안 DB 저장소. CivicDraft 엔티티 관리.")
public interface CivicDraftRepository extends JpaRepository<CivicDraft, Long> {
}
