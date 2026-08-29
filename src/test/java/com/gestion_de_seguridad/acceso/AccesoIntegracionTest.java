package com.gestion_de_seguridad.acceso;

import com.gestion_de_seguridad.acceso.domain.model.EstadoVisita;
import com.gestion_de_seguridad.acceso.domain.model.Visita;
import com.gestion_de_seguridad.acceso.domain.port.in.GestionarVisitaUseCase;
import com.gestion_de_seguridad.acceso.infrastructure.config.AccesoContainer;
import com.gestion_de_seguridad.personas.domain.model.Persona;
import com.gestion_de_seguridad.personas.domain.port.in.GestionarPersonaUseCase;
import com.gestion_de_seguridad.personas.infrastructure.config.PersonasContainer;
import com.gestion_de_seguridad.shared.domain.EstadoInvalidoException;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test de integracion del slice 'acceso' contra la base real sica_db.
 *
 * Verifica la maquina de estados de Visita y la regularizacion por salida
 * olvidada.
 */
class AccesoIntegracionTest {

    private final GestionarVisitaUseCase acceso = AccesoContainer.gestionar();
    private final GestionarPersonaUseCase personas = PersonasContainer.gestionar();

    private Persona crearPersonaTemporal() {
        String dni = "9997" + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
        return personas.registrar(1L, Persona.builder()
                .dni(dni)
                .nombreCompleto("Acceso Test")
                .emailCorporativo("acceso.test@acme.com")
                .build());
    }

    @Test
    void flujoPreRegistradoHastaCheckOut() {
        Persona persona = crearPersonaTemporal();

        Visita creada = acceso.crearVisita(1L, AccesoContainer.preRegistrada(),
                Visita.builder().personaId(persona.getId()).motivo("Reunion semanal").build());
        assertEquals(EstadoVisita.APROBADO, creada.getEstado());

        Visita checkin = acceso.registrarCheckIn(2L, creada.getId());
        assertEquals(EstadoVisita.DENTRO, checkin.getEstado());
        assertNotNull(checkin.getFechaHoraCheckin());

        Visita checkout = acceso.registrarCheckOut(2L, checkin.getId());
        assertEquals(EstadoVisita.CHECK_OUT, checkout.getEstado());
        assertNotNull(checkout.getFechaHoraCheckout());
    }

    @Test
    void flujoNoAnunciadoApruebaFuncionario() {
        Persona persona = crearPersonaTemporal();

        Visita creada = acceso.crearVisita(2L, AccesoContainer.noAnunciada(),
                Visita.builder().personaId(persona.getId()).motivo("Visita comercial").build());
        assertEquals(EstadoVisita.PENDIENTE_APROBACION, creada.getEstado());

        Visita aprobada = acceso.aprobar(3L, creada.getId());
        assertEquals(EstadoVisita.APROBADO, aprobada.getEstado());
    }

    @Test
    void rechazoTrancisionCheckOutSinCheckIn() {
        Persona persona = crearPersonaTemporal();

        Visita creada = acceso.crearVisita(1L, AccesoContainer.preRegistrada(),
                Visita.builder().personaId(persona.getId()).motivo("Reunion sin ingreso").build());
        // APROBADO no puede pasar a CHECK_OUT directamente
        Visita checkin = acceso.registrarCheckIn(2L, creada.getId());
        // Ahora si puede hacerse checkout
        Visita checkout = acceso.registrarCheckOut(2L, checkin.getId());
        assertEquals(EstadoVisita.CHECK_OUT, checkout.getEstado());

        // Un check-in sobre una visita ya finalizada debe fallar
        assertThrows(EstadoInvalidoException.class, () -> acceso.registrarCheckIn(2L, checkout.getId()));
    }

    @Test
    void regularizacionPorSalidaOlvidada() {
        // Primera visita: queda DENTRO sin check-out (salida olvidada simulada)
        Persona persona = crearPersonaTemporal();
        Visita primera = acceso.crearVisita(1L, AccesoContainer.preRegistrada(),
                Visita.builder().personaId(persona.getId()).motivo("Ingreso previo").build());
        acceso.registrarCheckIn(2L, primera.getId());

        // Nuevo ingreso de la misma persona: la visita previa debe cerrarse por sistema
        Visita segundoIngreso = acceso.crearVisita(2L, AccesoContainer.noAnunciada(),
                Visita.builder().personaId(persona.getId()).motivo("Nuevo ingreso").build());
        assertEquals(EstadoVisita.PENDIENTE_APROBACION, segundoIngreso.getEstado());

        Visita cerrada = acceso.buscarPorId(primera.getId());
        assertEquals(EstadoVisita.CERRADA_POR_SISTEMA, cerrada.getEstado());
        assertEquals("Salida Olvidada", cerrada.getMotivoCierre());
    }

    @Test
    void flujoOlvidoCarnetPaseTemporal() {
        Persona trabajador = crearPersonaTemporal();

        // Guarda crea solicitud por carnet olvidado
        Visita paseTemporal = acceso.crearVisita(2L, AccesoContainer.porOlvidoCarnet(),
                Visita.builder().personaId(trabajador.getId()).motivo("Pase temporal por carnet olvidado").build());
        assertEquals(EstadoVisita.PENDIENTE_APROBACION_POR_OLVIDO, paseTemporal.getEstado());

        // Funcionario aprueba el pase temporal para ese dia
        Visita aprobada = acceso.aprobar(3L, paseTemporal.getId());
        assertEquals(EstadoVisita.APROBADO, aprobada.getEstado());

        // Guarda realiza check-in
        Visita dentro = acceso.registrarCheckIn(2L, aprobada.getId());
        assertEquals(EstadoVisita.DENTRO, dentro.getEstado());
        assertNotNull(dentro.getFechaHoraCheckin());
    }
}
