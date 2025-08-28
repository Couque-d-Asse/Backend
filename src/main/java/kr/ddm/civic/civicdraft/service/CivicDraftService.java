package kr.ddm.civic.civicdraft.service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import kr.ddm.civic.civicdraft.dto.DraftRequest;
import kr.ddm.civic.civicdraft.model.CivicDraft;
import kr.ddm.civic.civicdraft.repository.CivicDraftRepository;
import java.util.*;

/**
 * 민원 초안 생성 서비스
 * - 채널 추천 및 초안 본문 생성(Python)
 * - DB 저장(공개일 경우)
 */
@Service
public class CivicDraftService {
    /**
     * 초안 생성 SSE 스트림 (Python 서버 /process/stream)
     * @param request DraftRequest (summary, title)
     * @param emitter SseEmitter
     */
    public void processRequestSse(DraftRequest request, SseEmitter emitter) {
        // Python 서버 SSE 엔드포인트 호출 (summary, title만 전달)
        try {
            okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
            String url = "http://localhost:8000/process/stream";
            String json = String.format("{\"summary\":\"%s\",\"title\":\"%s\"}",
                escapeJson(request.getSummary()), escapeJson(request.getTitle()));
            okhttp3.RequestBody body = okhttp3.RequestBody.create(json, okhttp3.MediaType.parse("application/json; charset=utf-8"));
            okhttp3.Request httpRequest = new okhttp3.Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Accept", "text/event-stream")
                    .build();
            client.newCall(httpRequest).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, java.io.IOException e) {
                    emitter.completeWithError(e);
                }
                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) {
                    try (java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(response.body().byteStream(), java.nio.charset.StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.startsWith("data: ")) {
                                String data = line.substring(6);
                                emitter.send(data);
                                if ("[END]".equals(data)) break;
                            }
                        }
                        emitter.complete();
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                    }
                }
            });
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }

    // JSON 특수문자 이스케이프
    private String escapeJson(String s) {
    if (s == null) return "";
    return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ");
    }

    @Autowired
    private CivicDraftRepository civicDraftRepository;

    /**
     * 단계별 입력값을 누적하여 최종 민원 초안 및 법률정보 응답 생성
     */
    public Map<String, Object> processRequest(DraftRequest request, String publicVisibility) {
        // 1. Python 서버에서 채널 추천
        String channel = "";
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, String> payload = new HashMap<>();
            payload.put("summary", request.getSummary());
            payload.put("title", request.getTitle());
            String json = mapper.writeValueAsString(payload);
            okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
            okhttp3.RequestBody body = okhttp3.RequestBody.create(json, okhttp3.MediaType.parse("application/json; charset=utf-8"));
            okhttp3.Request httpRequest = new okhttp3.Request.Builder()
                    .url("http://localhost:8000/recommend_channel")
                    .post(body)
                    .addHeader("Accept", "application/json")
                    .build();
            okhttp3.Response response = client.newCall(httpRequest).execute();
            if (response.isSuccessful() && response.body() != null) {
                String resp = response.body().string();
                Map<String, Object> respMap = mapper.readValue(resp, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                channel = (String) respMap.getOrDefault("channel", "");
            }
        } catch (Exception ex) {
            channel = "";
        }

    // 2. Python 서버 SSE 초안 생성 엔드포인트 호출
    // CivicDraftService에서는 SSE 방식만 사용하도록 변경됨
    // 실제 본문 생성은 processRequestSse에서 처리됨
    String title = request.getTitle();
    String bodyText = request.getSummary();

        // 3. DB 저장 (공개일 때만)
        if ("public".equalsIgnoreCase(publicVisibility)) {
            CivicDraft entity = new CivicDraft();
            entity.setBody(bodyText);
            entity.setPublicVisibility(publicVisibility);
            civicDraftRepository.save(entity);
        }

        // 4. 결과 반환 (channel, title, body)
        Map<String, Object> result = new HashMap<>();
        result.put("channel", channel);
        result.put("title", title);
        result.put("body", bodyText);
        return result;
    }
}
