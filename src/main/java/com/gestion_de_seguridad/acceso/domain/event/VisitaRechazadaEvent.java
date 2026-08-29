package com.gestion_de_seguridad.acceso.domain.event;

import com.gestion_de_seguridad.shared.domain.DomainEvent;

/**
 * Evento de dominio: visita rechazada por el Funcionario.
 */
public class VisitaRechazadaEvent implements DomainEvent {

    private final long ocurridoEn;
    private final Long visitaId;
    private final Long actorId;

    public VisitaRechazadaEvent(Long visitaId, Long actorId) {
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
