package com.gestion_de_seguridad.personas.domain.event;

import com.gestion_de_seguridad.shared.domain.DomainEvent;

/**
 * Evento de dominio: se publica cuando se registra una nueva persona en el
 * sistema. El slice de auditoria lo escucha para registrar la trazabilidad.
 */
public class PersonaRegistradaEvent implements DomainEvent {

    private final long ocurridoEn;
    private final Long personaId;
    private final String dni;
    private final String accion;

    public PersonaRegistradaEvent(Long personaId, String dni) {
        this.ocurridoEn = System.currentTimeMillis();
        this.personaId = personaId;
        this.dni = dni;
        this.accion = "REGISTRAR_PERSONA";
    }

    @Override
    public long ocurridoEn() {
        return ocurridoEn;
    }

    @Override
    public Long entidadId() {
        return personaId;
    }

    public String getDni() {
        return dni;
    }

    public String getAccion() {
        return accion;
    }
}
