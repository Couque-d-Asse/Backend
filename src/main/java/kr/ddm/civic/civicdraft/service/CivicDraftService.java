package kr.ddm.civic.civicdraft.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

    import org.springframework.stereotype.Service;
    import org.springframework.beans.factory.annotation.Autowired;
    import kr.ddm.civic.civicdraft.dto.CivicDraftRequest;
    import kr.ddm.civic.civicdraft.dto.CivicDraftResponse;
    import kr.ddm.civic.civicdraft.model.CivicDraft;
    import kr.ddm.civic.civicdraft.repository.CivicDraftRepository;
    import java.util.*;

    @Service
    public class CivicDraftService {
    public void processRequestSse(CivicDraftRequest request, SseEmitter emitter) {
        // 예시: 토큰 단위로 AI 응답을 받아 emitter로 전송
        try {
            // 실제로는 Python 서버에 SSE 요청 후, 응답을 emitter.send()로 전달
            for (int i = 0; i < 5; i++) {
                emitter.send("draft_chunk_" + i);
                Thread.sleep(500); // 예시: 0.5초마다 전송
            }
            emitter.complete();
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }
        // Object를 List<String>으로 안전하게 변환하는 유틸리티
        private List<String> toStringList(Object obj) {
            List<String> result = new ArrayList<>();
            if (obj instanceof List<?>) {
                for (Object item : (List<?>) obj) {
                    if (item instanceof String) {
                        result.add((String) item);
                    }
                }
            }
            return result;
        }

        @Autowired
        private CivicDraftRepository civicDraftRepository;

        @Autowired
        private InputParserService inputParserService;
        @Autowired
        private ChannelClassifierService channelClassifierService;
        @Autowired
        private DraftGeneratorService draftGeneratorService;
        @Autowired
        private LegalBasisService legalBasisService;
        @Autowired
        private QualityAssessorService qualityAssessorService;
        @Autowired
        private LegalCandidatesParserService legalCandidatesParserService;
        @Autowired
        private ChannelFieldsBuilderService channelFieldsBuilderService;

        public CivicDraftResponse processRequest(CivicDraftRequest request) {
            // 1. 입력 파싱(구조화)
            Map<String, Object> parsed = inputParserService.parse(
                request.getUserText(), request.isPhotos(), request.isVideos(), request.getLocationText()
            );

            // 2. 채널 분류
            String channel = channelClassifierService.classify((String) parsed.get("issueType"));

            // 3. 초안 생성
            List<String> facts = toStringList(parsed.get("facts"));
            List<String> desiredActions = toStringList(parsed.get("desiredActions"));
            Map<String, Object> draft = draftGeneratorService.generateDraft(
                facts,
                desiredActions,
                (String) parsed.get("location"),
                (String) parsed.get("issueType")
            );

            // 4. 법령 근거 첨부
            List<Map<String, Object>> legalCandidates = legalCandidatesParserService.parse(request.getLegalCandidatesJson());
            List<Map<String, Object>> legalBasis = legalBasisService.buildLegalBasis(legalCandidates);

            // 5. 품질/안전 평가
            // TODO: 실제 평가 로직 구현 필요 (채널 적합성, 내용 일관성, 법령 매칭 품질 등)
            double confidence = qualityAssessorService.assessConfidence(true, true, true);
            Map<String, Object> safetyFlags = qualityAssessorService.assessSafety((String) draft.get("body"));

            // DB 저장
        CivicDraft entity = new CivicDraft();
            entity.setUserText(request.getUserText());
            entity.setPhotos(request.isPhotos());
            entity.setVideos(request.isVideos());
            entity.setLocationText(request.getLocationText());
            entity.setLegalCandidatesJson(request.getLegalCandidatesJson());
        civicDraftRepository.save(entity);

            // 응답 생성
        CivicDraftResponse response = new CivicDraftResponse();
            response.setChannel(channel);
            response.setTitle((String) draft.get("title"));
            response.setBody((String) draft.get("body"));
            List<String> bulletedRequests = toStringList(draft.get("bulletedRequests"));
            response.setBulletedRequests(bulletedRequests);
            Map<String, Object> channelFields = channelFieldsBuilderService.build(channel, (String) parsed.get("location"));
            response.setChannelFields(channelFields);
            response.setLegalBasis(legalBasis);
            response.setConfidence(confidence);
            List<String> missingFields = toStringList(parsed.get("missingFields"));
            response.setMissingFields(missingFields);
            response.setSafetyFlags(safetyFlags);
            return response;
        }
    }
