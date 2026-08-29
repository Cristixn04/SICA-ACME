package com.gestion_de_seguridad.personas.application.service;

import com.gestion_de_seguridad.personas.domain.event.PersonaBloqueadaEvent;
import com.gestion_de_seguridad.personas.domain.event.PersonaRegistradaEvent;
import com.gestion_de_seguridad.personas.domain.model.EstadoPersona;
import com.gestion_de_seguridad.personas.domain.model.Persona;
import com.gestion_de_seguridad.personas.domain.port.in.GestionarPersonaUseCase;
import com.gestion_de_seguridad.personas.domain.port.out.PersonaRepositoryPort;
import com.gestion_de_seguridad.shared.domain.EntidadNoEncontradaException;
import com.gestion_de_seguridad.shared.domain.event.EventPublisher;
import com.gestion_de_seguridad.usuarios.domain.port.in.VerificarPermisoUseCase;

import java.util.List;

/**
 * Servicio de aplicacion de personas.
 *
 * Verifica permisos antes de cada operacion critica y publica eventos de
 * dominio para que el slice de auditoria registre la trazabilidad sin que
 * este servicio conozca a auditoria (desacople entre slices).
 */
public class GestionarPersonaService implements GestionarPersonaUseCase {

    private final PersonaRepositoryPort personaRepository;
    private final VerificarPermisoUseCase verificarPermiso;
    private final EventPublisher eventPublisher;

    public GestionarPersonaService(PersonaRepositoryPort personaRepository,
                                   VerificarPermisoUseCase verificarPermiso,
                                   EventPublisher eventPublisher) {
        this.personaRepository = personaRepository;
        this.verificarPermiso = verificarPermiso;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Persona registrar(Long idUsuario, Persona persona) {
        verificarPermiso.verificar(idUsuario, "registrar_persona");
        Persona guardada = personaRepository.guardar(persona);
        eventPublisher.publicar(new PersonaRegistradaEvent(guardada.getId(), guardada.getDni(), idUsuario));
        return guardada;
    }

    @Override
    public Persona buscarPorDni(String dni) {
        return personaRepository.buscarPorDni(dni)
                .orElseThrow(() -> new EntidadNoEncontradaException("persona con DNI", dni));
    }

    @Override
    public List<Persona> buscar(String texto) {
        return personaRepository.buscar(texto);
    }

    @Override
    public List<Persona> listarTodas() {
        return personaRepository.listarTodas();
    }

    @Override
    public Persona buscarPorId(Long idPersona) {
        return personaRepository.buscarPorId(idPersona).orElse(null);
    }

    @Override
    public void actualizar(Long idUsuario, Persona persona) {
        verificarPermiso.verificar(idUsuario, "editar_persona");
        if (persona.getId() == null) {
            throw new EntidadNoEncontradaException("persona con id null");
        }
        personaRepository.actualizar(persona);
    }

    @Override
    public void bloquear(Long idUsuario, Long idPersona) {
        verificarPermiso.verificar(idUsuario, "bloquear_persona");
        personaRepository.cambiarEstado(idPersona, EstadoPersona.INACTIVO);
        eventPublisher.publicar(new PersonaBloqueadaEvent(idPersona, idUsuario));
    }

    @Override
    public void activar(Long idUsuario, Long idPersona) {
        verificarPermiso.verificar(idUsuario, "editar_persona");
        personaRepository.cambiarEstado(idPersona, EstadoPersona.ACTIVO);
    }
}
