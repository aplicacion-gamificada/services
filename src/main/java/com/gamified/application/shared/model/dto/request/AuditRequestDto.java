package com.gamified.application.shared.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para solicitudes relacionadas con auditoría
 */
public class AuditRequestDto {

    /**
     * DTO para solicitudes de búsqueda de logs de auditoría
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuditLogSearchRequestDto {
        private List<Long> userIds;
        private List<Byte> actionIds;
        private String entityType;
        private Long entityId;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private String ipAddress;
        private Boolean isSensitive;
        private int limit;
        private int offset;
    }
} 