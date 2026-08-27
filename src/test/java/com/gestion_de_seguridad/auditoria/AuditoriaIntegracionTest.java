package com.gestion_de_seguridad.auditoria;

import com.gestion_de_seguridad.acceso.domain.model.Visita;
import com.gestion_de_seguridad.acceso.domain.port.in.GestionarVisitaUseCase;
import com.gestion_de_seguridad.acceso.infrastructure.config.AccesoContainer;
import com.gestion_de_seguridad.auditoria.domain.model.RegistroAuditoria;
import com.gestion_de_seguridad.auditoria.domain.port.in.RegistrarAuditoriaUseCase;
import com.gestion_de_seguridad.auditoria.infrastructure.config.AuditoriaContainer;
import com.gestion_de_seguridad.personas.domain.model.Persona;
import com.gestion_de_seguridad.personas.domain.port.in.GestionarPersonaUseCase;
import com.gestion_de_seguridad.personas.infrastructure.config.PersonasContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test de integracion del slice 'auditoria' contra la base real.
 *
 * Verifica que el listener (Observer) registra en la bitacora los eventos de
 * los otros slices sin que estos conozcan a auditoria.
 */
class AuditoriaIntegracionTest {

    @BeforeAll
    static void iniciar() {
        // Fuerza la carga de AuditoriaContainer para registrar los listeners.
        AuditoriaContainer.usar();
    }

    @Test
    void registraEventoDeVisitaCreadaEnBitacora() {
        RegistrarAuditoriaUseCase auditoria = AuditoriaContainer.usar();
        GestionarPersonaUseCase personas = PersonasContainer.gestionar();
        GestionarVisitaUseCase acceso = AccesoContainer.gestionar();

        String dni = "9996" + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
        Persona persona = personas.registrar(1L, Persona.builder()
                .dni(dni).nombreCompleto("Auditoria Test")
                .emailCorporativo("aud.test@acme.com").build());

        Visita visita = acceso.crearVisita(1L, AccesoContainer.preRegistrada(),
                Visita.builder().personaId(persona.getId()).motivo("Reunion de auditoria de eventos").build());

        List<RegistroAuditoria> recientes = auditoria.listarRecientes(50);
        assertTrue(recientes.stream().anyMatch(r ->
                "CREAR_VISITA".equals(r.getAccion()) && r.getEntidadId().equals(visita.getId())));
        // El registro de persona tambien debe haberse registrado por el listener
        assertTrue(recientes.stream().anyMatch(r -> "REGISTRAR_PERSONA".equals(r.getAccion())));
    }
}
