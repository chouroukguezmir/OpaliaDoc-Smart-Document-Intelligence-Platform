package com.example.demo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class NlpService {

    @Value("${nlp.service.url:http://localhost:8000}")
    private String nlpUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ── Vérifie si le service Python est actif ────────────────
    public boolean isAvailable() {
        try {
            ResponseEntity<String> response =
                    restTemplate.getForEntity(nlpUrl + "/health", String.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            log.warn("Python NLP service non disponible : {}", e.getMessage());
            return false;
        }
    }

    // ── Analyse complète d'un document ────────────────────────
    @SuppressWarnings("unchecked")
    public Map<String, Object> analyzeDocument(File file) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(file));

            HttpEntity<MultiValueMap<String, Object>> request =
                    new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    nlpUrl + "/analyze", request, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return objectMapper.readValue(response.getBody(), Map.class);
            }
        } catch (Exception e) {
            log.error("Erreur appel NLP /analyze : {}", e.getMessage());
        }
        return new HashMap<>();
    }

    // ── OCR seul ─────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    public Map<String, Object> extractText(File file) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(file));

            HttpEntity<MultiValueMap<String, Object>> request =
                    new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    nlpUrl + "/ocr", request, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return objectMapper.readValue(response.getBody(), Map.class);
            }
        } catch (Exception e) {
            log.error("Erreur appel NLP /ocr : {}", e.getMessage());
        }
        return new HashMap<>();
    }

    // ── Digitisation d'écriture manuscrite ───────────────────
    @SuppressWarnings("unchecked")
    public Map<String, Object> digitiseHandwriting(File file) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(file));

            HttpEntity<MultiValueMap<String, Object>> request =
                    new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    nlpUrl + "/handwriting/digitise", request, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return objectMapper.readValue(response.getBody(), Map.class);
            }
        } catch (Exception e) {
            log.error("Erreur appel NLP /handwriting/digitise : {}", e.getMessage());
        }
        return new HashMap<>();
    }
}