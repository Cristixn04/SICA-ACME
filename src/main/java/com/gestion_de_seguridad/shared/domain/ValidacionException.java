package com.gestion_de_seguridad.shared.domain;

/**
 * Se lanza cuando un dato ingresado por el usuario no es valido
 * (campo vacio, formato incorrecto, longitud excedida, caracteres no
 * permitidos, etc.). El mensaje describe claramente el problema en espanol.
 */
public class ValidacionException extends DomainException {

    public ValidacionException(String mensaje) {
        super(mensaje);
    }
}
