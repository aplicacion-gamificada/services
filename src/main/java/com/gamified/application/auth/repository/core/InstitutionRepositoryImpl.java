package com.gamified.application.auth.repository.core;

import com.gamified.application.auth.entity.core.Institution;
import com.gamified.application.auth.repository.interfaces.Result;
import com.gamified.application.auth.util.DatabaseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación del repositorio de instituciones con JDBC
 */
@Repository
public class InstitutionRepositoryImpl implements InstitutionRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public InstitutionRepositoryImpl(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public Optional<Institution> findById(Long id) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("institution_id", id, Types.BIGINT);

            String sql = "EXEC sp_get_institution_by_id @institution_id = :institution_id";
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            if (results.isEmpty()) {
                return Optional.empty();
            }
            
            Map<String, Object> institutionData = results.getFirst();
            
            // Mapear datos de la institución
            Institution institution = mapInstitutionFromResultMap(institutionData);
            
            return Optional.of(institution);
        } catch (Exception e) {
            // Log error
            return Optional.empty();
        }
    }
    
    /**
     * Mapea un Map<String, Object> a un objeto Institution
     */
    private Institution mapInstitutionFromResultMap(Map<String, Object> data) {
        try {
            Institution institution = new Institution();
            
            // Mapeo seguro del ID
            Object idObj = data.get("id");
            if (idObj != null) {
                if (idObj instanceof Number) {
                    institution.setId(((Number) idObj).longValue());
                } else {
                    // Intento de conversión alternativa
                    institution.setId(Long.parseLong(idObj.toString()));
                }
            }
            
            // Mapeo seguro del nombre
            institution.setName((String) data.get("name"));
            
            // Mapeo seguro de campos opcionales
            if (data.get("address") != null) {
                institution.setAddress((String) data.get("address"));
            }
            
            if (data.get("city") != null) {
                institution.setCity((String) data.get("city"));
            }
            
            if (data.get("state") != null) {
                institution.setState((String) data.get("state"));
            }
            
            if (data.get("country") != null) {
                institution.setCountry((String) data.get("country"));
            }
            
            if (data.get("postal_code") != null) {
                institution.setPostalCode((String) data.get("postal_code"));
            }
            
            if (data.get("phone") != null) {
                institution.setPhone((String) data.get("phone"));
            }
            
            if (data.get("email") != null) {
                institution.setEmail((String) data.get("email"));
            }
            
            if (data.get("website") != null) {
                institution.setWebsite((String) data.get("website"));
            }
            
            if (data.get("logo_url") != null) {
                institution.setLogoUrl((String) data.get("logo_url"));
            }
            
            // Mapeo seguro del estado usando DatabaseUtils
            institution.setStatus(DatabaseUtils.safeToBoolean(data.get("status"), true)); // 🔧 SIMPLIFICADO y CORREGIDO
            
            // Mapeo seguro de fechas
            if (data.get("created_at") != null) {
                Object createdAtObj = data.get("created_at");
                if (createdAtObj instanceof Timestamp) {
                    institution.setCreatedAt(((Timestamp) createdAtObj).toLocalDateTime());
                } else if (createdAtObj instanceof LocalDateTime) {
                    institution.setCreatedAt((LocalDateTime) createdAtObj);
                }
            }
            
            if (data.get("updated_at") != null) {
                Object updatedAtObj = data.get("updated_at");
                if (updatedAtObj instanceof Timestamp) {
                    institution.setUpdatedAt(((Timestamp) updatedAtObj).toLocalDateTime());
                } else if (updatedAtObj instanceof LocalDateTime) {
                    institution.setUpdatedAt((LocalDateTime) updatedAtObj);
                }
            }
            
            return institution;
        } catch (Exception e) {
            System.err.println("Error en mapInstitutionFromResultMap: " + e.getMessage());
            throw e; // Re-lanzar para manejar en el nivel superior
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
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("name", institution.getName(), Types.NVARCHAR);
            parameters.addValue("address", institution.getAddress(), Types.NVARCHAR);
            parameters.addValue("city", institution.getCity(), Types.NVARCHAR);
            parameters.addValue("state", institution.getState(), Types.NVARCHAR);
            parameters.addValue("country", institution.getCountry(), Types.NVARCHAR);
            parameters.addValue("postal_code", institution.getPostalCode(), Types.NVARCHAR);
            parameters.addValue("phone", institution.getPhone(), Types.NVARCHAR);
            parameters.addValue("email", institution.getEmail(), Types.NVARCHAR);
            parameters.addValue("website", institution.getWebsite(), Types.NVARCHAR);
            parameters.addValue("logo_url", institution.getLogoUrl(), Types.NVARCHAR);

            String sql = "EXEC sp_create_institution @name = :name, @address = :address, " +
                         "@city = :city, @state = :state, @country = :country, " +
                         "@postal_code = :postal_code, @phone = :phone, @email = :email, " +
                         "@website = :website, @logo_url = :logo_url";
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            if (!results.isEmpty()) {
                Map<String, Object> result = results.getFirst();
                
                // Verificar si la operación fue exitosa
                Integer success = (Integer) result.get("success");
                
                if (success == 1) {
                    // Obtener el ID generado
                    Long institutionId = ((Number) result.get("institution_id")).longValue();
                    institution.setId(institutionId);
                    
                    return Result.success(institution);
                }
            }
            
            return Result.failure("No se pudo crear la institución");
        } catch (Exception e) {
            return Result.failure("Error al insertar institución: " + e.getMessage(), e);
        }
    }

    private Result<Institution> update(Institution institution) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("institution_id", institution.getId(), Types.BIGINT);
            parameters.addValue("name", institution.getName(), Types.NVARCHAR);
            parameters.addValue("address", institution.getAddress(), Types.NVARCHAR);
            parameters.addValue("city", institution.getCity(), Types.NVARCHAR);
            parameters.addValue("state", institution.getState(), Types.NVARCHAR);
            parameters.addValue("country", institution.getCountry(), Types.NVARCHAR);
            parameters.addValue("postal_code", institution.getPostalCode(), Types.NVARCHAR);
            parameters.addValue("phone", institution.getPhone(), Types.NVARCHAR);
            parameters.addValue("email", institution.getEmail(), Types.NVARCHAR);
            parameters.addValue("website", institution.getWebsite(), Types.NVARCHAR);
            parameters.addValue("logo_url", institution.getLogoUrl(), Types.NVARCHAR);
            parameters.addValue("status", institution.getStatus(), Types.BIT);

            String sql = "EXEC sp_update_institution @institution_id = :institution_id, @name = :name, " +
                         "@address = :address, @city = :city, @state = :state, @country = :country, " +
                         "@postal_code = :postal_code, @phone = :phone, @email = :email, " +
                         "@website = :website, @logo_url = :logo_url, @status = :status";
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            if (!results.isEmpty()) {
                Map<String, Object> result = results.getFirst();
                
                // Verificar si la operación fue exitosa
                Integer success = (Integer) result.get("success");
                
                if (success == 1) {
                    return Result.success(institution);
                }
            }
            
            return Result.failure("No se pudo actualizar la institución con ID: " + institution.getId());
        } catch (Exception e) {
            return Result.failure("Error al actualizar institución: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public Result<Boolean> delete(Long id) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("institution_id", id, Types.BIGINT);

            String sql = "EXEC sp_delete_institution @institution_id = :institution_id";
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            if (results.isEmpty()) {
                return Result.failure("No se pudo eliminar la institución");
            }
            
            Map<String, Object> result = results.getFirst();
            
            // Verificar si la operación fue exitosa
            Integer success = (Integer) result.get("success");
            
            if (success != 1) {
                return Result.failure((String) result.get("message"));
            }
            
            return Result.success(true);
        } catch (Exception e) {
            return Result.failure("Error al eliminar institución: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Institution> findAll() {
        try {
            String sql = "EXEC sp_get_all_institutions";
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, new HashMap<>());
            
            List<Institution> institutions = new ArrayList<>();
            for (Map<String, Object> data : results) {
                institutions.add(mapInstitutionFromResultMap(data));
            }
            
            return institutions;
        } catch (Exception e) {
            // Log error
            return new ArrayList<>();
        }
    }

    @Override
    public Optional<Institution> findByName(String name) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("name", name, Types.NVARCHAR);

            String sql = "EXEC sp_get_institution_by_name @name = :name";
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            if (!results.isEmpty()) {
                Map<String, Object> institutionData = results.getFirst();
                
                // Mapear datos de la institución
                Institution institution = mapInstitutionFromResultMap(institutionData);
                
                return Optional.of(institution);
            }
            
            return Optional.empty();
        } catch (Exception e) {
            // Log error
            return Optional.empty();
        }
    }

    @Override
    public List<Institution> findByLocation(String country, String state, String city) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("country", country, Types.VARCHAR);
            parameters.addValue("state", state, Types.VARCHAR);
            parameters.addValue("city", city, Types.VARCHAR);

            String sql = "EXEC sp_get_institutions_by_location @country = :country, @state = :state, @city = :city";
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            List<Institution> institutions = new ArrayList<>();
            for (Map<String, Object> data : results) {
                institutions.add(mapInstitutionFromResultMap(data));
            }
            
            return institutions;
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
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("institution_id", institutionId, Types.BIGINT);
            parameters.addValue("status", active, Types.BIT);

            String sql = "EXEC sp_update_institution_status @institution_id = :institution_id, @status = :status";
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            if (results.isEmpty()) {
                return false;
            }
            
            Map<String, Object> result = results.getFirst();
            
            // Verificar si la operación fue exitosa
            Integer success = (Integer) result.get("success");
            
            return success == 1;
        } catch (Exception e) {
            // Log error
            return false;
        }
    }

    @Override
    public List<Institution> findAllActive() {
        try {
            String sql = "EXEC sp_get_active_institutions";
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, new HashMap<>());
            
            List<Institution> institutions = new ArrayList<>();
            for (Map<String, Object> data : results) {
                institutions.add(mapInstitutionFromResultMap(data));
            }
            
            return institutions;
        } catch (Exception e) {
            // Log error
            System.err.println("Error al ejecutar sp_get_active_institutions: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public List<Institution> searchByName(String query, int limit) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("search_term", query, Types.VARCHAR);
            parameters.addValue("limit", limit, Types.INTEGER);

            String sql = "EXEC sp_search_institutions_by_name @search_term = :search_term, @limit = :limit";
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            List<Institution> institutions = new ArrayList<>();
            for (Map<String, Object> data : results) {
                institutions.add(mapInstitutionFromResultMap(data));
            }
            
            return institutions;
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
            DatabaseUtils.safeToBoolean(rs.getObject("status")) // 🔧 CORREGIDO
        );
    }
} 