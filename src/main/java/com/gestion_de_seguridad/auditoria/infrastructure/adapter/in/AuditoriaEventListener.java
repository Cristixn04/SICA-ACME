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
import com.gestion_de_seguridad.shared.domain.DomainEvent;
import com.gestion_de_seguridad.shared.domain.event.EventPublisher;
import com.gestion_de_seguridad.usuarios.domain.event.LoginExitosoEvent;
import com.gestion_de_seguridad.usuarios.domain.event.LoginFallidoEvent;
import com.gestion_de_seguridad.usuarios.domain.port.in.ConsultarUsuarioUseCase;

/**
 * Listener (patron Observer) que conecta la auditoria con los eventos de
 * dominio publicados por otros slices (acceso, personas, incidentes, usuarios).
 *
 * El slice de auditoria se suscribe a los eventos que le interesan SIN que los
 * slices emisores lo conozcan: respeta el desacople de arquitectura hexagonal
 * y la comunicacion por eventos de dominio exigida por la especificacion.
 *
 * El actor de cada accion se resuelve mediante ConsultarUsuarioUseCase (nunca
 * null salvo eventos iniciados por el sistema), devolviendo trazabilidad real.
 */
public class AuditoriaEventListener {

    private final RegistrarAuditoriaUseCase auditoria;
    private final ConsultarUsuarioUseCase consultarUsuario;

    public AuditoriaEventListener(RegistrarAuditoriaUseCase auditoria,
                                  ConsultarUsuarioUseCase consultarUsuario) {
        this.auditoria = auditoria;
        this.consultarUsuario = consultarUsuario;
    }

    /**
     * Registra las suscripciones en el EventPublisher compartido.
     */
    public void registrar(EventPublisher publisher) {
        publisher.suscribir(VisitaCreadaEvent.class, e ->
                auditoria.registrar(actor(e), "CREAR_VISITA", "Visita", e.entidadId(),
                        "Estado: " + e.getEstado() + " / Flujo: " + e.getNombreFlujo()));

        publisher.suscribir(VisitaAprobadaEvent.class, e ->
                auditoria.registrar(actor(e), "APROBAR_VISITA", "Visita", e.entidadId(), null));

        publisher.suscribir(VisitaRechazadaEvent.class, e ->
                auditoria.registrar(actor(e), "RECHAZAR_VISITA", "Visita", e.entidadId(), null));

        publisher.suscribir(CheckInRealizadoEvent.class, e ->
                auditoria.registrar(actor(e), "CHECK_IN", "Visita", e.entidadId(), null));

        publisher.suscribir(CheckOutRealizadoEvent.class, e ->
                auditoria.registrar(actor(e), "CHECK_OUT", "Visita", e.entidadId(), null));

        // Cierre automatico por salida olvidada: evento auditable distinto de un
        // check-out normal (exigido en la seccion 4.2). El actor es quien
        // provoco el nuevo ingreso (puede ser null si no aplica).
        publisher.suscribir(VisitaCerradaPorSistemaEvent.class, e ->
                auditoria.registrar(actor(e), "CERRADA_POR_SISTEMA", "Visita", e.entidadId(),
                        "Motivo: " + e.getMotivo()));

        publisher.suscribir(PersonaRegistradaEvent.class, e ->
                auditoria.registrar(actor(e), "REGISTRAR_PERSONA", "Persona", e.entidadId(),
                        "DNI: " + e.getDni()));

        publisher.suscribir(PersonaBloqueadaEvent.class, e ->
                auditoria.registrar(actor(e), "BLOQUEAR_PERSONA", "Persona", e.entidadId(), null));

        publisher.suscribir(IncidenteReportadoEvent.class, e ->
                auditoria.registrar(actor(e), "REPORTAR_INCIDENTE", "Incidente", e.entidadId(),
                        "Severidad: " + e.getSeveridad()));

        // Intentos de login (exitosos y fallidos): requisito explicito de la
        // seccion 4.2.
        publisher.suscribir(LoginExitosoEvent.class, e ->
                auditoria.registrar(e.getNombreUsuario(), "LOGIN", "Usuario", e.entidadId(), null));

        publisher.suscribir(LoginFallidoEvent.class, e ->
                auditoria.registrar(e.getNombreUsuario(), "LOGIN_FALLIDO", "Usuario", null, null));
    }

    private String actor(DomainEvent evento) {
        return consultarUsuario.nombreUsuario(evento.actorId());
    }
}