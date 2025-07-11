package com.gamified.application.ai.service;

import com.gamified.application.exercise.service.AzureAiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Servicio de integración con IA para mejorar y validar contenido
 * Fase 4: Módulo de Integración IA
 */
@Service
public class AiIntegrationService {

    private static final Logger logger = LoggerFactory.getLogger(AiIntegrationService.class);

    @Autowired
    private AzureAiClient azureAiClient;

    // Patrones para validación de contenido
    private static final Pattern MATHEMATICAL_EXPRESSION = Pattern.compile(".*[0-9+\\-*/=()].*");
    private static final Pattern CODE_SNIPPET = Pattern.compile(".*(public|private|class|function|def|var|let|const).*");
    private static final Pattern FORMULA_PATTERN = Pattern.compile(".*[a-zA-Z]\\s*=\\s*.*");

    /**
     * Mejora un ejercicio generado usando IA
     */
    public Map<String, Object> enhanceGeneratedExercise(Map<String, Object> originalExercise) {
        logger.info("Mejorando ejercicio generado: {}", originalExercise.get("title"));
        
        try {
            Map<String, Object> enhancedExercise = new HashMap<>(originalExercise);
            
            // Analizar el tipo de contenido
            ContentType contentType = analyzeContentType((String) originalExercise.get("content"));
            
            // Mejorar según el tipo de contenido
            switch (contentType) {
                case MATHEMATICAL -> enhanceMathematicalExercise(enhancedExercise);
                case PROGRAMMING -> enhanceProgrammingExercise(enhancedExercise);
                case CONCEPTUAL -> enhanceConceptualExercise(enhancedExercise);
                default -> enhanceGenericExercise(enhancedExercise);
            }
            
            // Validar y ajustar dificultad
            validateAndAdjustDifficulty(enhancedExercise);
            
            // Generar hints inteligentes
            generateIntelligentHints(enhancedExercise, contentType);
            
            // Mejorar explicaciones
            enhanceExplanations(enhancedExercise, contentType);
            
            logger.info("Ejercicio mejorado exitosamente: {}", enhancedExercise.get("title"));
            return enhancedExercise;
            
        } catch (Exception e) {
            logger.error("Error mejorando ejercicio: {}", e.getMessage());
            return originalExercise; // Retornar original en caso de error
        }
    }

    /**
     * Analiza el tipo de contenido del ejercicio
     */
    private ContentType analyzeContentType(String content) {
        if (content == null) return ContentType.GENERIC;
        
        String lowerContent = content.toLowerCase();
        
        if (MATHEMATICAL_EXPRESSION.matcher(content).matches() || 
            lowerContent.contains("calcular") || lowerContent.contains("resolver") ||
            lowerContent.contains("ecuación") || FORMULA_PATTERN.matcher(content).matches()) {
            return ContentType.MATHEMATICAL;
        }
        
        if (CODE_SNIPPET.matcher(content).matches() || 
            lowerContent.contains("código") || lowerContent.contains("programa") ||
            lowerContent.contains("algoritmo") || lowerContent.contains("función")) {
            return ContentType.PROGRAMMING;
        }
        
        if (lowerContent.contains("concepto") || lowerContent.contains("definir") ||
            lowerContent.contains("explicar") || lowerContent.contains("teoría")) {
            return ContentType.CONCEPTUAL;
        }
        
        return ContentType.GENERIC;
    }

    /**
     * Mejora ejercicios matemáticos
     */
    private void enhanceMathematicalExercise(Map<String, Object> exercise) {
        logger.debug("Mejorando ejercicio matemático");
        
        // Agregar validaciones matemáticas
        exercise.put("requires_numeric_answer", true);
        exercise.put("allow_decimal", true);
        exercise.put("math_validation", true);
        
        // Mejorar formato matemático
        String content = (String) exercise.get("content");
        if (content != null) {
            // Formatear expresiones matemáticas
            content = formatMathematicalExpressions(content);
            exercise.put("content", content);
        }
        
        // Agregar pasos de solución sugeridos
        List<String> solutionSteps = generateMathSolutionSteps(content);
        exercise.put("solution_steps", solutionSteps);
    }

