package com.gamified.application.auth.repository.interfaces;

/**
 * Clase para manejar resultados de operaciones de repositorio
 * Permite encapsular éxito/fracaso y mensajes de error
 * @param <T> Tipo de datos del resultado
 */
public class Result<T> {
    private final boolean success;
    private final T data;
    private final String errorMessage;
    private final Throwable exception;
    
    private Result(boolean success, T data, String errorMessage, Throwable exception) {
        this.success = success;
        this.data = data;
        this.errorMessage = errorMessage;
        this.exception = exception;
    }
    
    /**
     * Crea un resultado exitoso con datos
     * @param data Datos del resultado
     * @return Resultado exitoso
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(true, data, null, null);
    }
    
    /**
     * Crea un resultado fallido con mensaje de error
     * @param errorMessage Mensaje de error
     * @return Resultado fallido
     */
    public static <T> Result<T> failure(String errorMessage) {
        return new Result<>(false, null, errorMessage, null);
    }
    
    /**
     * Crea un resultado fallido con mensaje y excepción
     * @param errorMessage Mensaje de error
     * @param exception Excepción que causó el error
     * @return Resultado fallido
     */
    public static <T> Result<T> failure(String errorMessage, Throwable exception) {
        return new Result<>(false, null, errorMessage, exception);
    }
    
    /**
     * Verifica si la operación fue exitosa
     * @return true si fue exitosa, false si no
     */
    public boolean isSuccess() {
        return success;
    }
    
    /**
     * Obtiene los datos del resultado
     * @return Datos del resultado (puede ser null si hubo error)
     */
    public T getData() {
        return data;
    }
    
    /**
     * Obtiene el mensaje de error
     * @return Mensaje de error (null si no hubo error)
     */
    public String getErrorMessage() {
        return errorMessage;
    }
    
    /**
     * Obtiene la excepción que causó el error
     * @return Excepción (null si no hubo error o no se capturó)
     */
    public Throwable getException() {
        return exception;
    }
} 