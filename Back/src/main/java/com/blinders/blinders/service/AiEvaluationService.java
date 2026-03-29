package com.blinders.blinders.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiEvaluationService {

    private final RestTemplate restTemplate;

    @Value("${ai.api.url:http://localhost:8000}")
    private String aiApiUrl;

    @Value("${ai.api.key:sk-english-test-secret-12345}")
    private String aiApiKey;

    public byte[] synthesizeSpeech(String text) {
        String url = aiApiUrl + "/api/ai/synthesize";
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-Key", aiApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("text", text);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<byte[]> response = restTemplate.exchange(
                url, HttpMethod.POST, request, byte[].class
        );

        return response.getBody();
    }

    public AiEvaluationResult evaluateVoice(MultipartFile audioFile, String correctAnswer) throws IOException {
        String url = aiApiUrl + "/api/ai/evaluate-voice";
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-Key", aiApiKey);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("audio", new ByteArrayResource(audioFile.getBytes()) {
            @Override
            public String getFilename() {
                return audioFile.getOriginalFilename() != null ? audioFile.getOriginalFilename() : "audio.wav";
            }
        });
        body.add("correct_answer", correctAnswer);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            AiEvaluationResult result = new AiEvaluationResult();
            result.setTranscribedText(root.path("transcribed_text").asText());
            result.setWasCorrect(root.path("was_correct").asBoolean());
            result.setCorrectAnswerWas(root.path("correct_answer_was").asText());
            return result;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse AI Engine response", e);
        }
    }

    public static class AiEvaluationResult {
        private String transcribedText;
        private boolean wasCorrect;
        private String correctAnswerWas;

        public String getTranscribedText() { return transcribedText; }
        public void setTranscribedText(String transcribedText) { this.transcribedText = transcribedText; }
        public boolean isWasCorrect() { return wasCorrect; }
        public void setWasCorrect(boolean wasCorrect) { this.wasCorrect = wasCorrect; }
        public String getCorrectAnswerWas() { return correctAnswerWas; }
        public void setCorrectAnswerWas(String correctAnswerWas) { this.correctAnswerWas = correctAnswerWas; }
    }
}
