package com.gamified.application.auth.exception;

/**
 * Excepción lanzada cuando se intenta registrar un usuario con un email que ya existe
 */
public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String message) {
        super(message);
    }

    public EmailAlreadyExistsException() {
        super("El email ya está registrado en el sistema");
    }
} 