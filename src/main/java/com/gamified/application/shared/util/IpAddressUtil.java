package com.gamified.application.shared.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

/**
 * Utilidad para extraer direcciones IP del cliente de manera confiable
 */
public class IpAddressUtil {

    private static final String[] IP_HEADER_CANDIDATES = {
            "X-Forwarded-For",
            "Proxy-Client-IP", 
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
    };

    /**
     * Extrae la dirección IP del cliente desde el HttpServletRequest
     * Considera proxies y load balancers
     * @param request HttpServletRequest
     * @return Dirección IP del cliente
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "0.0.0.0"; // IP desconocida
        }

        for (String header : IP_HEADER_CANDIDATES) {
            String ip = request.getHeader(header);
            if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
                // En el caso de X-Forwarded-For, puede ser una lista de IPs separadas por comas
                // La primera es la IP original del cliente
                return ip.split(",")[0].trim();
            }
        }

        // Fallback a la IP directa si no hay headers de proxy
        String remoteAddr = request.getRemoteAddr();
        return StringUtils.hasText(remoteAddr) ? remoteAddr : "0.0.0.0";
    }

    /**
     * Verifica si una IP es una dirección local/interna
     * @param ip Dirección IP
     * @return true si es una IP local
     */
    public static boolean isLocalIp(String ip) {
        if (ip == null || ip.isEmpty()) return false;
        
        return ip.equals("127.0.0.1") || 
               ip.equals("localhost") || 
               ip.equals("::1") ||
               ip.startsWith("192.168.") ||
               ip.startsWith("10.") ||
               ip.startsWith("172.");
    }

    /**
     * Sanitiza una dirección IP para logging seguro
     * @param ip Dirección IP
     * @return IP sanitizada
     */
    public static String sanitizeIpForLogging(String ip) {
        if (ip == null || ip.isEmpty()) return "unknown";
        
        // Para cumplimiento GDPR/LGPD, podemos enmascarar parte de la IP
        if (ip.contains(".")) {
            String[] parts = ip.split("\\.");
            if (parts.length == 4) {
                return parts[0] + "." + parts[1] + ".xxx.xxx";
            }
        }
        
        return ip;
    }
} 