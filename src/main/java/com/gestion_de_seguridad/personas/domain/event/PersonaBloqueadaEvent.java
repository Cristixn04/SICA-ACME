package com.gestion_de_seguridad.personas.domain.event;

import com.gestion_de_seguridad.shared.domain.DomainEvent;

/**
 * Evento de dominio: se publica cuando se bloquea el acceso de una persona
 * (estado INACTIVO). El slice de auditoria lo registra.
 */
public class PersonaBloqueadaEvent implements DomainEvent {

    private final long ocurridoEn;
    private final Long personaId;
    private final Long actorId;
    private final String accion;

    public PersonaBloqueadaEvent(Long personaId, Long actorId) {
        this.ocurridoEn = System.currentTimeMillis();
        this.personaId = personaId;
        this.actorId = actorId;
        this.accion = "BLOQUEAR_PERSONA";
    }

    @Override
    public long ocurridoEn() {
        return ocurridoEn;
    }

    @Override
    public Long entidadId() {
        return personaId;
    }

    @Override
    public Long actorId() {
        return actorId;
    }

    public String getAccion() {
        return accion;
    }
}
