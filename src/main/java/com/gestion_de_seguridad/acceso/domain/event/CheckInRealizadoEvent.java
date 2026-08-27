package com.gestion_de_seguridad.acceso.domain.event;

import com.gestion_de_seguridad.shared.domain.DomainEvent;

/**
 * Evento de dominio: se realiza un check-in de una visita.
 */
public class CheckInRealizadoEvent implements DomainEvent {

    private final long ocurridoEn;
    private final Long visitaId;

    public CheckInRealizadoEvent(Long visitaId) {
        this.ocurridoEn = System.currentTimeMillis();
        this.visitaId = visitaId;
    }

    @Override
    public long ocurridoEn() {
        return ocurridoEn;
    }

    @Override
    public Long entidadId() {
        return visitaId;
    }
}
