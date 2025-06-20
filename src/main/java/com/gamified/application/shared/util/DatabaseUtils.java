package com.gamified.application.shared.util;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Utilidades para conversión de tipos de datos de la base de datos
 */
public class DatabaseUtils {
    
    /**
     * Convierte valores de base de datos a Boolean de manera segura
     */
    public static Boolean safeToBoolean(Object value) {
        return safeToBoolean(value, false);
    }
    
    public static Boolean safeToBoolean(Object value, Boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        
        if (value instanceof Integer) {
            return ((Integer) value) == 1;
        } else if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            String strValue = ((String) value).toLowerCase().trim();
            return "1".equals(strValue) || "true".equals(strValue);
        } else if (value instanceof Byte) {
            return ((Byte) value) == 1;
        } else if (value instanceof Short) {
            return ((Short) value) == 1;
        } else if (value instanceof Long) {
            return ((Long) value) == 1L;
        } else {
            try {
                return Double.valueOf(value.toString()) == 1.0;
            } catch (NumberFormatException e) {
                String strValue = value.toString().toLowerCase().trim();
                return "1".equals(strValue) || "true".equals(strValue);
            }
        }
    }
    
    /**
     * Convierte valores de base de datos a Integer de manera segura
     */
    public static Integer safeToInteger(Object value, Integer defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        } else {
            try {
                return Integer.valueOf(value.toString().trim());
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
    }
    
    public static Integer safeToInteger(Object value) {
        return safeToInteger(value, null);
    }
    
    /**
     * Convierte valores de base de datos a Long de manera segura
     */
    public static Long safeToLong(Object value, Long defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        
        if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof Number) {
            return ((Number) value).longValue();
        } else {
            try {
                return Long.valueOf(value.toString().trim());
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
    }
    
    /**
     * Convierte valores de base de datos a Byte de manera segura
     */
    public static Byte safeToByte(Object value) {
        if (value == null) {
            return null;
        }
        
        if (value instanceof Number) {
            return ((Number) value).byteValue();
        } else if (value instanceof String) {
            try {
                return Byte.parseByte((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        
        return null;
    }

    /**
     * Convierte valores de base de datos a LocalDateTime de manera segura
     */
    public static LocalDateTime safeToLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        
        if (value instanceof Timestamp) {
            return ((Timestamp) value).toLocalDateTime();
        } else if (value instanceof java.sql.Date) {
            return ((java.sql.Date) value).toLocalDate().atStartOfDay();
        } else if (value instanceof java.util.Date) {
            return new Timestamp(((java.util.Date) value).getTime()).toLocalDateTime();
        }
        
        return null;
    }

    /**
     * Convierte valores de base de datos a BigDecimal de manera segura
     */
    public static BigDecimal safeToBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        } else if (value instanceof Number) {
            return new BigDecimal(((Number) value).toString());
        } else if (value instanceof String) {
            try {
                return new BigDecimal((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        
        return null;
    }

    /**
     * Convierte valores de base de datos a String de manera segura
     */
    public static String safeToString(Object value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }
} 