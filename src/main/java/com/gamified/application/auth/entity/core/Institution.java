package com.gamified.application.auth.entity.core;


import lombok.*;
import java.time.LocalDateTime;

/**
 * POJO que representa las instituciones educativas
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(of = {"id", "name"})
public class Institution {

    private Long id;
    private String name;
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String phone;
    private String email;
    private String website;
    private String logoUrl;
    private Boolean status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Constructor para mapeo desde stored procedures (datos básicos)
     */
    public Institution(Long id, String name, String email, String phone, Boolean status) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.status = status;
    }

    /**
     * Constructor para mapeo desde stored procedures (datos completos)
     */
    public Institution(Long id, String name, String address, String city, String state,
                       String country, String postalCode, String phone, String email,
                       String website, String logoUrl, Boolean status) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.city = city;
        this.state = state;
        this.country = country;
        this.postalCode = postalCode;
        this.phone = phone;
        this.email = email;
        this.website = website;
        this.logoUrl = logoUrl;
        this.status = status;
    }

    /**
     * Verifica si la institución está activa
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(this.status);
    }

    /**
     * Activa la institución
     */
    public void activate() {
        this.status = true;
    }

    /**
     * Desactiva la institución
     */
    public void deactivate() {
        this.status = false;
    }

    /**
     * Obtiene la dirección completa formateada
     */
    public String getFullAddress() {
        StringBuilder fullAddress = new StringBuilder();

        if (address != null && !address.trim().isEmpty()) {
            fullAddress.append(address);
        }

        if (city != null && !city.trim().isEmpty()) {
            if (!fullAddress.isEmpty()) fullAddress.append(", ");
            fullAddress.append(city);
        }

        if (state != null && !state.trim().isEmpty()) {
            if (!fullAddress.isEmpty()) fullAddress.append(", ");
            fullAddress.append(state);
        }

        if (country != null && !country.trim().isEmpty()) {
            if (!fullAddress.isEmpty()) fullAddress.append(", ");
            fullAddress.append(country);
        }

        if (postalCode != null && !postalCode.trim().isEmpty()) {
            if (!fullAddress.isEmpty()) fullAddress.append(" ");
            fullAddress.append(postalCode);
        }

        return fullAddress.toString();
    }

    /**
     * Válida que la institución tenga los datos mínimos requeridos
     */
    public boolean isValid() {
        return name != null && !name.trim().isEmpty() &&
                address != null && !address.trim().isEmpty() &&
                phone != null && !phone.trim().isEmpty() &&
                email != null && !email.trim().isEmpty();
    }

    /**
     * Obtiene información básica de la institución
     */
    public String getBasicInfo() {
        StringBuilder info = new StringBuilder(name != null ? name : "Sin nombre");
        if (city != null && !city.trim().isEmpty()) {
            info.append(" - ").append(city);
        }
        if (country != null && !country.trim().isEmpty()) {
            info.append(", ").append(country);
        }
        return info.toString();
    }
}
