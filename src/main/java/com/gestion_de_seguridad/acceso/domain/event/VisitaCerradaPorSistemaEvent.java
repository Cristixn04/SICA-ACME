package com.gestion_de_seguridad.acceso.domain.event;

import com.gestion_de_seguridad.shared.domain.DomainEvent;

/**
 * Evento de dominio: visita cerrada automaticamente por el sistema por
 * "Salida Olvidada" (CERRADA_POR_SISTEMA).
 *
 * Este evento se registra como auditable distinto de un check-out normal,
 * dado que el cierre lo ejecuto el sistema sin intervencion humana directa.
 */
public class VisitaCerradaPorSistemaEvent implements DomainEvent {

    private final long ocurridoEn;
    private final Long visitaId;
    private final String motivo;

    public VisitaCerradaPorSistemaEvent(Long visitaId, String motivo) {
        this.ocurridoEn = System.currentTimeMillis();
        this.visitaId = visitaId;
        this.motivo = motivo;
    }

    @Override
    public long ocurridoEn() {
        return ocurridoEn;
    }

    @Override
    public Long entidadId() {
        return visitaId;
    }

    public String getMotivo() {
        return motivo;
    }
}
