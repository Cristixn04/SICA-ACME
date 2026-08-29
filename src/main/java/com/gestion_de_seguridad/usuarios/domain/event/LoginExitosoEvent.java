package com.gestion_de_seguridad.usuarios.domain.event;

import com.gestion_de_seguridad.shared.domain.DomainEvent;

/**
 * Evento de dominio: autenticacion exitosa de un usuario.
 *
 * La auditoria lo escucha para registrar el LOGIN en la bitacora
 * (requisito explicito de la seccion 4.2 de la especificacion).
 */
public class LoginExitosoEvent implements DomainEvent {

    private final long ocurridoEn;
    private final Long usuarioId;
    private final String nombreUsuario;

    public LoginExitosoEvent(Long usuarioId, String nombreUsuario) {
        this.ocurridoEn = System.currentTimeMillis();
        this.usuarioId = usuarioId;
        this.nombreUsuario = nombreUsuario;
    }

    @Override
    public long ocurridoEn() {
        return ocurridoEn;
    }

    @Override
    public Long entidadId() {
        return usuarioId;
    }

    @Override
    public Long actorId() {
        return usuarioId;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }
}