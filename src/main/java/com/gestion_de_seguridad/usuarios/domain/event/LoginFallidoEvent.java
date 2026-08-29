package com.gestion_de_seguridad.usuarios.domain.event;

import com.gestion_de_seguridad.shared.domain.DomainEvent;

/**
 * Evento de dominio: intento de autenticacion fallido (usuario inexistente,
 * inactivo o clave incorrecta).
 *
 * La auditoria lo escucha para registrar el LOGIN_FALLIDO en la bitacora.
 */
public class LoginFallidoEvent implements DomainEvent {

    private final long ocurridoEn;
    private final String nombreUsuario;

    public LoginFallidoEvent(String nombreUsuario) {
        this.ocurridoEn = System.currentTimeMillis();
        this.nombreUsuario = nombreUsuario;
    }

    @Override
    public long ocurridoEn() {
        return ocurridoEn;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }
}