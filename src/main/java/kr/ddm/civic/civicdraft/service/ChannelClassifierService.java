package kr.ddm.civic.civicdraft.service;

import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import org.springframework.stereotype.Service;
import kr.ddm.civic.civicdraft.dto.*;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 채널 추천 서비스
 * 코드 내 채널 목록 기반으로 AI 추천 결과 반환
 */
@Schema(description = "채널 추천 서비스. 코드 내 채널 목록 기반으로 AI 추천 결과 반환.")
@Service
public class ChannelClassifierService {
    private static final Logger log = LoggerFactory.getLogger(ChannelClassifierService.class);

    // 채널 목록을 코드 내에서 직접 관리
    private static final List<Map<String, Object>> CHANNELS = Arrays.asList(
        new HashMap<String, Object>() {{ put("id", "safety_report"); put("title", "안전신문고"); }},
        new HashMap<String, Object>() {{ put("id", "mayor_board"); put("title", "구청장에게 바란다"); }},
        new HashMap<String, Object>() {{ put("id", "saeol"); put("title", "새올전자민원창구"); }}
    );

    public RecommendationResponse recommend(Issue issue) {
        try {
            // 1. 채널 목록을 직접 사용
            List<Map<String, Object>> channels = CHANNELS;
            if (channels.isEmpty()) throw new RuntimeException("channels가 비어있음");

            // 2. AI 서버에 민원 요약과 채널 목록 전달
            // (예시: OkHttp 등으로 Python 서버에 POST 요청)
            Map<String, Object> payload = new HashMap<>();
            payload.put("issue", issue);
            payload.put("channels", channels);

            // 실제 Python 서버 연동 코드 필요 (아래는 예시)
            RecommendationResponse aiResponse = callPythonAiServer(payload);
            if (aiResponse == null) throw new RuntimeException("AI 추천 결과 없음");
            return aiResponse;
        } catch (Exception e) {
            log.error("AI 기반 추천 recommend() 예외", e);
            throw new RuntimeException("AI 기반 추천 로직 예외", e);
        }
    }

    /**
     * Python AI 서버에 추천 요청 (실제 구현 필요)
     */
    private RecommendationResponse callPythonAiServer(Map<String, Object> payload) {
        try {
            OkHttpClient client = new OkHttpClient();
            ObjectMapper mapper = new ObjectMapper();
            String jsonPayload = mapper.writeValueAsString(payload);

            RequestBody body = RequestBody.create(jsonPayload, MediaType.parse("application/json"));
            Request request = new Request.Builder()
                .url("http://localhost:8000/api/recommend") // Python 서버 주소
                .post(body)
                .build();

            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            String responseJson = response.body().string();
            return mapper.readValue(responseJson, RecommendationResponse.class);
        } catch (Exception e) {
            log.error("Python AI 서버 연동 실패", e);
            return null;
        }
    }


}
