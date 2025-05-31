package com.gamified.application.auth.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

public class UserRequestDto {
    /**
     * DTO para actualización de perfil de usuario
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class UserUpdateRequestDto {

        @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
        @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*$", message = "El nombre solo puede contener letras y espacios")
        private String firstName;

        @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
        @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*$", message = "El apellido solo puede contener letras y espacios")
        private String lastName;

        @Size(max = 1024, message = "La URL de la foto de perfil no puede exceder 1024 caracteres")
        @Pattern(regexp = "^https?://.*\\.(jpg|jpeg|png|gif|webp)$",
                message = "Debe ser una URL válida de imagen (jpg, jpeg, png, gif, webp)")
        private String profilePictureUrl;

        /**
         * Verifica si hay al menos un campo para actualizar
         */
        public boolean hasUpdates() {
            return (firstName != null && !firstName.trim().isEmpty()) ||
                    (lastName != null && !lastName.trim().isEmpty()) ||
                    (profilePictureUrl != null && !profilePictureUrl.trim().isEmpty());
        }
    }

    /**
     * DTO para búsqueda y filtrado de usuarios (solo para administradores/profesores)
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class UserSearchRequestDto {

        private String searchTerm; // Búsqueda en nombre, apellido o email

        @Min(value = 1, message = "El ID de rol debe ser válido")
        @Max(value = 3, message = "El ID de rol debe ser válido")
        private Long roleId;

        @Positive(message = "El ID de institución debe ser válido")
        private Long institutionId;

        private Boolean status; // true = activo, false = inactivo, null = todos

        private Boolean emailVerified; // true = verificado, false = no verificado, null = todos

        @Min(value = 0, message = "La página debe ser mayor o igual a 0")
        @Builder.Default
        private Integer page = 0;

        @Min(value = 1, message = "El tamaño de página debe ser mayor a 0")
        @Max(value = 100, message = "El tamaño de página no puede exceder 100")
        @Builder.Default
        private Integer size = 20;

        @Pattern(regexp = "^(firstName|lastName|email|createdAt)$",
                message = "Campo de ordenamiento no válido")
        @Builder.Default
        private String sortBy = "createdAt";

        @Pattern(regexp = "^(asc|desc)$", message = "Dirección de ordenamiento no válida")
        @Builder.Default
        private String sortDirection = "desc";

        /**
         * Verifica si hay filtros aplicados
         */
        public boolean hasFilters() {
            return (searchTerm != null && !searchTerm.trim().isEmpty()) ||
                    roleId != null ||
                    institutionId != null ||
                    status != null ||
                    emailVerified != null;
        }
    }
}
