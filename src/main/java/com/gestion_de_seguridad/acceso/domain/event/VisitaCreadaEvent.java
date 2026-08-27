package com.gestion_de_seguridad.acceso.domain.event;

import com.gestion_de_seguridad.acceso.domain.model.EstadoVisita;
import com.gestion_de_seguridad.shared.domain.DomainEvent;

/**
 * Evento de dominio: se crea una visita. La auditoria lo registra y se
 * notifica al funcionario cuando es no anunciada.
 */
public class VisitaCreadaEvent implements DomainEvent {

    private final long ocurridoEn;
    private final Long visitaId;
    private final Long personaId;
    private final EstadoVisita estado;
    private final String nombreFlujo;

    public VisitaCreadaEvent(Long visitaId, Long personaId, EstadoVisita estado, String nombreFlujo) {
        this.ocurridoEn = System.currentTimeMillis();
        this.visitaId = visitaId;
        this.personaId = personaId;
        this.estado = estado;
        this.nombreFlujo = nombreFlujo;
    }

    @Override
    public long ocurridoEn() {
        return ocurridoEn;
    }

    @Override
    public Long entidadId() {
        return visitaId;
    }

    public Long getPersonaId() {
        return personaId;
    }

    public EstadoVisita getEstado() {
        return estado;
    }

    public String getNombreFlujo() {
        return nombreFlujo;
    }
}
