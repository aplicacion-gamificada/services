package com.gamified.application.exercise.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad que representa un ejercicio específico generado por IA
 * Mapea a la tabla 'generated_exercise' en la base de datos
 * 
 * Esquema real de la BD:
 * - id (bigint)
 * - exercise_template_id (int) 
 * - generated_content_json (nvarchar(max))
 * - correct_answer_hash (varchar(256))
 * - generation_prompt (nvarchar(max))
 * - ai_model_version (varchar(100))
 * - created_at (datetime2)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedExercise {
    private Long id;
    private Integer exerciseTemplateId; // FK a la tabla 'exercise' (plantilla)
    private String generatedContentJson; // Contenido JSON completo devuelto por la IA - Campo real: generated_content_json
    private String correctAnswerHash; // Hash SHA256 de la respuesta correcta - Campo real: correct_answer_hash  
    private String generationPrompt; // Prompt usado para generar - Campo real: generation_prompt
    private String aiModelVersion; // Versión del modelo AI usado - Campo real: ai_model_version
    private LocalDateTime createdAt; // Campo real: created_at
    
    // Relación virtual con la plantilla del ejercicio
    private Exercise exerciseTemplate;
    
    // Métodos de compatibilidad para el código existente que espera los nombres antiguos
    public String getAiContentJson() {
        return generatedContentJson;
    }
    
    public void setAiContentJson(String content) {
        this.generatedContentJson = content;
    }
    
    public String getAnswerHash() {
        return correctAnswerHash;
    }
    
    public void setAnswerHash(String hash) {
        this.correctAnswerHash = hash;
    }
    
    // Métodos para campos que no existen en el esquema real pero el código los usa
    public Integer getStudentProfileId() {
        // En el esquema real no hay student_profile_id en generated_exercise
        // Los ejercicios generados son genéricos y se asignan en exercise_attempt
        return null;
    }
    
    public void setStudentProfileId(Integer studentProfileId) {
        // No-op ya que el campo no existe en el esquema real
    }
    
    public String getDifficultyLevel() {
        // En el esquema real no hay difficulty_level en generated_exercise
        return "medium"; // valor por defecto
    }
    
    public void setDifficultyLevel(String difficulty) {
        // No-op ya que el campo no existe en el esquema real
    }
    
    public String getStatus() {
        // En el esquema real no hay status en generated_exercise
        return "available"; // valor por defecto
    }
    
    public void setStatus(String status) {
        // No-op ya que el campo no existe en el esquema real
    }
    
    public LocalDateTime getUsedAt() {
        // En el esquema real no hay used_at en generated_exercise
        return null;
    }
    
    public void setUsedAt(LocalDateTime usedAt) {
        // No-op ya que el campo no existe en el esquema real
    }
}
