package com.gamified.application.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            // Query to get user details by username
            String sql = "SELECT u.id, u.username, u.password, u.is_active, u.email_verified, u.status, r.name as role_name " +
                    "FROM [user] u " +
                    "JOIN role r ON u.role_id = r.id " +
                    "WHERE u.username = ? AND u.is_active = 1";

            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                boolean enabled = rs.getBoolean("is_active") && 
                                  rs.getBoolean("email_verified") && 
                                  rs.getBoolean("status");
                
                // Create Spring Security User object with role
                return new User(
                        rs.getString("username"),
                        rs.getString("password"),
                        enabled,
                        true,  // account non-expired
                        true,  // credentials non-expired
                        rs.getBoolean("status"),  // account non-locked
                        Collections.singleton(new SimpleGrantedAuthority("ROLE_" + rs.getString("role_name")))
                );
            }, username);

        } catch (Exception e) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
    }
} 