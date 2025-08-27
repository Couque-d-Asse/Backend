package kr.ddm.civic.civicdraft.service;

import org.springframework.stereotype.Service;
// import 제거: 더 이상 사용하지 않음
import java.util.*;

@Service
public class LegalCandidatesParserService {
    /**
     * 법령 후보 파싱 기능 제거됨 (legalCandidatesJson 사용하지 않음)
     * 필요시 다른 입력 방식으로 확장 가능
     */
    public List<Map<String, Object>> parse() {
        return new ArrayList<>();
    }
}
