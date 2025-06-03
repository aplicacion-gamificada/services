package com.gamified.application.auth.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.sql.Date;

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

    /**
     * DTO para registro de estudiante
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class StudentRegistrationRequestDto {
        // Datos básicos del usuario
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
        @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*$", message = "El nombre solo puede contener letras y espacios")
        private String firstName;
        
        @NotBlank(message = "El apellido es obligatorio")
        @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
        @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*$", message = "El apellido solo puede contener letras y espacios")
        private String lastName;
        
        @Email(message = "El email debe ser válido")
        @NotBlank(message = "El email es obligatorio")
        private String email;
        
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$", 
                message = "La contraseña debe contener al menos una letra mayúscula, una minúscula, un número y un carácter especial")
        private String password;
        
        // Datos específicos del estudiante
        @NotBlank(message = "El nombre de usuario es obligatorio")
        @Size(min = 3, max = 20, message = "El nombre de usuario debe tener entre 3 y 20 caracteres")
        @Pattern(regexp = "^[a-zA-Z0-9._-]*$", message = "El nombre de usuario solo puede contener letras, números, puntos, guiones y guiones bajos")
        private String username;
        
        private Date birth_date;
        
        // Datos de institución
        @NotNull(message = "El ID de institución es obligatorio")
        @Positive(message = "El ID de institución debe ser válido")
        private Long institutionId;
        
        // Datos del tutor (opcional)
        private Long guardianProfileId;
    }
    
    /**
     * DTO para registro de profesor
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class TeacherRegistrationRequestDto {
        // Datos básicos del usuario
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
        @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*$", message = "El nombre solo puede contener letras y espacios")
        private String firstName;
        
        @NotBlank(message = "El apellido es obligatorio")
        @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
        @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*$", message = "El apellido solo puede contener letras y espacios")
        private String lastName;
        
        @Email(message = "El email debe ser válido")
        @NotBlank(message = "El email es obligatorio")
        private String email;
        
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$", 
                message = "La contraseña debe contener al menos una letra mayúscula, una minúscula, un número y un carácter especial")
        private String password;
        
        // Datos específicos del profesor
        @NotNull(message = "El ID de área STEM es obligatorio")
        @Min(value = 1, message = "El ID de área STEM debe ser válido")
        @Max(value = 127, message = "El ID de área STEM debe ser válido")
        private Byte stemAreaId;
        
        // Datos de institución
        @NotNull(message = "El ID de institución es obligatorio")
        @Positive(message = "El ID de institución debe ser válido")
        private Long institutionId;
    }
    
    /**
     * DTO para registro de tutor
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class GuardianRegistrationRequestDto {
        // Datos básicos del usuario
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
        @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*$", message = "El nombre solo puede contener letras y espacios")
        private String firstName;
        
        @NotBlank(message = "El apellido es obligatorio")
        @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
        @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*$", message = "El apellido solo puede contener letras y espacios")
        private String lastName;
        
        @Email(message = "El email debe ser válido")
        @NotBlank(message = "El email es obligatorio")
        private String email;
        
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$", 
                message = "La contraseña debe contener al menos una letra mayúscula, una minúscula, un número y un carácter especial")
        private String password;
        
        // Datos específicos del tutor
        @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "El teléfono debe tener entre 8 y 15 dígitos, puede incluir el prefijo +")
        private String phone;
        
        // Datos de institución
        @NotNull(message = "El ID de institución es obligatorio")
        @Positive(message = "El ID de institución debe ser válido")
        private Long institutionId;
    }
    
    /**
     * DTO para asociación de estudiante a tutor
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class StudentGuardianAssociationRequestDto {
        @NotNull(message = "El ID de perfil de estudiante es obligatorio")
        @Positive(message = "El ID de perfil de estudiante debe ser válido")
        private Long studentProfileId;
        
        @NotNull(message = "El ID de perfil de tutor es obligatorio")
        @Positive(message = "El ID de perfil de tutor debe ser válido")
        private Long guardianProfileId;
    }
    
    /**
     * DTO para actualización de perfil de estudiante
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class StudentUpdateRequestDto {
        @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
        @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*$", message = "El nombre solo puede contener letras y espacios")
        private String firstName;
        
        @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
        @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*$", message = "El apellido solo puede contener letras y espacios")
        private String lastName;
        
        @Size(min = 3, max = 20, message = "El nombre de usuario debe tener entre 3 y 20 caracteres")
        @Pattern(regexp = "^[a-zA-Z0-9._-]*$", message = "El nombre de usuario solo puede contener letras, números, puntos, guiones y guiones bajos")
        private String username;
        
        private Date birth_date;
        
        @Size(max = 1024, message = "La URL de la foto de perfil no puede exceder 1024 caracteres")
        @Pattern(regexp = "^https?://.*\\.(jpg|jpeg|png|gif|webp)$",
                message = "Debe ser una URL válida de imagen (jpg, jpeg, png, gif, webp)")
        private String profilePictureUrl;
    }
    
    /**
     * DTO para actualización de perfil de profesor
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class TeacherUpdateRequestDto {
        @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
        @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*$", message = "El nombre solo puede contener letras y espacios")
        private String firstName;
        
        @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
        @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*$", message = "El apellido solo puede contener letras y espacios")
        private String lastName;
        
        @Min(value = 1, message = "El ID de área STEM debe ser válido")
        @Max(value = 127, message = "El ID de área STEM debe ser válido")
        private Byte stemAreaId;
        
        @Size(max = 1024, message = "La URL de la foto de perfil no puede exceder 1024 caracteres")
        @Pattern(regexp = "^https?://.*\\.(jpg|jpeg|png|gif|webp)$",
                message = "Debe ser una URL válida de imagen (jpg, jpeg, png, gif, webp)")
        private String profilePictureUrl;
    }
    
    /**
     * DTO para actualización de perfil de tutor
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class GuardianUpdateRequestDto {
        @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
        @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*$", message = "El nombre solo puede contener letras y espacios")
        private String firstName;
        
        @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
        @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*$", message = "El apellido solo puede contener letras y espacios")
        private String lastName;
        
        @Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "El teléfono debe tener entre 8 y 15 dígitos, puede incluir el prefijo +")
        private String phone;
        
        @Size(max = 1024, message = "La URL de la foto de perfil no puede exceder 1024 caracteres")
        @Pattern(regexp = "^https?://.*\\.(jpg|jpeg|png|gif|webp)$",
                message = "Debe ser una URL válida de imagen (jpg, jpeg, png, gif, webp)")
        private String profilePictureUrl;
    }
    
    /**
     * DTO para actualización de contraseña
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class PasswordUpdateRequestDto {
        @NotBlank(message = "La contraseña actual es obligatoria")
        private String currentPassword;
        
        @NotBlank(message = "La nueva contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$", 
                message = "La contraseña debe contener al menos una letra mayúscula, una minúscula, un número y un carácter especial")
        private String newPassword;
        
        @NotBlank(message = "La confirmación de contraseña es obligatoria")
        private String confirmPassword;
    }
}
