package com.gestion_de_seguridad.incidentes.domain.event;

import com.gestion_de_seguridad.incidentes.domain.model.SeveridadIncidente;
import com.gestion_de_seguridad.shared.domain.DomainEvent;

/**
 * Evento de dominio: se reporta un incidente de seguridad.
 * La auditoria lo registra.
 */
public class IncidenteReportadoEvent implements DomainEvent {

    private final long ocurridoEn;
    private final Long incidenteId;
    private final Long personaId;
    private final SeveridadIncidente severidad;
    private final Long actorId;

    public IncidenteReportadoEvent(Long incidenteId, Long personaId, SeveridadIncidente severidad, Long actorId) {
        this.ocurridoEn = System.currentTimeMillis();
        this.incidenteId = incidenteId;
        this.personaId = personaId;
        this.severidad = severidad;
        this.actorId = actorId;
    }

    @Override
    public long ocurridoEn() {
        return ocurridoEn;
    }

    @Override
    public Long entidadId() {
        return incidenteId;
    }

    @Override
    public Long actorId() {
        return actorId;
    }

    public Long getPersonaId() {
        return personaId;
    }

    public SeveridadIncidente getSeveridad() {
        return severidad;
    }
}
