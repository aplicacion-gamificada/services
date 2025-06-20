package com.gamified.application.shared.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO para respuestas comunes a operaciones
 * @param <T> Tipo de datos que puede contener la respuesta
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonResponseDto<T> {
    
    private Boolean success;
    private String message;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    private String operationType;
    private Long entityId;
    private String entityType;
    
    private T data;
    private Map<String, Object> additionalData;
    
    /**
     * Constructor simplificado
     */
    public CommonResponseDto(Boolean success, String message, Map<String, Object> additionalData) {
        this.success = success;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.additionalData = additionalData;
    }
    
    /**
     * Constructor con datos genéricos
     */
    public CommonResponseDto(Boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.data = data;
    }
}
