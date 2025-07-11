package com.gamified.application.exercise.service;

import com.azure.ai.inference.ChatCompletionsClient;
import com.azure.ai.inference.ChatCompletionsClientBuilder;
import com.azure.ai.inference.models.ChatCompletions;
import com.azure.ai.inference.models.ChatCompletionsOptions;
import com.azure.ai.inference.models.ChatRequestMessage;
import com.azure.ai.inference.models.ChatRequestSystemMessage;
import com.azure.ai.inference.models.ChatRequestUserMessage;
import com.azure.core.credential.AzureKeyCredential;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cliente para interactuar con Azure AI Foundry para generar ejercicios
 * Usa Azure AI Inference SDK para comunicarse con modelos como Phi-4-mini-instruct
 */
@Service
@Slf4j
public class AzureAiClient {

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private ChatCompletionsClient chatClient;

    @Value("${azure.ai.foundry.endpoint}")
    private String azureAiEndpoint;

    @Value("${azure.ai.foundry.api-key}")
    private String azureAiApiKey;

    @Value("${azure.ai.foundry.deployment-name}")
    private String deploymentName;

    @Value("${azure.ai.foundry.api-version:2024-05-01-preview}")
    private String apiVersion;

    @Value("${azure.ai.foundry.model-version:1}")
    private String modelVersion;

    @Value("${azure.ai.foundry.max-tokens:4000}")
    private int maxTokens;

    @Value("${azure.ai.foundry.temperature:1.2}")
    private double temperature;

    @Value("${azure.ai.foundry.top-p:0.95}")
    private double topP;

    @Value("${azure.ai.foundry.frequency-penalty:0.0}")
    private double frequencyPenalty;

    @Value("${azure.ai.foundry.presence-penalty:0.0}")
    private double presencePenalty;

    @Value("${azure.ai.foundry.timeout:30}")
    private int timeoutSeconds;

    public AzureAiClient(ObjectMapper objectMapper, RestTemplate restTemplate) {
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
    }

    /**
     * Inicializa el cliente de Azure AI Foundry
     */
    private ChatCompletionsClient getChatClient() {
        if (chatClient == null && isConfigured()) {
            log.info("Initializing Azure AI Foundry client with endpoint: {}", azureAiEndpoint);
            try {
                AzureKeyCredential credential = new AzureKeyCredential(azureAiApiKey);
                chatClient = new ChatCompletionsClientBuilder()
                        .credential(credential)
                        .endpoint(azureAiEndpoint)
                        .buildClient();
                log.info("Azure AI Foundry client initialized successfully");
            } catch (Exception e) {
                log.error("Failed to initialize Azure AI Foundry client", e);
                throw new RuntimeException("Error initializing Azure AI client: " + e.getMessage(), e);
            }
        }
        return chatClient;
    }

    /**
     * Genera contenido de ejercicio usando Azure AI Foundry
     * 
     * @param prompt El prompt estructurado para generar el ejercicio
     * @return JSON string con el contenido del ejercicio generado
     * @throws RuntimeException si la generación falla
     */
    public String generateExerciseContent(String prompt) {
        log.info("Generando ejercicio con Azure AI Foundry. Prompt length: {}", prompt.length());
        
        try {
            // ULTRA LIMPIEZA del prompt antes de enviar
            String cleanPrompt = ultraCleanPrompt(prompt);
            log.info("Prompt limpio length: {}", cleanPrompt.length());

            // Intentar con llamada REST directa PRIMERO (más confiable)
            String directResult = callAzureAiDirectly(cleanPrompt);
            if (directResult != null && !directResult.trim().isEmpty()) {
                log.info("Llamada REST directa exitosa");
                return directResult;
            }

            // Fallback: intentar con SDK
            ChatCompletionsClient client = getChatClient();
            if (client == null) {
                throw new RuntimeException("Azure AI client not configured");
            }
            
            // Mensajes optimizados para modelo de razonamiento
            List<ChatRequestMessage> chatMessages = Arrays.asList(
                new ChatRequestSystemMessage("Eres un experto profesor de matemáticas con un modelo de razonamiento avanzado. INCLUYE tu proceso de razonamiento completo en la respuesta, luego termina con el JSON solicitado. Piensa paso a paso, razona sobre el problema, y asegúrate de que la pregunta y respuesta sean coherentes. Usa español en todo el contenido educativo."),
                new ChatRequestUserMessage(cleanPrompt)
            );

            // Configurar opciones para modelo de razonamiento
            ChatCompletionsOptions options = new ChatCompletionsOptions(chatMessages);
            options.setModel(deploymentName);
            options.setMaxTokens(4000); // Más tokens para razonamiento completo
            options.setTemperature(1.2); // Mayor creatividad para variabilidad
            options.setTopP(0.95); // Más diversidad en respuestas

            ChatCompletions completions = client.complete(options);
            String exerciseContent = extractAndCleanContentFromResponse(completions);
            log.info("Ejercicio generado exitosamente con SDK. Content length: {}", exerciseContent.length());
            return exerciseContent;

        } catch (Exception e) {
            log.error("Error al comunicarse con Azure AI Foundry", e);
            
            // NO usar respaldo - FALLAR correctamente
            throw new RuntimeException("Fallo completo en Azure AI: " + e.getMessage(), e);
        }
    }

