package com.fixit.api.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.ChatOptionsBuilder;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class GoogleGeminiChatModel implements ChatModel {

    private static final String GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${app.ai.gemini.api-key:chave-demonstracao-google-ai-studio}")
    private String apiKey;

    @Value("${app.ai.gemini.model:gemini-1.5-flash}")
    private String model;

    @Value("${app.ai.gemini.temperature:0.2}")
    private double temperature;

    public GoogleGeminiChatModel(ObjectMapper objectMapper) {
        this.restClient = RestClient.builder()
                .baseUrl(GEMINI_BASE_URL)
                .build();
        this.objectMapper = objectMapper;
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        String promptUnificado = prompt.getInstructions()
                .stream()
                .map(Message::getContent)
                .collect(Collectors.joining("\n\n"));

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of(
                                "role", "user",
                                "parts", List.of(Map.of("text", promptUnificado))
                        )
                ),
                "generationConfig", Map.of(
                        "temperature", temperature,
                        "responseMimeType", "application/json"
                )
        );

        String rawResponse = restClient.post()
                .uri("/{model}:generateContent?key={key}", model.trim(), apiKey.trim())
                .header("x-goog-api-key", apiKey.trim())
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(String.class);

        String generatedText = extrairTextoCandidato(rawResponse);
        Generation generation = new Generation(new AssistantMessage(generatedText));
        return new ChatResponse(List.of(generation));
    }

    @Override
    public ChatOptions getDefaultOptions() {
        return ChatOptionsBuilder.builder()
                .withModel(model)
                .withTemperature(temperature)
                .build();
    }

    private String extrairTextoCandidato(String rawResponse) {
        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            JsonNode textNode = root.path("candidates")
                    .path(0)
                    .path("content")
                    .path("parts")
                    .path(0)
                    .path("text");

            if (textNode.isMissingNode() || textNode.asText().isBlank()) {
                throw new IllegalStateException("Resposta vazia recebida do Google AI Studio (Gemini).");
            }
            return textNode.asText();
        } catch (Exception ex) {
            throw new IllegalStateException("Erro ao processar resposta do Google Gemini: " + ex.getMessage(), ex);
        }
    }

}

