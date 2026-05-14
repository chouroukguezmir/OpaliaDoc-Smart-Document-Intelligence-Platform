package com.example.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.Collections;
import java.util.Map;

@Slf4j
@Service
public class NlpService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${nlp.service.url:http://localhost:8000}")
    private String nlpServiceUrl;

    public Map<String, Object> analyzeDocument(File file) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(file));

            HttpEntity<MultiValueMap<String, Object>> requestEntity =
                    new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    nlpServiceUrl + "/analyze",
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful()
                    && response.getBody() != null) {
                log.info("NLP result: type={}, confidence={}",
                        response.getBody().get("document_type"),
                        response.getBody().get("confidence"));
                return response.getBody();
            }

        } catch (Exception e) {
            log.warn("Python NLP non disponible, fallback: {}", e.getMessage());
        }

        return Collections.emptyMap();
    }

    public boolean isAvailable() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    nlpServiceUrl + "/health", Map.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }
}