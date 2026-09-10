package com.gestion_de_seguridad.acceso.application.service;

import com.gestion_de_seguridad.acceso.domain.event.CheckInRealizadoEvent;
import com.gestion_de_seguridad.acceso.domain.event.CheckOutRealizadoEvent;
import com.gestion_de_seguridad.acceso.domain.event.VisitaAprobadaEvent;
import com.gestion_de_seguridad.acceso.domain.event.VisitaCerradaPorSistemaEvent;
import com.gestion_de_seguridad.acceso.domain.event.VisitaCreadaEvent;
import com.gestion_de_seguridad.acceso.domain.event.VisitaRechazadaEvent;
import com.gestion_de_seguridad.acceso.domain.model.EstrategiaCreacionVisita;
import com.gestion_de_seguridad.acceso.domain.model.Visita;
import com.gestion_de_seguridad.acceso.domain.port.in.GestionarVisitaUseCase;
import com.gestion_de_seguridad.acceso.domain.port.out.VisitaRepositoryPort;
import com.gestion_de_seguridad.shared.domain.EntidadNoEncontradaException;
import com.gestion_de_seguridad.shared.domain.ValidacionException;
import com.gestion_de_seguridad.shared.domain.event.EventPublisher;
import com.gestion_de_seguridad.usuarios.domain.port.in.VerificarPermisoUseCase;

import java.util.List;

/**
 * Servicio de aplicacion de control de acceso (visitas).
 *
 * Implementa la maquina de estados de la Visita y aplica las reglas de
 * negocio de la seccion 2:
 * - Creacion por tipo de flujo (patron Strategy).
 * - Aprobacion/rechazo de visitas no anunciadas.
 * - Check-in / check-out.
 * - Regularizacion de "salida olvidada" (CERRADA_POR_SISTEMA), que se
 *   detecta en el proximo ingreso de la misma persona sin bloquearlo.
 */
public class GestionarVisitaService implements GestionarVisitaUseCase {

    private static final String MOTIVO_SALIDA_OLVIDADA = "Salida Olvidada";

    private final VisitaRepositoryPort visitaRepository;
    private final VerificarPermisoUseCase verificarPermiso;
    private final EventPublisher eventPublisher;