    /**
     * Mejora ejercicios de programación
     */
    private void enhanceProgrammingExercise(Map<String, Object> exercise) {
        logger.debug("Mejorando ejercicio de programación");
        
        // Agregar configuración de código
        exercise.put("requires_code_answer", true);
        exercise.put("syntax_highlighting", true);
        exercise.put("auto_indentation", true);
        
        // Detectar lenguaje de programación
        String content = (String) exercise.get("content");
        String detectedLanguage = detectProgrammingLanguage(content);
        exercise.put("programming_language", detectedLanguage);
        
        // Agregar casos de prueba básicos
        List<Map<String, Object>> testCases = generateBasicTestCases(content);
        exercise.put("test_cases", testCases);
    }

    /**
     * Mejora ejercicios conceptuales
     */
    private void enhanceConceptualExercise(Map<String, Object> exercise) {
        logger.debug("Mejorando ejercicio conceptual");
        
        // Configurar para respuestas de texto
        exercise.put("requires_text_answer", true);
        exercise.put("min_word_count", 10);
        exercise.put("keyword_validation", true);
        
        // Extraer conceptos clave
        String content = (String) exercise.get("content");
        List<String> keyWords = extractKeywords(content);
        exercise.put("key_concepts", keyWords);
        
        // Generar preguntas de seguimiento
        List<String> followUpQuestions = generateFollowUpQuestions(content);
        exercise.put("follow_up_questions", followUpQuestions);
    }

    /**
     * Mejora ejercicios genéricos
     */
    private void enhanceGenericExercise(Map<String, Object> exercise) {
        logger.debug("Mejorando ejercicio genérico");
        
        // Configuración básica mejorada
        exercise.put("enhanced", true);
        exercise.put("ai_processed", true);
        
        // Mejorar claridad del enunciado
        String content = (String) exercise.get("content");
        if (content != null) {
            content = improveClarityAndGrammar(content);
            exercise.put("content", content);
        }
    }

    /**
     * Valida y ajusta la dificultad del ejercicio
     */
    private void validateAndAdjustDifficulty(Map<String, Object> exercise) {
        String content = (String) exercise.get("content");
        String currentDifficulty = (String) exercise.get("difficulty_level");
        
        // Análisis de complejidad
        int complexityScore = calculateComplexityScore(content);
        String suggestedDifficulty = mapComplexityToDifficulty(complexityScore);
        
        // Ajustar si hay discrepancia significativa
        if (!suggestedDifficulty.equals(currentDifficulty)) {
            logger.info("Ajustando dificultad de {} a {} basado en análisis de complejidad", 
                       currentDifficulty, suggestedDifficulty);
            exercise.put("difficulty_level", suggestedDifficulty);
            exercise.put("difficulty_adjusted_by_ai", true);
        }
    }

    /**
     * Genera hints inteligentes basados en el contenido
     */
    private void generateIntelligentHints(Map<String, Object> exercise, ContentType contentType) {
        List<String> hints = new ArrayList<>();
        String content = (String) exercise.get("content");
        
        switch (contentType) {
            case MATHEMATICAL -> {
                hints.add("Identifica qué operación matemática necesitas realizar");
                hints.add("Revisa el orden de las operaciones (PEMDAS)");
                hints.add("Verifica tus cálculos paso a paso");
            }
            case PROGRAMMING -> {
                hints.add("Piensa en los pasos del algoritmo antes de escribir código");
                hints.add("Considera los casos especiales y validaciones");
                hints.add("Revisa la sintaxis del lenguaje");
            }
            case CONCEPTUAL -> {
                hints.add("Define los conceptos clave en tus propias palabras");
                hints.add("Busca ejemplos que ilustren el concepto");
                hints.add("Conecta con conocimientos previos");
            }
            default -> {
                hints.add("Lee cuidadosamente todo el enunciado");
                hints.add("Identifica la información clave");
                hints.add("Organiza tu respuesta de manera clara");
            }
        }
        
        exercise.put("intelligent_hints", hints);
    }

