package com.gamified.application.auth.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/perf-test")
@RequiredArgsConstructor
@Slf4j
public class PerformanceTestController {

    private final DataSource dataSource;

    @GetMapping("/jdbc-metrics")
    public ResponseEntity<Map<String, Object>> testJdbcPerformance() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            // Medir tiempo de conexión
            long startConnection = System.currentTimeMillis();
            Connection conn = dataSource.getConnection();
            long connectedTime = System.currentTimeMillis();
            long connectionTime = connectedTime - startConnection;
            
            metrics.put("connectionTimeMs", connectionTime);
            log.info("Tiempo de conexión: {} ms", connectionTime);
            
            // Medir tiempo de preparación de sentencia
            long startPrepare = System.currentTimeMillis();
            PreparedStatement stmt = conn.prepareStatement("SELECT TOP 10 * FROM [user]");
            long preparedTime = System.currentTimeMillis();
            long prepareTime = preparedTime - startPrepare;
            
            metrics.put("prepareStatementTimeMs", prepareTime);
            log.info("Tiempo de preparación: {} ms", prepareTime);
            
            // Medir tiempo de ejecución de query
            long startQuery = System.currentTimeMillis();
            ResultSet rs = stmt.executeQuery();
            long queriedTime = System.currentTimeMillis();
            long queryTime = queriedTime - startQuery;
            
            metrics.put("queryExecutionTimeMs", queryTime);
            log.info("Tiempo de ejecución: {} ms", queryTime);
            
            // Medir tiempo de procesamiento de resultados
            long startProcess = System.currentTimeMillis();
            int count = 0;
            while (rs.next()) {
                count++;
            }
            long processedTime = System.currentTimeMillis();
            long processTime = processedTime - startProcess;
            
            metrics.put("resultProcessingTimeMs", processTime);
            metrics.put("rowCount", count);
            log.info("Tiempo de procesamiento: {} ms para {} filas", processTime, count);
            
            // Tiempo total
            long totalTime = processedTime - startConnection;
            metrics.put("totalTimeMs", totalTime);
            log.info("Tiempo total: {} ms", totalTime);
            
            // Cerrar recursos
            rs.close();
            stmt.close();
            conn.close();
            
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            log.error("Error en prueba de rendimiento JDBC", e);
            metrics.put("error", e.getMessage());
            return ResponseEntity.status(500).body(metrics);
        }
    }
} 