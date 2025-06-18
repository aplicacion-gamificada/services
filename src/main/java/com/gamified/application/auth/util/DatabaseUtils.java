package com.gamified.application.auth.util;

/**
 * Utilidades para conversión de tipos de datos de la base de datos
 */
public class DatabaseUtils {
    
    /**
     * Convierte valores de base de datos a Boolean de manera segura
     * Maneja Integer (1/0), Boolean, String ("1"/"0", "true"/"false"), y null
     * 
     * @param value Valor a convertir
     * @return Boolean equivalente (false si es null)
     */
    public static Boolean safeToBoolean(Object value) {
        return safeToBoolean(value, false);
    }
    
    /**
     * Convierte valores de base de datos a Boolean de manera segura
     * Maneja Integer (1/0), Boolean, String ("1"/"0", "true"/"false"), y null
     * 
     * @param value Valor a convertir
     * @param defaultValue Valor por defecto si value es null
     * @return Boolean equivalente
     */
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
            // Para cualquier otro tipo numérico
            try {
                return Double.valueOf(value.toString()) == 1.0;
            } catch (NumberFormatException e) {
                // Si no es numérico, intentar como string
                String strValue = value.toString().toLowerCase().trim();
                return "1".equals(strValue) || "true".equals(strValue);
            }
        }
    }
    
    /**
     * Convierte valores de base de datos a Integer de manera segura
     * 
     * @param value Valor a convertir
     * @param defaultValue Valor por defecto si value es null o no convertible
     * @return Integer equivalente
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
    
    /**
     * Convierte valores de base de datos a Long de manera segura
     * 
     * @param value Valor a convertir
     * @param defaultValue Valor por defecto si value es null o no convertible
     * @return Long equivalente
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
     * 
     * @param value Valor a convertir
     * @return Byte equivalente o null si no se puede convertir
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
} 