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
        // bloquea el nuevo ingreso).
        Long personaId = datos.getPersonaId();
        if (personaId != null) {
            visitaRepository.ultimaVisitaActivaDePersona(personaId)
                    .filter(activa -> !activa.getId().equals(datos.getId()))
                    .ifPresent(activa -> {
                        Visita cerrada = activa.cerrarPorSistema(MOTIVO_SALIDA_OLVIDADA);
                        visitaRepository.actualizar(cerrada);
                        eventPublisher.publicar(
                                new VisitaCerradaPorSistemaEvent(cerrada.getId(), MOTIVO_SALIDA_OLVIDADA));
                    });
        }

        Visita aCrear = Visita.builder()
                .personaId(datos.getPersonaId())
                .empresaPropietariaId(datos.getEmpresaPropietariaId())
                .personaVisitadaId(datos.getPersonaVisitadaId())
                .motivo(datos.getMotivo())
                .fechaHoraVisita(datos.getFechaHoraVisita())
                .estado(estrategia.estadoInicial())
                .build();

        Visita guardada = visitaRepository.guardar(aCrear);
        eventPublisher.publicar(new VisitaCreadaEvent(
                guardada.getId(), guardada.getPersonaId(), guardada.getEstado(), estrategia.nombreFlujo()));
        return guardada;
    }

    @Override
    public Visita aprobar(Long idFuncionario, Long idVisita) {
        verificarPermiso.verificar(idFuncionario, "aprobar_visita");
        Visita actual = buscarPorId(idVisita);
        Visita aprobada = actual.aprobar();
        visitaRepository.actualizar(aprobada);
        eventPublisher.publicar(new VisitaAprobadaEvent(aprobada.getId()));
        return aprobada;
    }

    @Override
    public Visita rechazar(Long idFuncionario, Long idVisita) {
        verificarPermiso.verificar(idFuncionario, "rechazar_visita");
        Visita actual = buscarPorId(idVisita);
        Visita rechazada = actual.rechazar();
        visitaRepository.actualizar(rechazada);
        eventPublisher.publicar(new VisitaRechazadaEvent(rechazada.getId()));
        return rechazada;
    }

    @Override
    public Visita registrarCheckIn(Long idGuarda, Long idVisita) {
        verificarPermiso.verificar(idGuarda, "registrar_checkin");
        Visita actual = buscarPorId(idVisita);
        Visita checkin = actual.realizarCheckIn(idGuarda);
        Visita dentro = checkin.confirmarDentro();
        visitaRepository.actualizar(dentro);
        eventPublisher.publicar(new CheckInRealizadoEvent(dentro.getId()));
        return dentro;
    }

    @Override
    public Visita registrarCheckOut(Long idGuarda, Long idVisita) {
        verificarPermiso.verificar(idGuarda, "registrar_checkout");
        Visita actual = buscarPorId(idVisita);
        Visita checkout = actual.realizarCheckOut();
        visitaRepository.actualizar(checkout);
        eventPublisher.publicar(new CheckOutRealizadoEvent(checkout.getId()));
        return checkout;
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
}
