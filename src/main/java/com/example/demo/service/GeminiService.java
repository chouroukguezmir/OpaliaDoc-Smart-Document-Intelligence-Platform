package com.example.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.nio.file.Files;
import java.util.*;

/**
 * Analyse un document via l'API Gemini (multimodal) : l'image est envoyée
 * directement au modèle, qui renvoie le type de document et les champs
 * extraits en JSON.
 *
 * Le résultat est exposé avec les mêmes clés que le service Python NLP
 * (raw_text, document_type, is_handwritten, extracted_fields) afin de se
 * brancher sans friction dans {@link ScanServiceImpl}.
 */
@Slf4j
@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** Valeur placeholder présente dans application.properties tant qu'aucune clé réelle n'est fournie. */
    private static final String PLACEHOLDER_KEY = "TON_API_KEY_ICI";

    private static final String PROMPT = """
            Tu es un assistant d'extraction de documents administratifs pour Opalia Pharma.
            Analyse l'image fournie et renvoie UNIQUEMENT un objet JSON valide, sans texte autour.

            Détermine le type de document parmi :
            - "EMPLOYEE" : formulaire de compte utilisateur. En-tête type
              "User account activation/modification/removal form" ou
              "Demande de compte utilisateur" (code E DSI 3812).
            - "TYPE_A" : demande de droits d'accès informatique (code E DSI 3813).
            - "TYPE_B" : demande de matériels informatique (code E DSI 3328).
            - "TYPE_C" : demande d'utilisation de matériel informatique externe (code E DSI 3797).
            - "UNKNOWN" : si aucun ne correspond.

            Renvoie ce schéma exact :
            {
              "documentType": "EMPLOYEE|TYPE_A|TYPE_B|TYPE_C|UNKNOWN",
              "isEmployeeForm": true|false,
              "isHandwritten": true|false,
              "confidence": 0.0-1.0,
              "rawText": "tout le texte lisible du document",
              "fields": {
                "name": "",             // "User to be activated" — nom de l'utilisateur
                "company": "",          // Company
                "site": "",             // Site
                "department": "",       // Department
                "mobile": "",           // Mobile
                "officePhone": "",      // Office Phone
                "kindOfUpdate": "",     // case cochée : Activation/Modification/Removal/Suspension/Reactivation
                "requester": "",        // Requester (System Owner / HR responsible)
                "requesterJobRole": ""  // "Job Role" — celui du Requester (demandeur)
              }
            }

            Ces champs proviennent du formulaire EMPLOYEE (E DSI 3812). Pour les
            autres types, remplis ce que tu peux et laisse le reste vide.

            Le document est souvent REMPLI À LA MAIN : lis attentivement chaque
            case manuscrite, même peu lisible, et reporte la valeur exacte. Pour
            "kindOfUpdate", indique l'option dont la case est cochée.

            ATTENTION à ne pas confondre deux personnes :
            - "name" = "User to be activated" (l'utilisateur concerné).
            - "requester" = "Requester" (le demandeur) et "requesterJobRole" =
              le champ "Job Role", qui appartient au demandeur, PAS à l'utilisateur.

            Règles : laisse une chaîne vide pour un champ absent ou non rempli ;
            ne devine jamais ; n'invente aucune valeur ; le JSON final ne doit
            contenir aucun commentaire ; toutes les valeurs de "fields" doivent
            être des chaînes de caractères.
            """;

    /** Vrai si une clé API réelle a été fournie. */
    public boolean isConfigured() {
        return apiKey != null
                && !apiKey.isBlank()
                && !apiKey.equals(PLACEHOLDER_KEY);
    }

    /**
     * Envoie l'image au modèle et renvoie une Map compatible NLP :
     * raw_text, document_type, is_handwritten, extracted_fields.
     * Renvoie une Map vide en cas d'échec ou si Gemini n'est pas configuré.
     */
    public Map<String, Object> analyzeDocument(File file, String mimeType) {
        Map<String, Object> result = new HashMap<>();

        if (!isConfigured()) {
            log.warn("Gemini non configuré (clé API absente) — analyse ignorée");
            return result;
        }

        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            String base64 = Base64.getEncoder().encodeToString(bytes);
            String mt = (mimeType != null && !mimeType.isBlank()) ? mimeType : "image/jpeg";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request =
                    new HttpEntity<>(buildRequestBody(base64, mt), headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    apiUrl + "?key=" + apiKey, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return parseResponse(response.getBody());
            }
            log.error("Gemini a répondu : {}", response.getStatusCode());

        } catch (Exception e) {
            log.error("Erreur appel Gemini : {}", e.getMessage(), e);
        }
        return result;
    }

    // ── Construction du corps de requête Gemini generateContent ──
    private Map<String, Object> buildRequestBody(String base64, String mimeType) {
        Map<String, Object> textPart = Map.of("text", PROMPT);
        Map<String, Object> imagePart = Map.of("inline_data",
                Map.of("mime_type", mimeType, "data", base64));

        Map<String, Object> content = Map.of("parts", List.of(textPart, imagePart));

        Map<String, Object> body = new HashMap<>();
        body.put("contents", List.of(content));
        // Force une réponse JSON pure (pas de balises markdown)
        body.put("generationConfig", Map.of("responseMimeType", "application/json"));
        return body;
    }

    // ── Extraction et conversion de la réponse Gemini ──
    private Map<String, Object> parseResponse(String responseBody) throws Exception {
        Map<String, Object> result = new HashMap<>();

        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode textNode = root.path("candidates").path(0)
                .path("content").path("parts").path(0).path("text");

        if (textNode.isMissingNode() || textNode.asText().isBlank()) {
            log.warn("Réponse Gemini vide ou inattendue");
            return result;
        }

        // Le texte renvoyé est lui-même du JSON (responseMimeType=application/json)
        String json = textNode.asText().trim()
                .replaceAll("^```(?:json)?", "")
                .replaceAll("```$", "")
                .trim();
        JsonNode doc = objectMapper.readTree(json);

        result.put("document_type", doc.path("documentType").asText("UNKNOWN"));
        result.put("is_handwritten", doc.path("isHandwritten").asBoolean(false));
        result.put("raw_text", doc.path("rawText").asText(""));

        Map<String, String> fields = new LinkedHashMap<>();
        JsonNode fieldsNode = doc.path("fields");
        if (fieldsNode.isObject()) {
            fieldsNode.fields().forEachRemaining(e ->
                    fields.put(e.getKey(), e.getValue().asText("")));
        }
        result.put("extracted_fields", fields);

        log.info("Gemini : type={} | champs extraits={}",
                result.get("document_type"), fields.size());
        return result;
    }
}