package kr.ddm.civic.civic_assist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import kr.ddm.civic.civic_assist.model.CivicAssist;

public interface CivicAssistRepository extends JpaRepository<CivicAssist, Long> {
}
