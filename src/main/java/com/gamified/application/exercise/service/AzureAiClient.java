package com.gamified.application.exercise.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Cliente para interactuar con Azure OpenAI para generar ejercicios
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AzureAiClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${azure.openai.endpoint}")
    private String azureOpenAiEndpoint;

    @Value("${azure.openai.api-key}")
    private String azureOpenAiApiKey;

    @Value("${azure.openai.deployment-name}")
    private String deploymentName;

    @Value("${azure.openai.api-version:2024-02-15-preview}")
    private String apiVersion;

    @Value("${azure.openai.model-version:gpt-4-turbo}")
    private String modelVersion;

    /**
     * Genera contenido de ejercicio usando Azure OpenAI
     * 
     * @param prompt El prompt estructurado para generar el ejercicio
     * @return JSON string con el contenido del ejercicio generado
     * @throws RuntimeException si la generación falla
     */
    public String generateExerciseContent(String prompt) {
        log.info("Generando ejercicio con Azure OpenAI. Prompt length: {}", prompt.length());
        
        try {
            // Construir la URL del endpoint
            String url = String.format("%s/openai/deployments/%s/chat/completions?api-version=%s",
                    azureOpenAiEndpoint, deploymentName, apiVersion);

            // Configurar headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", azureOpenAiApiKey);

            // Construir el payload
            Map<String, Object> requestBody = Map.of(
                "messages", new Object[] {
                    Map.of(
                        "role", "system",
                        "content", "Eres un experto en educación matemática que genera ejercicios educativos. " +
                                  "Responde ÚNICAMENTE con JSON válido, sin texto adicional."
                    ),
                    Map.of(
                        "role", "user", 
                        "content", prompt
                    )
                },
                "max_tokens", 1500,
                "temperature", 0.7,
                "top_p", 0.9,
                "frequency_penalty", 0.0,
                "presence_penalty", 0.0
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Realizar la llamada
            long startTime = System.currentTimeMillis();
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            long endTime = System.currentTimeMillis();

            log.info("Azure OpenAI response received in {} ms", endTime - startTime);

            if (response.getStatusCode() == HttpStatus.OK) {
                String responseBody = response.getBody();
                String exerciseContent = extractContentFromResponse(responseBody);
                
                log.info("Ejercicio generado exitosamente. Content length: {}", exerciseContent.length());
                return exerciseContent;
            } else {
                log.error("Error en Azure OpenAI: {} - {}", response.getStatusCode(), response.getBody());
                throw new RuntimeException("Error al generar ejercicio: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("Error al comunicarse con Azure OpenAI", e);
            throw new RuntimeException("Error al generar ejercicio con IA: " + e.getMessage(), e);
        }
    }

    /**
     * Extrae el contenido del ejercicio de la respuesta de Azure OpenAI
     */
    private String extractContentFromResponse(String responseBody) {
        try {
            JsonNode responseJson = objectMapper.readTree(responseBody);
            JsonNode choices = responseJson.path("choices");
            
            if (choices.isArray() && choices.size() > 0) {
                JsonNode firstChoice = choices.get(0);
                JsonNode message = firstChoice.path("message");
                String content = message.path("content").asText();
                
                // Validar que el contenido sea JSON válido
                objectMapper.readTree(content); // Esto lanzará excepción si no es JSON válido
                
                return content;
            } else {
                throw new RuntimeException("Respuesta inesperada de Azure OpenAI: no hay choices");
            }
            
        } catch (Exception e) {
            log.error("Error al parsear respuesta de Azure OpenAI: {}", responseBody, e);
            throw new RuntimeException("Error al procesar respuesta de IA: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene la versión del modelo configurado
     */
    public String getModelVersion() {
        return modelVersion;
    }

    /**
     * Valida que la configuración de Azure OpenAI esté completa
     */
    public boolean isConfigured() {
        return azureOpenAiEndpoint != null && !azureOpenAiEndpoint.trim().isEmpty() &&
               azureOpenAiApiKey != null && !azureOpenAiApiKey.trim().isEmpty() &&
               deploymentName != null && !deploymentName.trim().isEmpty();
    }
}
