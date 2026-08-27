package com.gestion_de_seguridad.shared.domain;

/**
 * Excepcion base para errores de reglas de negocio del dominio.
 *
 * Todas las excepciones especificas de negocio del sistema deben extender
 * esta clase para diferenciarse claramente de los errores tecnicos
 * (JDBC, I/O, etc.). El mensaje debe ser legible y en espanol, pensado para
 * mostrarse al usuario final.
 */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