    /**
     * ULTRA LIMPIEZA del prompt para evitar problemas de parsing
     */
    private String ultraCleanPrompt(String prompt) {
        if (prompt == null) return "";
        
        return prompt
            // Remover caracteres de control individualmente
            .replaceAll("\\r", " ")
            .replaceAll("\\n", " ")
            .replaceAll("\\t", " ")
            .replaceAll("\\f", " ")
            .replaceAll("\\b", " ")
            // Remover caracteres especiales problemáticos
            .replaceAll("[\\u0000-\\u001F\\u007F-\\u009F]", " ")
            // Normalizar espacios
            .replaceAll("\\s+", " ")
            // Remover comillas que pueden romper JSON
            .replace("\"", "'")
            .replace("'", "")
            // Limpiar y trim
            .trim();
    }

    /**
     * Extrae y limpia el contenido del ejercicio de la respuesta de Azure AI Foundry
     */
    private String extractAndCleanContentFromResponse(ChatCompletions completions) {
        try {
            if (completions.getChoices() == null || completions.getChoices().isEmpty()) {
                throw new RuntimeException("Respuesta inesperada de Azure AI Foundry: no hay choices");
            }
            
            String content = completions.getChoices().get(0).getMessage().getContent();
            log.info("Contenido original recibido de Azure AI: '{}'", 
                    content.substring(0, Math.min(100, content.length())));
            
            // Limpieza y extracción de JSON
            content = cleanContent(content);
            content = extractJsonFromContent(content);
            
            // Validar JSON final
            validateJsonContent(content);
            
            return content;
            
        } catch (Exception e) {
            log.error("Error extracting content from Azure response", e);
            throw new RuntimeException("Error processing Azure response: " + e.getMessage());
        }
    }

