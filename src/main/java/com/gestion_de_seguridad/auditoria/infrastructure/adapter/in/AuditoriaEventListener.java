package com.gestion_de_seguridad.auditoria.infrastructure.adapter.in;

import com.gestion_de_seguridad.acceso.domain.event.CheckInRealizadoEvent;
import com.gestion_de_seguridad.acceso.domain.event.CheckOutRealizadoEvent;
import com.gestion_de_seguridad.acceso.domain.event.VisitaAprobadaEvent;
import com.gestion_de_seguridad.acceso.domain.event.VisitaCerradaPorSistemaEvent;
import com.gestion_de_seguridad.acceso.domain.event.VisitaCreadaEvent;
import com.gestion_de_seguridad.acceso.domain.event.VisitaRechazadaEvent;
import com.gestion_de_seguridad.auditoria.domain.port.in.RegistrarAuditoriaUseCase;
import com.gestion_de_seguridad.incidentes.domain.event.IncidenteReportadoEvent;
import com.gestion_de_seguridad.personas.domain.event.PersonaBloqueadaEvent;
import com.gestion_de_seguridad.personas.domain.event.PersonaRegistradaEvent;
import com.gestion_de_seguridad.shared.domain.event.EventPublisher;

/**
 * Listener (patron Observer) que conecta la auditoria con los eventos de
 * dominio publicados por otros slices (acceso, personas).
 *
 * El slice de auditoria se suscribe a los eventos que le interesan SIN que los
 * slices emisores lo conozcan: respeta el desacople de arquitectura hexagonal
 * y la comunicacion por eventos de dominio exigida por la especificacion.
 */
public class AuditoriaEventListener {

    private final RegistrarAuditoriaUseCase auditoria;

    public AuditoriaEventListener(RegistrarAuditoriaUseCase auditoria) {
        this.auditoria = auditoria;
    }

    /**
     * Registra las suscripciones en el EventPublisher compartido.
     */
    public void registrar(EventPublisher publisher) {
        publisher.suscribir(VisitaCreadaEvent.class, e ->
                auditoria.registrar(null, "CREAR_VISITA", "Visita", e.entidadId(),
                        "Estado: " + e.getEstado() + " / Flujo: " + e.getNombreFlujo()));

        publisher.suscribir(VisitaAprobadaEvent.class, e ->
                auditoria.registrar(null, "APROBAR_VISITA", "Visita", e.entidadId(), null));

        publisher.suscribir(VisitaRechazadaEvent.class, e ->
                auditoria.registrar(null, "RECHAZAR_VISITA", "Visita", e.entidadId(), null));

        publisher.suscribir(CheckInRealizadoEvent.class, e ->
                auditoria.registrar(null, "CHECK_IN", "Visita", e.entidadId(), null));

        publisher.suscribir(CheckOutRealizadoEvent.class, e ->
                auditoria.registrar(null, "CHECK_OUT", "Visita", e.entidadId(), null));

        // Cierre automatico por salida olvidada: evento auditable distinto de un
        // check-out normal (exigido en la seccion 4.2).
        publisher.suscribir(VisitaCerradaPorSistemaEvent.class, e ->
                auditoria.registrar(null, "CERRADA_POR_SISTEMA", "Visita", e.entidadId(),
                        "Motivo: " + e.getMotivo()));

        publisher.suscribir(PersonaRegistradaEvent.class, e ->
                auditoria.registrar(null, "REGISTRAR_PERSONA", "Persona", e.entidadId(),
                        "DNI: " + e.getDni()));

        publisher.suscribir(PersonaBloqueadaEvent.class, e ->
                auditoria.registrar(null, "BLOQUEAR_PERSONA", "Persona", e.entidadId(), null));

        publisher.suscribir(IncidenteReportadoEvent.class, e ->
                auditoria.registrar(null, "REPORTAR_INCIDENTE", "Incidente", e.entidadId(),
                        "Severidad: " + e.getSeveridad()));
    }
}
