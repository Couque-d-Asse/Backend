package kr.ddm.civic.civicdraft.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;
import kr.ddm.civic.civicdraft.dto.*;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ChannelClassifierService {
    private static final Logger log = LoggerFactory.getLogger(ChannelClassifierService.class);

    @Value("classpath:classifier.yml")
    private Resource classifierConfig;

    @SuppressWarnings("unchecked")
    public RecommendationResponse recommend(Issue issue) {
        try {
            // 1. classifier.yml 로드
            Yaml yaml = new Yaml();
            Map<String, Object> config = null;
            try (java.io.InputStreamReader reader = new java.io.InputStreamReader(classifierConfig.getInputStream(), java.nio.charset.StandardCharsets.UTF_8)) {
                config = yaml.load(reader);
            }
            if (config == null) {
                log.error("classifier.yml 파싱 결과가 null입니다.");
                throw new RuntimeException("classifier.yml 파싱 실패");
            }
            Object channelsObj = config.get("channels");
            List<Map<String, Object>> channels = new ArrayList<>();
            if (channelsObj instanceof List<?>) {
                for (Object o : (List<?>) channelsObj) {
                    if (o instanceof Map<?,?>) {
                        channels.add((Map<String, Object>) o);
                    } else {
                        log.warn("channels 내부 요소가 Map이 아님: {}", o);
                    }
                }
            } else {
                log.error("channels 필드가 List가 아님: {}", channelsObj);
                throw new RuntimeException("channels 필드 타입 오류");
            }
            if (channels.isEmpty()) {
                log.error("classifier.yml에 channels가 비어있음");
                throw new RuntimeException("channels가 비어있음");
            }

            // 2. 각 채널별 점수 계산
            List<ChannelOption> options = new ArrayList<>();
            double minScore = Double.MAX_VALUE, maxScore = Double.MIN_VALUE;
            for (Map<String, Object> ch : channels) {
                if (ch == null) continue;
                String id = ch.get("id") instanceof String ? (String) ch.get("id") : "";
                String title = ch.get("title") instanceof String ? (String) ch.get("title") : "";
                // 타입 안전하게 변환
                List<Map<String, Object>> keywords = new ArrayList<>();
                Object keywordsObj = ch.get("keywords");
                if (keywordsObj instanceof List<?>) {
                    for (Object o : (List<?>) keywordsObj) {
                        if (o instanceof Map<?,?>) {
                            keywords.add((Map<String, Object>) o);
                        } else {
                            log.warn("keywords 내부 요소가 Map이 아님: {}", o);
                        }
                    }
                } else if (keywordsObj != null) {
                    log.warn("keywords 필드가 List가 아님: {}", keywordsObj);
                }
                List<Map<String, Object>> penalties = new ArrayList<>();
                Object penaltiesObj = ch.get("penalties");
                if (penaltiesObj instanceof List<?>) {
                    for (Object o : (List<?>) penaltiesObj) {
                        if (o instanceof Map<?,?>) {
                            penalties.add((Map<String, Object>) o);
                        } else {
                            log.warn("penalties 내부 요소가 Map이 아님: {}", o);
                        }
                    }
                } else if (penaltiesObj != null) {
                    log.warn("penalties 필드가 List가 아님: {}", penaltiesObj);
                }
                double score = 0.05; // 최소 바닥값
                List<String> reasonCodes = new ArrayList<>();
                List<String> matchedTerms = new ArrayList<>();

                // 키워드 가중치
                if (issue != null && issue.getDescription() != null) {
                    for (Map<String, Object> kw : keywords) {
                        if (kw == null) continue;
                        String term = kw.get("term") instanceof String ? (String) kw.get("term") : null;
                        Object weightObj = kw.get("weight");
                        double weight = (weightObj instanceof Number) ? ((Number) weightObj).doubleValue() : 0.0;
                        if (term != null && (issue.getDescription().contains(term) || (issue.getTags() != null && issue.getTags().contains(term)))) {
                            score += weight;
                            reasonCodes.add("KW_" + term.toUpperCase());
                            matchedTerms.add(term);
                        }
                    }
                }
                // 증거 가중치
                if (id.equals("safety_report") && issue != null && issue.getEvidenceCount() >= 2) {
                    score += 1.0;
                    reasonCodes.add("EV_PHOTOS>=2");
                }
                // 감점
                if (issue != null && issue.getDescription() != null) {
                    for (Map<String, Object> pen : penalties) {
                        if (pen == null) continue;
                        String term = pen.get("term") instanceof String ? (String) pen.get("term") : null;
                        Object weightObj = pen.get("weight");
                        double weight = (weightObj instanceof Number) ? ((Number) weightObj).doubleValue() : 0.0;
                        if (term != null && issue.getDescription().contains(term)) {
                            score += weight;
                            reasonCodes.add("PEN_" + term.toUpperCase());
                            matchedTerms.add(term);
                        }
                    }
                }
                minScore = Math.min(minScore, score);
                maxScore = Math.max(maxScore, score);

                ChannelOption option = new ChannelOption();
                option.setId(id);
                option.setTitle(title);
                option.setScore(score);
                option.setReasonCodes(reasonCodes);
                option.setMatchedTerms(matchedTerms);
                option.setSuggestedTemplate(generateTemplate(id, issue));
                options.add(option);
            }

            if (options.isEmpty()) {
                log.error("추천 options가 비어있음. classifier.yml, 입력값 확인 필요");
                throw new RuntimeException("추천 options가 비어있음");
            }

            // 3. confidence 정규화
            for (ChannelOption opt : options) {
                double conf = (maxScore == minScore) ? 1.0 : (opt.getScore() - minScore) / (maxScore - minScore);
                opt.setConfidence(Math.max(0.05, Math.min(1.0, conf)));
            }

            // 4. 추천 채널 결정 (최고 점수, 동점 시 우선순위)
            ChannelOption best = options.stream().max(Comparator.comparing(ChannelOption::getScore)).orElse(options.get(0));
            final ChannelOption bestFinal;
            List<ChannelOption> top = options.stream().filter(o -> o.getScore() == best.getScore()).collect(Collectors.toList());
            if (top.size() > 1) {
                Map<String, Integer> prio = new HashMap<>();
                for (Map<String, Object> ch : channels) {
                    String cid = ch.get("id") instanceof String ? (String) ch.get("id") : "";
                    Integer priority = ch.get("priority") instanceof Integer ? (Integer) ch.get("priority") : Integer.MAX_VALUE;
                    prio.put(cid, priority);
                }
                bestFinal = top.stream().min(Comparator.comparing(o -> prio.getOrDefault(o.getId(), Integer.MAX_VALUE))).orElse(best);
            } else {
                bestFinal = best;
            }
            for (ChannelOption opt : options) opt.setHighlight(opt.getId().equals(bestFinal.getId()));

            // 5. rationaleSummary 생성 및 응답
            String rationaleSummary = (bestFinal.getReasonCodes().isEmpty()) ? "근거 약함" : reasonCodesToSummary(bestFinal.getReasonCodes(), bestFinal.getMatchedTerms());
            RecommendationResponse resp = new RecommendationResponse();
            resp.setOptions(options);
            resp.setRecommendedChannel(bestFinal.getId());
            resp.setRationaleSummary(rationaleSummary);
            return resp;
        } catch (Exception e) {
            log.error("카드형 추천 recommend() 예외", e);
            throw new RuntimeException("카드형 추천 로직 예외", e);
        }
    }

    // 채널별 suggestedTemplate 생성 규칙
    private String generateTemplate(String id, Issue issue) {
        String location = (issue.getLocation() != null) ? issue.getLocation() : "";
        if (id.equals("safety_report")) {
            return String.format("[%s] 불법주정차로 병목 발생 신고\n날짜/시간대 · 위치: %s · 위반유형 · 정체 영향 · 사진(1분 간격 2장)", location, location);
        } else if (id.equals("mayor_board")) {
            return String.format("[%s] 신호주기 연장 및 좌회전 분리 신설 건의\n대기행렬 · 신호 연동 · 좌회전 분리 · 노면표지 개선 요청", location);
        } else {
            return String.format("[정보공개청구] %s 임시개통·교통관리 계획\n사업명 · 임시개통 검토 · 단계별 시나리오 · 협의 문서 열람 · 처리기한/담당부서", location);
        }
    }

    // reasonCodes를 자연어 요약
    private String reasonCodesToSummary(List<String> codes, List<String> terms) {
        if (codes == null || codes.isEmpty() || terms == null || terms.isEmpty()) {
            log.warn("reasonCodesToSummary called with empty/null codes or terms: codes={}, terms={}", codes, terms);
            return "근거 약함";
        }
        StringBuilder sb = new StringBuilder();
        int limit = Math.min(2, Math.min(codes.size(), terms.size()));
        for (int i = 0; i < limit; i++) {
            if (terms.get(i) == null) {
                log.warn("Null term at index {} in reasonCodesToSummary: codes={}, terms={}", i, codes, terms);
                continue;
            }
            sb.append(terms.get(i)).append(" 신호");
            if (i < limit - 1) sb.append(", ");
        }
        if (sb.length() == 0) return "근거 약함";
        return sb.toString();
    }
}