    /**
     * Limpia y formatea el contenido para que sea compatible con Azure AI
     */
    private String cleanContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return content;
        }
        
        log.info("LIMPIANDO CONTENIDO ORIGINAL: '{}'", content);
        
        // PASO 1: Quitar comillas externas si está envuelto como string
        content = content.trim();
        if (content.startsWith("\"") && content.endsWith("\"")) {
            content = content.substring(1, content.length() - 1);
            log.info("COMILLAS EXTERNAS REMOVIDAS");
        }
        
        // PASO 2: Decodificar caracteres escapados Y convertir comillas simples a dobles
        content = content
                .replace("\\\"", "\"")   // \" -> "
                .replace("\\n", " ")     // \n -> espacio
                .replace("\\t", " ")     // \t -> espacio
                .replace("\\r", " ")     // \r -> espacio
                .replace("'", "\"");     // ' -> " (CRÍTICO para JSON válido)
                
        // PASO 3: Normalizar espacios múltiples (NO tocar nada más)
        content = content.replaceAll("  +", " ").trim();
        
        log.info("CONTENIDO FINAL LIMPIO: '{}'", content);
        return content;
    }

    /**
     * Extrae JSON del contenido - CON SOPORTE PARA RAZONAMIENTO Y PROPIEDADES
     */
    private String extractJsonFromContent(String content) {
        log.info("Extrayendo JSON del contenido (puede incluir razonamiento)...");
        
        content = content.trim();
        
        // Si ya empieza y termina con llaves, es JSON válido
        if (content.startsWith("{") && content.endsWith("}")) {
            log.info("JSON ya está en formato correcto");
            return content;
        }
        
        // Para modelos de razonamiento: buscar el ÚLTIMO JSON válido en la respuesta
        // (el modelo puede incluir razonamiento antes del JSON final)
        int lastStartJson = content.lastIndexOf("{\"question\"");
        if (lastStartJson == -1) {
            lastStartJson = content.lastIndexOf("{");
        }
        int lastEndJson = content.lastIndexOf("}");
        
        if (lastStartJson != -1 && lastEndJson != -1 && lastStartJson < lastEndJson) {
            String potentialJson = content.substring(lastStartJson, lastEndJson + 1);
            try {
                // Validar que es JSON válido antes de retornarlo
                objectMapper.readTree(potentialJson);
                log.info("JSON válido extraído de respuesta con razonamiento");
                return potentialJson;
            } catch (Exception e) {
                log.warn("JSON extraído no es válido, buscando alternativas...");
            }
        }
        
        // Buscar JSON tradicional (cualquier posición)
        int startJson = content.indexOf("{");
        int endJson = content.lastIndexOf("}");
        
        if (startJson != -1 && endJson != -1 && startJson < endJson) {
            String jsonCandidate = content.substring(startJson, endJson + 1);
            try {
                objectMapper.readTree(jsonCandidate);
                log.info("JSON válido extraído de contenido mixto");
                return jsonCandidate;
            } catch (Exception e) {
                log.warn("Primer JSON encontrado no es válido...");
            }
        }
        
        // Si no hay JSON, convertir formato de propiedades a JSON
        if (content.contains("question :") && content.contains("correct_answer :")) {
            log.info("Detectado formato de propiedades, convirtiendo a JSON...");
            return convertPropertiesToJson(content);
        }
        
        // Último intento: buscar patrones de JSON malformados y repararlos
        if (content.contains("\"question\"") && content.contains("\"correct_answer\"")) {
            log.info("Detectado JSON malformado, intentando reparar...");
            return repairMalformedJson(content);
        }
        
        // Si no se puede procesar, fallar
        log.error("NO SE PUEDE PROCESAR EL CONTENIDO: '{}'", content.substring(0, Math.min(200, content.length())));
        throw new RuntimeException("No se puede convertir el contenido a JSON válido");
    }
    
    /**
     * Convierte formato de propiedades a JSON válido
     */
    private String convertPropertiesToJson(String content) {
        try {
            log.info("CONVIRTIENDO PROPIEDADES A JSON...");
            
            // Extraer campos usando regex simple
            String question = extractField(content, "question");
            String correctAnswer = extractField(content, "correct_answer");
            String options = extractOptions(content);
            String explanation = extractField(content, "explanation");
            
            // Construir JSON válido
            String json = String.format(
                "{\"question\": \"%s\", \"correct_answer\": \"%s\", \"options\": %s, \"explanation\": \"%s\"}",
                escapeJson(question),
                escapeJson(correctAnswer), 
                options,
                escapeJson(explanation)
            );
            
            log.info("CONVERSIÓN EXITOSA: '{}'", json);
            return json;
            
        } catch (Exception e) {
            log.error("Error convirtiendo propiedades a JSON", e);
            throw new RuntimeException("Error en conversión de formato: " + e.getMessage());
        }
    }
    
    /**
     * Extrae un campo específico del formato de propiedades
     */
    private String extractField(String content, String fieldName) {
        String[] lines = content.split("\n");
        for (String line : lines) {
            if (line.trim().startsWith(fieldName + " :")) {
                return line.substring(line.indexOf(":") + 1).trim();
            }
        }
        return "Campo no encontrado";
    }
    
    /**
     * Extrae y formatea las opciones como array JSON
     */
    private String extractOptions(String content) {
        String[] lines = content.split("\n");
        for (String line : lines) {
            if (line.trim().startsWith("options :")) {
                String optionsStr = line.substring(line.indexOf(":") + 1).trim();
                // Convertir [ 1 / 5 ,  5 / 5 ,  1 / 2 ,  5 / 10 ] a JSON array
                optionsStr = optionsStr.replace("[", "").replace("]", "").trim();
                String[] options = optionsStr.split(",");
                StringBuilder jsonArray = new StringBuilder("[");
                for (int i = 0; i < options.length; i++) {
                    if (i > 0) jsonArray.append(", ");
                    jsonArray.append("\"").append(options[i].trim()).append("\"");
                }
                jsonArray.append("]");
                log.info("Opciones convertidas: {}", jsonArray.toString());
                return jsonArray.toString();
            }
        }
        return "[\"Opción no encontrada\"]";
    }
    
    /**
     * Escapa caracteres especiales para JSON
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\"", "\\\"").replace("\n", " ").replace("\r", " ");
    }

    /**
     * Intenta reparar JSON malformado extrayendo los campos principales
     */
    private String repairMalformedJson(String content) {
        try {
            log.info("Intentando reparar JSON malformado...");
            
            // Extraer campos usando patrones más flexibles
            String question = extractFieldWithPattern(content, "\"question\"\\s*:\\s*\"([^\"]+)\"");
            String correctAnswer = extractFieldWithPattern(content, "\"correct_answer\"\\s*:\\s*\"([^\"]+)\"");
            String explanation = extractFieldWithPattern(content, "\"explanation\"\\s*:\\s*\"([^\"]+)\"");
            
            // Buscar array de opciones
            String optionsPattern = "\"options\"\\s*:\\s*\\[([^\\]]+)\\]";
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(optionsPattern);
            java.util.regex.Matcher matcher = pattern.matcher(content);
            
            String options = "[\"Opción 1\", \"Opción 2\", \"Opción 3\", \"Opción 4\"]"; // Default
            if (matcher.find()) {
                String optionsContent = matcher.group(1);
                // Limpiar y formatear las opciones
                options = "[" + optionsContent.replaceAll("'", "\"") + "]";
            }
            
            // Construir JSON válido
            String repairedJson = String.format(
                "{\"question\": \"%s\", \"correct_answer\": \"%s\", \"options\": %s, \"explanation\": \"%s\"}",
                escapeJson(question),
                escapeJson(correctAnswer),
                options,
                escapeJson(explanation)
            );
            
            // Validar que el JSON reparado es válido
            objectMapper.readTree(repairedJson);
            log.info("JSON reparado exitosamente");
            return repairedJson;
            
        } catch (Exception e) {
            log.error("Error reparando JSON malformado", e);
            throw new RuntimeException("No se pudo reparar el JSON malformado: " + e.getMessage());
        }
    }

    /**
     * Extrae un campo usando patrón regex
     */
    private String extractFieldWithPattern(String content, String pattern) {
        try {
            java.util.regex.Pattern regexPattern = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher matcher = regexPattern.matcher(content);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception e) {
            log.warn("Error extrayendo campo con patrón: {}", pattern);
        }
        return "Campo no encontrado";
    }

    /**
     * Hace una llamada REST directa a Azure AI como método principal
     */
    private String callAzureAiDirectly(String cleanPrompt) {
        try {
            log.info("Intentando llamada REST directa a Azure AI");
            
            // Construir endpoint correcto para Azure AI Foundry
            String correctEndpoint = buildCorrectEndpoint();
            log.info("Usando endpoint: {}", correctEndpoint);
            
            // Construir payload simple y limpio
            Map<String, Object> payload = new HashMap<>();
            payload.put("messages", List.of(
                Map.of("role", "system", "content", "Eres un experto profesor de matemáticas con capacidades de razonamiento avanzado. PIENSA PASO A PASO: incluye tu proceso completo de razonamiento, luego termina con el JSON exacto solicitado. Asegúrate de que la pregunta tenga sentido, resuélvela correctamente, y verifica que tu respuesta sea coherente. Usa español en todo el contenido. Formato final: {\"question\": \"pregunta completa en español\", \"correct_answer\": \"respuesta correcta\", \"options\": [\"opcion correcta\", \"error común 1\", \"error común 2\", \"error común 3\"], \"explanation\": \"explicación paso a paso en español\"}"),
                Map.of("role", "user", "content", cleanPrompt)
            ));
            payload.put("max_tokens", 4000);
            payload.put("temperature", 1.2);
            payload.put("top_p", 0.95);
            
            // Headers correctos
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", azureAiApiKey); // Azure AI Foundry usa 'api-key', no 'Authorization'
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            
            // Hacer llamada
            ResponseEntity<String> response = restTemplate.postForEntity(correctEndpoint, request, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String rawResponse = response.getBody();
                log.debug("Respuesta REST exitosa recibida");
                
                return extractContentFromDirectResponse(rawResponse);
            } else {
                log.warn("Llamada REST falló con status: {}", response.getStatusCode());
                return null;
            }
            
        } catch (Exception e) {
            log.error("Error en llamada REST directa: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Construye el endpoint correcto para Azure AI Foundry
     */
    private String buildCorrectEndpoint() {
        // Corregir endpoint si es necesario
        String baseEndpoint = azureAiEndpoint;
        
        // Si el endpoint termina en /models, corregirlo
        if (baseEndpoint.endsWith("/models")) {
            baseEndpoint = baseEndpoint.replace("/models", "");
        }
        
        // Construir endpoint correcto para chat completions
        return baseEndpoint + "/openai/deployments/" + deploymentName + "/chat/completions?api-version=" + apiVersion;
    }

    /**
     * Extrae el contenido del mensaje de la respuesta REST directa
     */
    private String extractContentFromDirectResponse(String rawResponse) {
        try {
            log.info("RESPUESTA CRUDA DE AZURE AI: {}", rawResponse);
            
            // Parsear respuesta JSON
            var responseJson = objectMapper.readTree(rawResponse);
            log.info("JSON parseado exitosamente");
            
            var choices = responseJson.get("choices");
            log.info("Choices encontrados: {}", choices != null ? choices.size() : "null");
            
            if (choices != null && choices.isArray() && choices.size() > 0) {
                var message = choices.get(0).get("message");
                log.info("Mensaje encontrado: {}", message != null ? "SI" : "NO");
                
                if (message != null) {
                    var content = message.get("content");
                    log.info("Content encontrado: {}", content != null ? "SI" : "NO");
                    
                    if (content != null) {
                        String exerciseContent = content.asText();
                        log.info("CONTENIDO EXTRAÍDO DE AZURE AI: '{}'", exerciseContent);
                        
                        // Limpiar y validar
                        log.info("CONTENIDO ANTES DE LIMPIAR: '{}'", exerciseContent);
                        exerciseContent = cleanContent(exerciseContent);
                        log.info("CONTENIDO DESPUÉS DE LIMPIAR: '{}'", exerciseContent);
                        
                        exerciseContent = extractJsonFromContent(exerciseContent);
                        log.info("CONTENIDO DESPUÉS DE EXTRAER JSON: '{}'", exerciseContent);
                        validateJsonContent(exerciseContent);
                        
                        log.info("CONTENIDO FINAL VALIDADO: '{}'", exerciseContent);
                        return exerciseContent;
                    }
                }
            }
            
            throw new RuntimeException("No se pudo extraer contenido de la respuesta REST - estructura inesperada");
            
        } catch (Exception e) {
            log.error("Error extrayendo contenido de respuesta REST: {}", e.getMessage());
            throw new RuntimeException("Error procesando respuesta REST: " + e.getMessage());
        }
    }

    /**
     * Valida que el contenido sea JSON válido
     */
    private void validateJsonContent(String content) {
        try {
            objectMapper.readTree(content);
        } catch (Exception e) {
            log.error("JSON inválido después de limpieza: {}", content);
            throw new RuntimeException("Contenido no es JSON válido después de limpieza: " + e.getMessage());
        }
    }



    /**
     * Obtiene la versión del modelo configurado
     */
    public String getModelVersion() {
        return modelVersion;
    }

    /**
     * Valida que la configuración de Azure AI esté completa
     */
    public boolean isConfigured() {
        log.info("Checking Azure AI configuration:");
        log.info("  - Endpoint: {}", azureAiEndpoint != null ? (azureAiEndpoint.isEmpty() ? "[EMPTY]" : "[SET]") : "[NULL]");
        log.info("  - API Key: {}", azureAiApiKey != null ? (azureAiApiKey.isEmpty() ? "[EMPTY]" : "[SET]") : "[NULL]");
        log.info("  - Deployment: {}", deploymentName != null ? deploymentName : "[NULL]");
        log.info("  - API Version: {}", apiVersion != null ? apiVersion : "[NULL]");
        
        boolean configured = azureAiEndpoint != null && !azureAiEndpoint.trim().isEmpty() &&
                           azureAiApiKey != null && !azureAiApiKey.trim().isEmpty() &&
                           deploymentName != null && !deploymentName.trim().isEmpty();
        
        log.info("  - Is Configured: {}", configured);
        return configured;
    }

    /**
     * Verifica el estado de salud de Azure AI Foundry
     */
    public Map<String, Object> healthCheck() {
        log.info("Verificando estado de salud de Azure AI Foundry");
        
        try {
            if (!isConfigured()) {
                return Map.of(
                    "status", "ERROR",
                    "endpoint", azureAiEndpoint != null ? azureAiEndpoint : "",
                    "deployment", deploymentName != null ? deploymentName : "",
                    "error", "Azure AI client not configured",
                    "timestamp", System.currentTimeMillis()
                );
            }

            // Intenta hacer una llamada simple para verificar conectividad
            long startTime = System.currentTimeMillis();
            
            ChatCompletionsClient client = getChatClient();
            List<ChatRequestMessage> testMessages = Arrays.asList(
                new ChatRequestUserMessage("Hello")
            );

            ChatCompletionsOptions testOptions = new ChatCompletionsOptions(testMessages);
            testOptions.setModel(deploymentName);
            testOptions.setMaxTokens(10);
            testOptions.setTemperature(0.1);

            ChatCompletions testResponse = client.complete(testOptions);
            
            long endTime = System.currentTimeMillis();
            long responseTime = endTime - startTime;

            if (testResponse != null && testResponse.getChoices() != null && !testResponse.getChoices().isEmpty()) {
                return Map.of(
                    "status", "UP",
                    "endpoint", azureAiEndpoint,
                    "deployment", deploymentName,
                    "model_version", modelVersion,
                    "response_time_ms", responseTime,
                    "timestamp", System.currentTimeMillis()
                );
            } else {
                return Map.of(
                    "status", "DOWN",
                    "endpoint", azureAiEndpoint,
                    "deployment", deploymentName,
                    "error", "Empty response from Azure AI",
                    "response_time_ms", responseTime,
                    "timestamp", System.currentTimeMillis()
                );
            }

        } catch (Exception e) {
            log.error("Error verificando estado de Azure AI Foundry", e);
            return Map.of(
                "status", "ERROR",
                "endpoint", azureAiEndpoint != null ? azureAiEndpoint : "",
                "deployment", deploymentName != null ? deploymentName : "",
                "error", e.getMessage(),
                "timestamp", System.currentTimeMillis()
            );
        }
    }

    /**
     * Obtiene métricas de rendimiento y configuración de Azure AI Foundry
     */
    public Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("deployment_name", deploymentName);
        metrics.put("model_version", modelVersion);
        metrics.put("api_version", apiVersion);
        metrics.put("endpoint", azureAiEndpoint);
        metrics.put("max_tokens", maxTokens);
        metrics.put("temperature", temperature);
        metrics.put("top_p", topP);
        metrics.put("frequency_penalty", frequencyPenalty);
        metrics.put("presence_penalty", presencePenalty);
        metrics.put("timeout_seconds", timeoutSeconds);
        metrics.put("generation_enabled", isConfigured());
        metrics.put("fallback_enabled", false);
        metrics.put("configured", isConfigured());
        metrics.put("timestamp", System.currentTimeMillis());
        return metrics;
    }
}
