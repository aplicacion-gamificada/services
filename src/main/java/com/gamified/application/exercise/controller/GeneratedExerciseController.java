package com.gamified.application.exercise.controller;

import com.gamified.application.exercise.model.dto.response.GeneratedExerciseResponseDto;
import com.gamified.application.exercise.service.GeneratedExerciseService;
import com.gamified.application.exercise.service.ExercisePoolService;
import com.gamified.application.shared.model.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Controlador REST para ejercicios generados por IA
 */
@RestController
@RequestMapping("/api/v1/generated-exercises")
@RequiredArgsConstructor
@Slf4j
public class GeneratedExerciseController {

    private final GeneratedExerciseService generatedExerciseService;
    private final ExercisePoolService exercisePoolService;

    /**
     * Obtiene el siguiente ejercicio generado por IA para un estudiante
     * 
     * @param studentId ID del estudiante
     * @param learningPointId ID del learning point
     * @param difficulty Dificultad preferida (opcional)
     * @return Ejercicio generado listo para presentar
     */
    @GetMapping("/next")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER')")
    public ResponseEntity<ApiResponse> getNextExercise(
            @RequestParam @NotNull @Positive Integer studentId,
            @RequestParam @NotNull @Positive Integer learningPointId,
            @RequestParam(required = false) String difficulty) {
        
        log.info("Obteniendo siguiente ejercicio generado para estudiante {} en learning point {}", 
                studentId, learningPointId);

        try {
            GeneratedExerciseResponseDto.GeneratedExerciseDto exercise = 
                    generatedExerciseService.getNextExercise(studentId, learningPointId, difficulty);

            return ResponseEntity.ok(new ApiResponse(true, "Ejercicio generado exitosamente", 
                    java.time.LocalDateTime.now(), exercise));

        } catch (Exception e) {
            log.error("Error obteniendo ejercicio generado para estudiante {}", studentId, e);
            return ResponseEntity.badRequest().body(
                    new ApiResponse(false, "Error al generar ejercicio: " + e.getMessage(), 
                    java.time.LocalDateTime.now()));
        }
    }

    /**
     * Obtiene estadísticas del pool de ejercicios
     */
    @GetMapping("/pool/stats")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getPoolStats() {
        
        log.info("Obteniendo estadísticas del pool de ejercicios");

        try {
            GeneratedExerciseResponseDto.GeneratedExerciseStatsDto stats = 
                    generatedExerciseService.getPoolStats();

            return ResponseEntity.ok(new ApiResponse(true, "Estadísticas obtenidas exitosamente", 
                    java.time.LocalDateTime.now(), stats));

        } catch (Exception e) {
            log.error("Error obteniendo estadísticas del pool", e);
            return ResponseEntity.badRequest().body(
                    new ApiResponse(false, "Error al obtener estadísticas: " + e.getMessage(), 
                    java.time.LocalDateTime.now()));
        }
    }

    /**
     * Fuerza la generación de ejercicios para plantillas específicas
     * Útil para administradores que quieren llenar el pool rápidamente
     */
    @PostMapping("/pool/warm-up")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> warmUpPool(@RequestBody List<Integer> templateIds) {
        
        log.info("Iniciando warm-up del pool para {} plantillas", templateIds.size());

        try {
            // Ejecutar en un hilo separado para no bloquear la respuesta
            CompletableFuture.runAsync(() -> exercisePoolService.warmUpPool(templateIds));

            return ResponseEntity.ok(new ApiResponse(true, "El proceso de warm-up ha comenzado en segundo plano", 
                    java.time.LocalDateTime.now(), "Warm-up iniciado"));

        } catch (Exception e) {
            log.error("Error iniciando warm-up del pool", e);
            return ResponseEntity.badRequest().body(
                    new ApiResponse(false, "Error al iniciar warm-up: " + e.getMessage(), 
                    java.time.LocalDateTime.now()));
        }
    }