    /**
     * Mejora las explicaciones del ejercicio
     */
    private void enhanceExplanations(Map<String, Object> exercise, ContentType contentType) {
        String content = (String) exercise.get("content");
        
        // Generar explicación mejorada
        Map<String, Object> enhancedExplanation = new HashMap<>();
        enhancedExplanation.put("step_by_step", true);
        enhancedExplanation.put("visual_aids_recommended", shouldUseVisualAids(contentType));
        enhancedExplanation.put("difficulty_level", exercise.get("difficulty_level"));
        
        // Agregar recursos adicionales recomendados
        List<String> recommendedResources = generateRecommendedResources(contentType, content);
        enhancedExplanation.put("recommended_resources", recommendedResources);
        
        exercise.put("enhanced_explanation", enhancedExplanation);
    }

    /**
     * Calcula un score de complejidad para el contenido
     */
    private int calculateComplexityScore(String content) {
        if (content == null) return 1;
        
        int score = 0;
        
        // Longitud del texto
        score += Math.min(content.length() / 50, 3);
        
        // Presencia de elementos complejos
        if (MATHEMATICAL_EXPRESSION.matcher(content).matches()) score += 2;
        if (CODE_SNIPPET.matcher(content).matches()) score += 3;
        if (content.toLowerCase().contains("analizar") || content.toLowerCase().contains("evaluar")) score += 2;
        
        // Número de conceptos (palabras clave técnicas)
        String[] technicalWords = {"algoritmo", "función", "variable", "clase", "objeto", "método", "ecuación", "fórmula"};
        for (String word : technicalWords) {
            if (content.toLowerCase().contains(word)) score += 1;
        }
        
        return Math.max(1, Math.min(score, 10)); // Entre 1 y 10
    }

    /**
     * Mapea score de complejidad a nivel de dificultad
     */
    private String mapComplexityToDifficulty(int complexityScore) {
        return switch (complexityScore) {
            case 1, 2 -> "EASY";
            case 3, 4, 5 -> "MEDIUM";
            case 6, 7, 8 -> "HARD";
            default -> "EXPERT";
        };
    }

    /**
     * Formatea expresiones matemáticas
     */
    private String formatMathematicalExpressions(String content) {
        // Básico: agregar espacios alrededor de operadores
        content = content.replaceAll("([+\\-*/=])", " $1 ");
        content = content.replaceAll("\\s+", " "); // Eliminar espacios múltiples
        return content.trim();
    }

    /**
     * Genera pasos de solución para problemas matemáticos
     */
    private List<String> generateMathSolutionSteps(String content) {
        List<String> steps = new ArrayList<>();
        steps.add("1. Identificar los datos del problema");
        steps.add("2. Determinar qué se está pidiendo");
        steps.add("3. Seleccionar la operación o fórmula apropiada");
        steps.add("4. Realizar los cálculos paso a paso");
        steps.add("5. Verificar el resultado");
        return steps;
    }

    /**
     * Detecta el lenguaje de programación en el contenido
     */
    private String detectProgrammingLanguage(String content) {
        if (content == null) return "pseudocode";
        
        String lowerContent = content.toLowerCase();
        
        if (lowerContent.contains("public class") || lowerContent.contains("system.out")) return "java";
        if (lowerContent.contains("def ") || lowerContent.contains("print(")) return "python";
        if (lowerContent.contains("function ") || lowerContent.contains("console.log")) return "javascript";
        if (lowerContent.contains("#include") || lowerContent.contains("cout")) return "cpp";
        
        return "pseudocode";
    }

    /**
     * Genera casos de prueba básicos para ejercicios de programación
     */
    private List<Map<String, Object>> generateBasicTestCases(String content) {
        List<Map<String, Object>> testCases = new ArrayList<>();
        
        // Caso básico
        Map<String, Object> basicCase = new HashMap<>();
        basicCase.put("description", "Caso básico");
        basicCase.put("input", "valores_normales");
        basicCase.put("expected_behavior", "funcionamiento_esperado");
        testCases.add(basicCase);
        
        // Caso límite
        Map<String, Object> edgeCase = new HashMap<>();
        edgeCase.put("description", "Caso límite");
        edgeCase.put("input", "valores_extremos");
        edgeCase.put("expected_behavior", "manejo_apropiado");
        testCases.add(edgeCase);
        
        return testCases;
    }

