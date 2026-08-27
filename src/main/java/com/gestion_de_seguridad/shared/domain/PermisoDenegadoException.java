package com.gestion_de_seguridad.shared.domain;

/**
 * Se lanza cuando un usuario autenticado no posee el permiso requerido
 * para ejecutar una operacion (control de acceso basado en roles).
 */
public class PermisoDenegadoException extends DomainException {

    public PermisoDenegadoException(String permiso) {
        super("Acceso denegado: el usuario no posee el permiso requerido ('" + permiso + "').");
    }
}
