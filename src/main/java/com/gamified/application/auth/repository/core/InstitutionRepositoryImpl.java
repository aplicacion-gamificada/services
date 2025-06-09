package com.gamified.application.auth.repository.core;

import com.gamified.application.auth.entity.core.Institution;
import com.gamified.application.auth.repository.interfaces.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del repositorio de instituciones con JDBC
 */
@Repository
public class InstitutionRepositoryImpl implements InstitutionRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public InstitutionRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Institution> findById(Long id) {
        try {
            String sql = "SELECT * FROM institution WHERE id = ?";
            List<Institution> institutions = jdbcTemplate.query(sql, (rs, rowNum) -> mapInstitution(rs), id);
            return institutions.isEmpty() ? Optional.empty() : Optional.of(institutions.get(0));
        } catch (Exception e) {
            // Log error
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public Result<Institution> save(Institution institution) {
        try {
            if (institution.getId() == null) {
                return insert(institution);
            } else {
                return update(institution);
            }
        } catch (Exception e) {
            return Result.failure("Error al guardar institución: " + e.getMessage(), e);
        }
    }

    private Result<Institution> insert(Institution institution) {
        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO institution (name, address, city, state, country, postal_code, " +
                    "phone, email, website, logo_url, status, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
                );
                ps.setString(1, institution.getName());
                ps.setString(2, institution.getAddress());
                ps.setString(3, institution.getCity());
                ps.setString(4, institution.getState());
                ps.setString(5, institution.getCountry());
                ps.setString(6, institution.getPostalCode());
                ps.setString(7, institution.getPhone());
                ps.setString(8, institution.getEmail());
                ps.setString(9, institution.getWebsite());
                ps.setString(10, institution.getLogoUrl());
                ps.setBoolean(11, institution.getStatus() != null ? institution.getStatus() : true);
                
                LocalDateTime now = LocalDateTime.now();
                ps.setTimestamp(12, Timestamp.valueOf(institution.getCreatedAt() != null ? institution.getCreatedAt() : now));
                ps.setTimestamp(13, Timestamp.valueOf(institution.getUpdatedAt() != null ? institution.getUpdatedAt() : now));
                
                return ps;
            }, keyHolder);
            
            Long institutionId = keyHolder.getKey().longValue();
            institution.setId(institutionId);
            
            return Result.success(institution);
        } catch (Exception e) {
            return Result.failure("Error al insertar institución: " + e.getMessage(), e);
        }
    }

    private Result<Institution> update(Institution institution) {
        try {
            int rowsAffected = jdbcTemplate.update(
                "UPDATE institution SET name = ?, address = ?, city = ?, state = ?, " +
                "country = ?, postal_code = ?, phone = ?, email = ?, website = ?, " +
                "logo_url = ?, status = ?, updated_at = ? WHERE id = ?",
                institution.getName(),
                institution.getAddress(),
                institution.getCity(),
                institution.getState(),
                institution.getCountry(),
                institution.getPostalCode(),
                institution.getPhone(),
                institution.getEmail(),
                institution.getWebsite(),
                institution.getLogoUrl(),
                institution.getStatus(),
                Timestamp.valueOf(LocalDateTime.now()),
                institution.getId()
            );
            
            if (rowsAffected > 0) {
                return Result.success(institution);
            } else {
                return Result.failure("No se encontró la institución con ID: " + institution.getId());
            }
        } catch (Exception e) {
            return Result.failure("Error al actualizar institución: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public Result<Boolean> delete(Long id) {
        try {
            int rowsAffected = jdbcTemplate.update("DELETE FROM institution WHERE id = ?", id);
            if (rowsAffected > 0) {
                return Result.success(true);
            } else {
                return Result.failure("No se encontró la institución con ID: " + id);
            }
        } catch (Exception e) {
            return Result.failure("Error al eliminar institución: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Institution> findAll() {
        try {
            return jdbcTemplate.query("SELECT * FROM institution", (rs, rowNum) -> mapInstitution(rs));
        } catch (Exception e) {
            // Log error
            return new ArrayList<>();
        }
    }

    @Override
    public Optional<Institution> findByName(String name) {
        try {
            String sql = "SELECT * FROM institution WHERE name = ?";
            List<Institution> institutions = jdbcTemplate.query(sql, (rs, rowNum) -> mapInstitution(rs), name);
            return institutions.isEmpty() ? Optional.empty() : Optional.of(institutions.get(0));
        } catch (Exception e) {
            // Log error
            return Optional.empty();
        }
    }

    @Override
    public List<Institution> findByLocation(String country, String state, String city) {
        try {
            StringBuilder sql = new StringBuilder("SELECT * FROM institution WHERE country = ?");
            List<Object> params = new ArrayList<>();
            params.add(country);
            
            if (state != null && !state.trim().isEmpty()) {
                sql.append(" AND state = ?");
                params.add(state);
            }
            
            if (city != null && !city.trim().isEmpty()) {
                sql.append(" AND city = ?");
                params.add(city);
            }
            
            return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> mapInstitution(rs), params.toArray());
        } catch (Exception e) {
            // Log error
            return new ArrayList<>();
        }
    }

    @Override
    public List<Institution> findActiveInstitutions() {
        return findAllActive();
    }

    @Override
    public boolean updateStatus(Long institutionId, boolean active) {
        try {
            int rowsAffected = jdbcTemplate.update(
                "UPDATE institution SET status = ?, updated_at = ? WHERE id = ?",
                active,
                Timestamp.valueOf(LocalDateTime.now()),
                institutionId
            );
            return rowsAffected > 0;
        } catch (Exception e) {
            // Log error
            return false;
        }
    }

    @Override
    public List<Institution> findAllActive() {
        try {
            return jdbcTemplate.query(
                "SELECT * FROM institution WHERE status = 1",
                (rs, rowNum) -> mapInstitution(rs)
            );
        } catch (Exception e) {
            // Log error
            return new ArrayList<>();
        }
    }

    @Override
    public List<Institution> searchByName(String query, int limit) {
        try {
            return jdbcTemplate.query(
                "SELECT * FROM institution WHERE name LIKE ? LIMIT ?",
                (rs, rowNum) -> mapInstitution(rs),
                "%" + query + "%",
                limit
            );
        } catch (Exception e) {
            // Log error
            return new ArrayList<>();
        }
    }
    
    // Mapeo de ResultSet a Institution
    private Institution mapInstitution(ResultSet rs) throws SQLException {
        return new Institution(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("address"),
            rs.getString("city"),
            rs.getString("state"),
            rs.getString("country"),
            rs.getString("postal_code"),
            rs.getString("phone"),
            rs.getString("email"),
            rs.getString("website"),
            rs.getString("logo_url"),
            rs.getBoolean("status")
        );
    }
} 