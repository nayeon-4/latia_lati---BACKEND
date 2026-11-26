package pe.edu.upc.latia_lati.serviceimplements;

import tools.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import pe.edu.upc.latia_lati.dtos.ChatMessageDTO;
import pe.edu.upc.latia_lati.dtos.ChatRequestDTO;
import pe.edu.upc.latia_lati.dtos.ChatResponseDTO;
import pe.edu.upc.latia_lati.exceptions.ChatServiceException;
import pe.edu.upc.latia_lati.serviceinterfaces.IChatService;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatServiceImplement implements IChatService {
    private static final Logger logger = LoggerFactory.getLogger(ChatServiceImplement.class);
    private static final String SYSTEM_INSTRUCTION = """
            Eres Lati, un asistente informativo de salud que responde en español claro y empático.
            Ayuda a comprender información general, pero no diagnostiques, no indiques tratamientos
            ni reemplaces la atención de profesionales de salud. Anima a consultar a un profesional
            ante dudas clínicas. Si la persona describe una posible emergencia, recomienda contactar
            de inmediato los servicios de emergencia locales. No afirmes que has revisado documentos
            ni datos personales que no aparezcan en la conversación.
            """; //xdd

    private final RestClient restClient;

    public ChatServiceImplement() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(60));
        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    @Value("${gemini.api-key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.5-flash}")
    private String model;

    @Override
    public ChatResponseDTO generateReply(ChatRequestDTO request) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ChatServiceException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "El servicio de chat no está configurado en el servidor."
            );
        }

        JsonNode response;
        try {
            response = restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("generativelanguage.googleapis.com")
                            .path("/v1beta/models/{model}:generateContent")
                            .queryParam("key", apiKey)
                            .build(model))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(buildGeminiRequest(request))
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientResponseException exception) {
            logger.warn("Gemini respondió con estado HTTP {}", exception.getStatusCode().value());
            throw new ChatServiceException(
                    HttpStatus.BAD_GATEWAY,
                    "No se pudo obtener una respuesta de la IA."
            );
        } catch (ResourceAccessException exception) {
            logger.warn("No se pudo conectar con Gemini ({})", exception.getClass().getSimpleName());
            throw new ChatServiceException(
                    HttpStatus.BAD_GATEWAY,
                    "No se pudo conectar con el servicio de IA."
            );
        }

        String reply = extractReply(response);
        if (reply.isBlank()) {
            throw new ChatServiceException(
                    HttpStatus.BAD_GATEWAY,
                    "La IA no devolvió una respuesta. Inténtalo nuevamente."
            );
        }
        return new ChatResponseDTO(reply);
    }

    private Map<String, Object> buildGeminiRequest(ChatRequestDTO request) {
        List<Map<String, Object>> contents = new ArrayList<>();
        if (request.getHistory() != null) {
            for (ChatMessageDTO message : request.getHistory()) {
                contents.add(content(message.getRole(), message.getContent()));
            }
        }
        contents.add(content("user", request.getMessage()));

        Map<String, Object> payload = new HashMap<>();
        payload.put("systemInstruction", Map.of(
                "parts", List.of(Map.of("text", SYSTEM_INSTRUCTION))
        ));
        payload.put("contents", contents);
        payload.put("generationConfig", Map.of(
                "temperature", 0.4,
                "maxOutputTokens", 1024
        ));
        return payload;
    }

    private Map<String, Object> content(String role, String text) {
        return Map.of(
                "role", role,
                "parts", List.of(Map.of("text", text))
        );
    }

    private String extractReply(JsonNode response) {
        JsonNode parts = response == null
                ? null
                : response.path("candidates").path(0).path("content").path("parts");
        if (parts == null || !parts.isArray()) {
            return "";
        }

        StringBuilder reply = new StringBuilder();
        for (JsonNode part : parts) {
            JsonNode text = part.get("text");
            if (text != null && text.isTextual()) {
                if (!reply.isEmpty()) {
                    reply.append('\n');
                }
                reply.append(text.asText());
            }
        }
        return reply.toString().trim();
    }
}