    /**
     * Extrae palabras clave del contenido
     */
    private List<String> extractKeywords(String content) {
        List<String> keywords = new ArrayList<>();
        
        if (content == null) return keywords;
        
        // Lista básica de palabras técnicas comunes
        String[] technicalTerms = {
            "algoritmo", "estructura", "función", "variable", "clase", "objeto",
            "método", "proceso", "sistema", "modelo", "teoría", "concepto"
        };
        
        String lowerContent = content.toLowerCase();
        for (String term : technicalTerms) {
            if (lowerContent.contains(term)) {
                keywords.add(term);
            }
        }
        
        return keywords;
    }

    /**
     * Genera preguntas de seguimiento
     */
    private List<String> generateFollowUpQuestions(String content) {
        List<String> questions = new ArrayList<>();
        questions.add("¿Puedes dar un ejemplo práctico de este concepto?");
        questions.add("¿Cómo se relaciona esto con lo que ya sabes?");
        questions.add("¿Qué pasaría si cambiáramos algún parámetro?");
        questions.add("¿Cuáles serían las aplicaciones reales?");
        return questions;
    }

    /**
     * Mejora la claridad y gramática del texto
     */
    private String improveClarityAndGrammar(String content) {
        if (content == null) return "";
        
        // Mejoras básicas
        content = content.trim();
        content = content.replaceAll("\\s+", " "); // Espacios múltiples
        
        // Asegurar punto final
        if (!content.endsWith(".") && !content.endsWith("?") && !content.endsWith("!")) {
            content += ".";
        }
        
        return content;
    }

    /**
     * Determina si se deberían usar ayudas visuales
     */
    private boolean shouldUseVisualAids(ContentType contentType) {
        return contentType == ContentType.MATHEMATICAL || contentType == ContentType.PROGRAMMING;
    }

    /**
     * Genera recursos recomendados
     */
    private List<String> generateRecommendedResources(ContentType contentType, String content) {
        List<String> resources = new ArrayList<>();
        
        switch (contentType) {
            case MATHEMATICAL -> {
                resources.add("Calculadora paso a paso");
                resources.add("Gráficos interactivos");
                resources.add("Ejemplos resueltos similares");
            }
            case PROGRAMMING -> {
                resources.add("Editor de código con syntax highlighting");
                resources.add("Debugger paso a paso");
                resources.add("Documentación de la API");
            }
            case CONCEPTUAL -> {
                resources.add("Diagramas conceptuales");
                resources.add("Videos explicativos");
                resources.add("Lecturas complementarias");
            }
            default -> {
                resources.add("Recursos de estudio general");
                resources.add("Ejercicios similares");
            }
        }
        
        return resources;
    }

    /**
     * Valida la calidad de un ejercicio generado
     */
    public QualityAssessment validateExerciseQuality(Map<String, Object> exercise) {
        logger.info("Validando calidad del ejercicio: {}", exercise.get("title"));
        
        QualityAssessment assessment = new QualityAssessment();
        
        // Validar completitud
        assessment.setCompleteness(validateCompleteness(exercise));
        
        // Validar claridad
        assessment.setClarity(validateClarity(exercise));
        
        // Validar adecuación pedagógica
        assessment.setPedagogicalSuitability(validatePedagogicalSuitability(exercise));
        
        // Calcular score general
        double overallScore = (assessment.getCompleteness() + assessment.getClarity() + 
                              assessment.getPedagogicalSuitability()) / 3.0;
        assessment.setOverallScore(overallScore);
        
        // Determinar si es aceptable
        assessment.setAcceptable(overallScore >= 0.7);
        
        // Generar recomendaciones de mejora
        assessment.setImprovementSuggestions(generateImprovementSuggestions(assessment, exercise));
        
        logger.info("Validación completada - Score: {:.2f}, Aceptable: {}", 
                   overallScore, assessment.isAcceptable());
        
        return assessment;
    }

