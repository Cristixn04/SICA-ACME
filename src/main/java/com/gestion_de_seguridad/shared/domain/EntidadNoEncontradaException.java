package com.gestion_de_seguridad.shared.domain;

/**
 * Se lanza cuando se intenta consultar una operacion sobre una entidad
 * que no existe en el sistema (ej. un DNI no registrado).
 */
public class EntidadNoEncontradaException extends DomainException {

    public EntidadNoEncontradaException(String tipoEntidad, Object identificador) {
        super("No se encontro " + tipoEntidad + " con identificador: " + identificador + ".");
    }

    public EntidadNoEncontradaException(String mensajeClaro) {
        super(mensajeClaro);
    }
}
