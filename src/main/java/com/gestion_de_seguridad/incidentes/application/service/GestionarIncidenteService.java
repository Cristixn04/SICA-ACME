package com.gestion_de_seguridad.incidentes.application.service;

import com.gestion_de_seguridad.incidentes.domain.event.IncidenteReportadoEvent;
import com.gestion_de_seguridad.incidentes.domain.model.EstadoIncidente;
import com.gestion_de_seguridad.incidentes.domain.model.Incidente;
import com.gestion_de_seguridad.incidentes.domain.model.SeveridadIncidente;
import com.gestion_de_seguridad.incidentes.domain.port.in.GestionarIncidenteUseCase;
import com.gestion_de_seguridad.incidentes.domain.port.out.IncidenteRepositoryPort;
import com.gestion_de_seguridad.personas.domain.port.in.GestionarPersonaUseCase;
import com.gestion_de_seguridad.shared.domain.EntidadNoEncontradaException;
import com.gestion_de_seguridad.shared.domain.event.EventPublisher;
import com.gestion_de_seguridad.usuarios.domain.port.in.VerificarPermisoUseCase;

import java.util.List;

/**
 * Servicio de aplicacion de incidentes.
 *
 * El bloqueo de acceso de una persona se delega en el puerto de entrada del
 * slice de personas (interfaz), respetando el desacople entre slices (nunca
 * dependencia directa de una implementacion de otro slice).
 */
public class GestionarIncidenteService implements GestionarIncidenteUseCase {

    private final IncidenteRepositoryPort incidenteRepository;
    private final VerificarPermisoUseCase verificarPermiso;
    private final GestionarPersonaUseCase gestionarPersona;
    private final EventPublisher eventPublisher;

    public GestionarIncidenteService(IncidenteRepositoryPort incidenteRepository,
                                     VerificarPermisoUseCase verificarPermiso,
                                     GestionarPersonaUseCase gestionarPersona,
                                     EventPublisher eventPublisher) {
        this.incidenteRepository = incidenteRepository;
        this.verificarPermiso = verificarPermiso;
        this.gestionarPersona = gestionarPersona;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Incidente reportar(Long idUsuario, Incidente incidente) {
        verificarPermiso.verificar(idUsuario, "reportar_incidente");
        Incidente guardado = incidenteRepository.guardar(incidente);
        eventPublisher.publicar(new IncidenteReportadoEvent(
                guardado.getId(), guardado.getPersonaId(), guardado.getSeveridad()));
        return guardado;
    }

    @Override
    public void cambiarEstado(Long idUsuario, Long idIncidente, EstadoIncidente nuevoEstado) {
        verificarPermiso.verificar(idUsuario, "gestionar_incidente");
        Incidente actual = buscarPorId(idIncidente);
        incidenteRepository.actualizar(actual.cambiarEstado(nuevoEstado));
    }

    @Override
    public void bloquearAccesoPersona(Long idUsuario, Long idPersona) {
        // El permiso 'bloquear_persona' lo re-verifica el slice de personas.
        gestionarPersona.bloquear(idUsuario, idPersona);
    }

    @Override
    public List<Incidente> listar() {
        return incidenteRepository.listar();
    }

    @Override
    public List<Incidente> filtrar(EstadoIncidente estado, SeveridadIncidente severidad) {
        return incidenteRepository.filtrar(estado, severidad);
    }

    @Override
    public Incidente buscarPorId(Long id) {
        return incidenteRepository.buscarPorId(id)
                .orElseThrow(() -> new EntidadNoEncontradaException("incidente", id));
    }
}
