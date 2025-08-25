    package kr.ddm.civic.civic_assist.service;

    import org.springframework.stereotype.Service;
    import org.springframework.beans.factory.annotation.Autowired;
    import kr.ddm.civic.civic_assist.dto.CivicAssistRequest;
    import kr.ddm.civic.civic_assist.dto.CivicAssistResponse;
    import kr.ddm.civic.civic_assist.model.CivicAssist;
    import kr.ddm.civic.civic_assist.repository.CivicAssistRepository;
    import java.util.*;

    @Service
    public class CivicAssistService {
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
        private CivicAssistRepository civicAssistRepository;

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

        public CivicAssistResponse processRequest(CivicAssistRequest request) {
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
            CivicAssist entity = new CivicAssist();
            entity.setUserText(request.getUserText());
            entity.setPhotos(request.isPhotos());
            entity.setVideos(request.isVideos());
            entity.setLocationText(request.getLocationText());
            entity.setLegalCandidatesJson(request.getLegalCandidatesJson());
            civicAssistRepository.save(entity);

            // 응답 생성
            CivicAssistResponse response = new CivicAssistResponse();
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
