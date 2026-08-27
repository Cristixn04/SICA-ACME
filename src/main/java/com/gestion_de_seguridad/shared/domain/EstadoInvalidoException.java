package com.gestion_de_seguridad.shared.domain;

/**
 * Se lanza cuando se intenta realizar una transicion de estado no permitida
 * por la maquina de estados (ej. hacer check-out sin check-in previo).
 */
public class EstadoInvalidoException extends DomainException {

    public EstadoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