    public GestionarVisitaService(VisitaRepositoryPort visitaRepository,
                                  VerificarPermisoUseCase verificarPermiso,
                                  EventPublisher eventPublisher) {
        this.visitaRepository = visitaRepository;
        this.verificarPermiso = verificarPermiso;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Visita crearVisita(Long idUsuario, EstrategiaCreacionVisita estrategia, Visita datos) {
        verificarPermiso.verificar(idUsuario, "crear_visita");
        if (estrategia == null) {
            throw new ValidacionException("Debe especificarse el tipo de flujo de la visita.");
        }

        // Regularizacion por salida olvidada (seccion 2.4): si la persona
        // tiene una visita previa aun activa, se cierra por sistema (no se
        // bloquea el nuevo ingreso). El cierre y el alta de la nueva visita se
        // persisten de forma atomica (una unica transaccion).
        Visita anteriorActiva = null;
        Long personaId = datos.getPersonaId();
        if (personaId != null) {
            anteriorActiva = visitaRepository.ultimaVisitaActivaDePersona(personaId)
                    .filter(activa -> !activa.getId().equals(datos.getId()))
                    .orElse(null);
        }

        Visita aCrear = Visita.builder()
                .personaId(datos.getPersonaId())
                .empresaPropietariaId(datos.getEmpresaPropietariaId())
                .personaVisitadaId(datos.getPersonaVisitadaId())
                .motivo(datos.getMotivo())
                .fechaHoraVisita(datos.getFechaHoraVisita())
                .estado(estrategia.estadoInicial())
                .build();

        Visita guardada = visitaRepository.guardarRegularizando(
                anteriorActiva == null ? null : anteriorActiva.cerrarPorSistema(MOTIVO_SALIDA_OLVIDADA),
                aCrear);

        if (anteriorActiva != null) {
            eventPublisher.publicar(new VisitaCerradaPorSistemaEvent(
                    anteriorActiva.getId(), MOTIVO_SALIDA_OLVIDADA, idUsuario));
        }
        eventPublisher.publicar(new VisitaCreadaEvent(
                guardada.getId(), guardada.getPersonaId(), guardada.getEstado(),
                estrategia.nombreFlujo(), idUsuario));
        return guardada;
    }

    @Override
    public Visita aprobar(Long idFuncionario, Long idVisita) {
        verificarPermiso.verificar(idFuncionario, "aprobar_visita");
        Visita actual = buscarPorId(idVisita);
        Visita aprobada = actual.aprobar();
        visitaRepository.actualizar(aprobada);
        eventPublisher.publicar(new VisitaAprobadaEvent(aprobada.getId(), idFuncionario));
        return aprobada;
    }

    @Override
    public Visita rechazar(Long idFuncionario, Long idVisita) {
        verificarPermiso.verificar(idFuncionario, "rechazar_visita");
        Visita actual = buscarPorId(idVisita);
        Visita rechazada = actual.rechazar();
        visitaRepository.actualizar(rechazada);
        eventPublisher.publicar(new VisitaRechazadaEvent(rechazada.getId(), idFuncionario));
        return rechazada;
    }

    @Override
    public Visita registrarCheckIn(Long idGuarda, Long idVisita) {
        verificarPermiso.verificar(idGuarda, "registrar_checkin");
        Visita actual = buscarPorId(idVisita);

        
        try {
            var persona = com.gestion_de_seguridad.personas.infrastructure.config.PersonasContainer.gestionar().buscarPorId(actual.getPersonaId());
            if (persona != null && persona.getEstado() != com.gestion_de_seguridad.personas.domain.model.EstadoPersona.ACTIVO) {
                throw new com.gestion_de_seguridad.shared.domain.EstadoInvalidoException(
                        "Acceso Denegado: La persona '" + persona.getNombreCompleto() + "' se encuentra en estado " + persona.getEstado() + ".");
            }
        } catch (com.gestion_de_seguridad.shared.domain.EstadoInvalidoException e) {
            throw e;
        } catch (Exception ignored) {
        }

        Visita checkin = actual.realizarCheckIn(idGuarda);
        Visita dentro = checkin.confirmarDentro();
        visitaRepository.actualizar(dentro);
        eventPublisher.publicar(new CheckInRealizadoEvent(dentro.getId(), idGuarda));
        return dentro;
    }

    @Override
    public Visita registrarCheckOut(Long idGuarda, Long idVisita) {
        verificarPermiso.verificar(idGuarda, "registrar_checkout");
        Visita actual = buscarPorId(idVisita);
        Visita checkout = actual.realizarCheckOut();
        visitaRepository.actualizar(checkout);
        eventPublisher.publicar(new CheckOutRealizadoEvent(checkout.getId(), idGuarda));
        return checkout;
    }

    
    @Override
    public Visita cancelar(Long idUsuario, Long idVisita) {
        // Puede cancelar quien tenga permiso de rechazar o crear visitas
        try {
            verificarPermiso.verificar(idUsuario, "rechazar_visita");
        } catch (Exception e) {
            verificarPermiso.verificar(idUsuario, "crear_visita");
        }
        Visita actual = buscarPorId(idVisita);
        if (actual.getEstado() == com.gestion_de_seguridad.acceso.domain.model.EstadoVisita.DENTRO
                || actual.getEstado() == com.gestion_de_seguridad.acceso.domain.model.EstadoVisita.CHECK_OUT
                || actual.getEstado() == com.gestion_de_seguridad.acceso.domain.model.EstadoVisita.CERRADA_POR_SISTEMA) {
            throw new ValidacionException("No se puede cancelar una visita que ya ingresó o finalizó.");
        }
        Visita cancelada = actual.rechazar();
        visitaRepository.actualizar(cancelada);
        eventPublisher.publicar(new VisitaRechazadaEvent(cancelada.getId(), idUsuario));
        return cancelada;
    }

    @Override
    public List<Visita> buscarPorTexto(String texto) {
        return visitaRepository.buscarPorTexto(texto);
    }

    @Override
    public Visita buscarPorId(Long idVisita) {
        return visitaRepository.buscarPorId(idVisita)
                .orElseThrow(() -> new EntidadNoEncontradaException("visita", idVisita));
    }

    @Override
    public List<Visita> listarTodas() {
        return visitaRepository.listar();
    }
}