    /**
     * Genera ejercicios de forma asíncrona para una plantilla específica
     */
    @PostMapping("/pool/generate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> generateExercisesAsync(
            @RequestParam @NotNull @Positive Integer templateId,
            @RequestParam @NotNull @Positive Integer count) {
        
        log.info("Iniciando generación asíncrona de {} ejercicios para plantilla {}", count, templateId);

        try {
            CompletableFuture<Integer> future = exercisePoolService.generateExercisesAsync(templateId, count);
            
            // No esperamos el resultado, solo confirmamos que se inició
            String message = String.format("Se iniciará la generación de %d ejercicios para la plantilla %d", count, templateId);
            return ResponseEntity.ok(new ApiResponse(true, message, 
                    java.time.LocalDateTime.now(), "Generación iniciada"));

        } catch (Exception e) {
            log.error("Error iniciando generación asíncrona", e);
            return ResponseEntity.badRequest().body(
                    new ApiResponse(false, "Error al iniciar generación: " + e.getMessage(), 
                    java.time.LocalDateTime.now()));
        }
    }

    /**
     * Verifica si hay ejercicios disponibles en el pool para una plantilla
     */
    @GetMapping("/pool/availability/{templateId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> checkPoolAvailability(@PathVariable Integer templateId) {
        
        log.debug("Verificando disponibilidad del pool para plantilla {}", templateId);

        try {
            boolean hasAvailable = exercisePoolService.hasAvailableExercises(templateId);
            String message = hasAvailable ? "Ejercicios disponibles en el pool" : "No hay ejercicios disponibles en el pool";
            
            return ResponseEntity.ok(new ApiResponse(true, message, 
                    java.time.LocalDateTime.now(), hasAvailable));

        } catch (Exception e) {
            log.error("Error verificando disponibilidad del pool", e);
            return ResponseEntity.badRequest().body(
                    new ApiResponse(false, "Error al verificar disponibilidad: " + e.getMessage(), 
                    java.time.LocalDateTime.now()));
        }
    }

    /**
     * Endpoint de salud para verificar que la integración con Azure AI está funcionando
     */
    @GetMapping("/health")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> healthCheck() {
        
        log.info("Verificando salud del servicio de ejercicios generados");

        try {
            // Verificar que Azure AI esté configurado
            boolean isConfigured = exercisePoolService.getPoolStatistics() != null;
            
            if (isConfigured) {
                return ResponseEntity.ok(new ApiResponse(true, 
                        "Servicio de ejercicios generados funcionando correctamente", 
                        java.time.LocalDateTime.now(), "Healthy"));
            } else {
                return ResponseEntity.ok(new ApiResponse(true, 
                        "Servicio funcionando pero Azure AI no está configurado", 
                        java.time.LocalDateTime.now(), "Warning"));
            }

        } catch (Exception e) {
            log.error("Error en health check", e);
            return ResponseEntity.badRequest().body(
                    new ApiResponse(false, "Servicio no disponible: " + e.getMessage(), 
                    java.time.LocalDateTime.now()));
        }
    }

    /**
     * Obtiene estadísticas detalladas del pool (solo para administradores)
     */
    @GetMapping("/pool/detailed-stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getDetailedPoolStats() {
        
        log.info("Obteniendo estadísticas detalladas del pool");

        try {
            var poolStats = exercisePoolService.getPoolStatistics();
            
            // Crear objeto con estadísticas detalladas
            var detailedStats = new Object() {
                public final Integer totalGenerated = poolStats.getTotalGenerated();
                public final Integer totalUsed = poolStats.getTotalUsed();
                public final Integer availableInPool = poolStats.getAvailableInPool();
                public final Double cacheHitRate = poolStats.getCacheHitRate();
                public final Double averageGenerationTime = poolStats.getAverageGenerationTimeMs();
                public final String status = availableInPool > 0 ? "HEALTHY" : "LOW_STOCK";
            };

            return ResponseEntity.ok(new ApiResponse(true, "Estadísticas detalladas obtenidas exitosamente", 
                    java.time.LocalDateTime.now(), detailedStats));

        } catch (Exception e) {
            log.error("Error obteniendo estadísticas detalladas", e);
            return ResponseEntity.badRequest().body(
                    new ApiResponse(false, "Error al obtener estadísticas detalladas: " + e.getMessage(), 
                    java.time.LocalDateTime.now()));
        }
    }
}
