package com.gestion_de_seguridad.acceso.domain.event;

import com.gestion_de_seguridad.shared.domain.DomainEvent;

/**
 * Evento de dominio: se realiza un check-out de una visita.
 */
public class CheckOutRealizadoEvent implements DomainEvent {

    private final long ocurridoEn;
    private final Long visitaId;
    private final Long actorId;

    public CheckOutRealizadoEvent(Long visitaId, Long actorId) {
        this.ocurridoEn = System.currentTimeMillis();
        this.visitaId = visitaId;
        this.actorId = actorId;
    }

    @Override
    public long ocurridoEn() {
        return ocurridoEn;
    }

    @Override
    public Long entidadId() {
        return visitaId;
    }

    @Override
    public Long actorId() {
        return actorId;
    }
}
