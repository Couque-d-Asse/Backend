package kr.ddm.civic.civicdraft.service;

import org.springframework.stereotype.Service;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 민원 초안 본문 및 첨부 안내 생성 서비스
 */
@Schema(description = "민원 초안 본문 및 첨부 안내 생성 서비스. GPT 기반 본문, 첨부 안내 텍스트 생성.")
@Service
public class DraftGeneratorService {
    /**
     * 민원 초안 본문 및 첨부 안내 생성
     * @param facts 사실 리스트
     * @param desiredActions 요청사항 리스트
     * @param location 민원 발생 위치
     * @param issueType 민원 유형
     * @return 초안 정보(title, body, bulletedRequests, attachmentGuidance)
     */
    public Map<String, Object> generateDraft(List<String> facts, List<String> desiredActions, String location, String issueType) {
        Map<String, Object> draft = new HashMap<>();
        // Python 서버에 요청할 userText 생성 (CivicDraftRequest의 모든 주요 필드 포함)
        StringBuilder userText = new StringBuilder();
        userText.append("민원 제목: ").append(issueType != null ? issueType : "").append(" ").append(location != null ? location : "").append("\n");
        // 실제 CivicDraftRequest 객체를 받아야 더 풍부하게 가능하지만, 현재 파라미터 기반으로 최대한 정보 포함
        userText.append("상세주소: ").append(location != null ? location : "").append("\n");
        userText.append("민원 내용: ").append(issueType != null ? issueType : "").append("\n");
        if (!facts.isEmpty()) {
            userText.append("사실: ").append(String.join(", ", facts)).append("\n");
        }
        if (!desiredActions.isEmpty()) {
            userText.append("요청사항: ").append(String.join(", ", desiredActions)).append("\n");
        }

        // OkHttp로 Python 서버 /process 호출
        try {
            okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
            okhttp3.MediaType JSON = okhttp3.MediaType.parse("application/json; charset=utf-8");
            String url = "http://localhost:8000/process";
            String json = "{\"userText\":\"" + escapeJson(userText.toString()) + "\",\"photos\":false,\"videos\":false,\"locationText\":\"" + escapeJson(location) + "\"}";
            System.out.println("[DraftGeneratorService] Python 서버로 전달되는 JSON: " + json);
            okhttp3.RequestBody body = okhttp3.RequestBody.create(json, JSON);
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Accept", "application/json")
                    .build();
            okhttp3.Response response = client.newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                String resp = response.body().string();
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                @SuppressWarnings("unchecked")
                Map<String, Object> respMap = (Map<String, Object>) mapper.readValue(resp, Map.class);
                draft.put("title", respMap.getOrDefault("title", "AI 초안 제목"));
                draft.put("body", respMap.getOrDefault("body", ""));
                draft.put("bulletedRequests", respMap.getOrDefault("bulletedRequests", desiredActions));
                draft.put("attachmentGuidance", respMap.getOrDefault("attachmentGuidance", "현장 사진(문제 상황, 위치 식별 가능), 관련 영상(필요시), 증빙자료 첨부 시 처리 속도 향상"));
            } else {
                draft.put("title", location + " " + issueType + " 관련 민원");
                draft.put("body", userText.toString());
                draft.put("bulletedRequests", desiredActions);
                draft.put("attachmentGuidance", "현장 사진(문제 상황, 위치 식별 가능), 관련 영상(필요시), 증빙자료 첨부 시 처리 속도 향상");
            }
        } catch (Exception e) {
            draft.put("title", location + " " + issueType + " 관련 민원");
            draft.put("body", userText.toString());
            draft.put("bulletedRequests", desiredActions);
            draft.put("attachmentGuidance", "현장 사진(문제 상황, 위치 식별 가능), 관련 영상(필요시), 증빙자료 첨부 시 처리 속도 향상");
        }
        return draft;
    }

    // JSON 특수문자 이스케이프
    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ");
    }
}
