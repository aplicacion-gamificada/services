package com.gamified.application.auth.repository.core;

import com.gamified.application.shared.model.entity.Role;
import com.gamified.application.shared.repository.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del repositorio de roles con JDBC
 */
@Repository
public class RoleRepositoryImpl implements RoleRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public RoleRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Role> findById(Byte id) {
        try {
            String sql = "SELECT * FROM role WHERE id = ?";
            List<Role> roles = jdbcTemplate.query(sql, (rs, rowNum) -> mapRole(rs), id);
            return roles.isEmpty() ? Optional.empty() : Optional.of(roles.get(0));
        } catch (Exception e) {
            // Log error
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public Result<Role> save(Role role) {
        try {
            if (role.getId() == null) {
                return insert(role);
            } else {
                return update(role);
            }
        } catch (Exception e) {
            return Result.failure("Error al guardar rol: " + e.getMessage(), e);
        }
    }

    private Result<Role> insert(Role role) {
        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO role (name, description, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
                );
                ps.setString(1, role.getName());
                ps.setString(2, role.getDescription());
                
                Timestamp now = new Timestamp(System.currentTimeMillis());
                ps.setTimestamp(3, role.getCreatedAt() != null ? role.getCreatedAt() : now);
                ps.setTimestamp(4, role.getUpdatedAt() != null ? role.getUpdatedAt() : now);
                
                return ps;
            }, keyHolder);
            
            Byte roleId = keyHolder.getKey().byteValue();
            role.setId(roleId);
            
            return Result.success(role);
        } catch (Exception e) {
            return Result.failure("Error al insertar rol: " + e.getMessage(), e);
        }
    }

    private Result<Role> update(Role role) {
        try {
            int rowsAffected = jdbcTemplate.update(
                "UPDATE role SET name = ?, description = ?, updated_at = ? WHERE id = ?",
                role.getName(),
                role.getDescription(),
                new Timestamp(System.currentTimeMillis()),
                role.getId()
            );
            
            if (rowsAffected > 0) {
                return Result.success(role);
            } else {
                return Result.failure("No se encontró el rol con ID: " + role.getId());
            }
        } catch (Exception e) {
            return Result.failure("Error al actualizar rol: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public Result<Boolean> delete(Byte id) {
        try {
            int rowsAffected = jdbcTemplate.update("DELETE FROM role WHERE id = ?", id);
            if (rowsAffected > 0) {
                return Result.success(true);
            } else {
                return Result.failure("No se encontró el rol con ID: " + id);
            }
        } catch (Exception e) {
            return Result.failure("Error al eliminar rol: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Role> findAll() {
        try {
            return jdbcTemplate.query("SELECT * FROM role", (rs, rowNum) -> mapRole(rs));
        } catch (Exception e) {
            // Log error
            return new ArrayList<>();
        }
    }

    @Override
    public Optional<Role> findByName(String name) {
        try {
            String sql = "SELECT * FROM role WHERE name = ?";
            List<Role> roles = jdbcTemplate.query(sql, (rs, rowNum) -> mapRole(rs), name);
            return roles.isEmpty() ? Optional.empty() : Optional.of(roles.get(0));
        } catch (Exception e) {
            // Log error
            return Optional.empty();
        }
    }
    
    // Mapeo de ResultSet a Role
    private Role mapRole(ResultSet rs) throws SQLException {
        Role role = new Role();
        role.setId(rs.getByte("id"));
        role.setName(rs.getString("name"));
        role.setDescription(rs.getString("description"));
        role.setCreatedAt(rs.getTimestamp("created_at"));
        role.setUpdatedAt(rs.getTimestamp("updated_at"));
        return role;
    }
} 