    private double validateCompleteness(Map<String, Object> exercise) {
        double score = 0.0;
        int criteria = 0;
        
        // Verificar campos esenciales
        if (exercise.get("title") != null && !exercise.get("title").toString().trim().isEmpty()) {
            score += 1.0; criteria++;
        }
        if (exercise.get("content") != null && !exercise.get("content").toString().trim().isEmpty()) {
            score += 1.0; criteria++;
        }
        if (exercise.get("difficulty_level") != null) {
            score += 1.0; criteria++;
        }
        if (exercise.get("estimated_duration") != null) {
            score += 1.0; criteria++;
        }
        
        return criteria > 0 ? score / criteria : 0.0;
    }

    private double validateClarity(Map<String, Object> exercise) {
        String content = (String) exercise.get("content");
        if (content == null || content.trim().isEmpty()) return 0.0;
        
        double score = 0.0;
        
        // Verificar longitud adecuada
        if (content.length() >= 20 && content.length() <= 500) score += 0.3;
        
        // Verificar estructura de oración
        if (content.contains(".") || content.contains("?")) score += 0.3;
        
        // Verificar ausencia de texto muy técnico sin explicación
        if (!content.matches(".*[{}\\[\\]<>].*")) score += 0.2;
        
        // Verificar claridad del objetivo
        if (content.toLowerCase().contains("calcular") || content.toLowerCase().contains("determinar") ||
            content.toLowerCase().contains("encontrar") || content.toLowerCase().contains("resolver")) {
            score += 0.2;
        }
        
        return Math.min(score, 1.0);
    }

    private double validatePedagogicalSuitability(Map<String, Object> exercise) {
        double score = 0.5; // Score base
        
        // Verificar si tiene hints o ayudas
        if (exercise.get("intelligent_hints") != null) score += 0.2;
        
        // Verificar si tiene explicaciones mejoradas
        if (exercise.get("enhanced_explanation") != null) score += 0.2;
        
        // Verificar adecuación de dificultad
        if (exercise.get("difficulty_adjusted_by_ai") != null) score += 0.1;
        
        return Math.min(score, 1.0);
    }

    private List<String> generateImprovementSuggestions(QualityAssessment assessment, Map<String, Object> exercise) {
        List<String> suggestions = new ArrayList<>();
        
        if (assessment.getCompleteness() < 0.8) {
            suggestions.add("Completar campos faltantes (título, contenido, dificultad, duración)");
        }
        
        if (assessment.getClarity() < 0.7) {
            suggestions.add("Mejorar claridad del enunciado - usar lenguaje más directo");
        }
        
        if (assessment.getPedagogicalSuitability() < 0.7) {
            suggestions.add("Agregar ayudas pedagógicas como hints o explicaciones paso a paso");
        }
        
        if (suggestions.isEmpty()) {
            suggestions.add("El ejercicio tiene buena calidad general");
        }
        
        return suggestions;
    }

    // Enums y clases auxiliares
    
    public enum ContentType {
        MATHEMATICAL, PROGRAMMING, CONCEPTUAL, GENERIC
    }
    
    public static class QualityAssessment {
        private double completeness;
        private double clarity;
        private double pedagogicalSuitability;
        private double overallScore;
        private boolean acceptable;
        private List<String> improvementSuggestions = new ArrayList<>();
        
        // Getters and setters
        public double getCompleteness() { return completeness; }
        public void setCompleteness(double completeness) { this.completeness = completeness; }
        
        public double getClarity() { return clarity; }
        public void setClarity(double clarity) { this.clarity = clarity; }
        
        public double getPedagogicalSuitability() { return pedagogicalSuitability; }
        public void setPedagogicalSuitability(double pedagogicalSuitability) { this.pedagogicalSuitability = pedagogicalSuitability; }
        
        public double getOverallScore() { return overallScore; }
        public void setOverallScore(double overallScore) { this.overallScore = overallScore; }
        
        public boolean isAcceptable() { return acceptable; }
        public void setAcceptable(boolean acceptable) { this.acceptable = acceptable; }
        
        public List<String> getImprovementSuggestions() { return improvementSuggestions; }
        public void setImprovementSuggestions(List<String> improvementSuggestions) { this.improvementSuggestions = improvementSuggestions; }
    }
} 