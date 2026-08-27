package com.gestion_de_seguridad.incidentes;

import com.gestion_de_seguridad.incidentes.domain.model.EstadoIncidente;
import com.gestion_de_seguridad.incidentes.domain.model.Incidente;
import com.gestion_de_seguridad.incidentes.domain.model.SeveridadIncidente;
import com.gestion_de_seguridad.incidentes.domain.port.in.GestionarIncidenteUseCase;
import com.gestion_de_seguridad.incidentes.infrastructure.config.IncidentesContainer;
import com.gestion_de_seguridad.personas.domain.model.EstadoPersona;
import com.gestion_de_seguridad.personas.domain.model.Persona;
import com.gestion_de_seguridad.personas.domain.port.in.GestionarPersonaUseCase;
import com.gestion_de_seguridad.personas.infrastructure.config.PersonasContainer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test de integracion del slice 'incidentes' contra la base real sica_db.
 */
class IncidentesIntegracionTest {

    private final GestionarIncidenteUseCase incidentes = IncidentesContainer.gestionar();
    private final GestionarPersonaUseCase personas = PersonasContainer.gestionar();

    private Persona crearPersonaTemporal() {
        String dni = "9995" + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
        return personas.registrar(1L, Persona.builder()
                .dni(dni).nombreCompleto("Incidente Test")
                .emailCorporativo("inc.test@acme.com").build());
    }

    @Test
    void reportaIncidentesYFiltraPorSeveridad() {
        Persona persona = crearPersonaTemporal();
        Incidente i1 = incidentes.reportar(2L, new Incidente(null, persona.getId(),
                "Acceso Fallido", "Credenciales invalidas", SeveridadIncidente.ALTO,
                EstadoIncidente.ABIERTO, null));

        List<Incidente> altos = incidentes.filtrar(null, SeveridadIncidente.ALTO);
        assertTrue(altos.stream().anyMatch(i -> i.getId().equals(i1.getId())));
        assertEquals(EstadoIncidente.ABIERTO, i1.getEstado());
    }

    @Test
    void cambiaEstadoDeIncidente() {
        Persona persona = crearPersonaTemporal();
        Incidente i = incidentes.reportar(2L, new Incidente(null, persona.getId(),
                "Alarma", "Alarma activada", SeveridadIncidente.MEDIO,
                EstadoIncidente.ABIERTO, null));

        incidentes.cambiarEstado(1L, i.getId(), EstadoIncidente.EN_PROGRESO);
        assertEquals(EstadoIncidente.EN_PROGRESO, incidentes.buscarPorId(i.getId()).getEstado());
    }

    @Test
    void bloqueoAccesoPersonaLaDejaInactiva() {
        Persona persona = crearPersonaTemporal();
        incidentes.bloquearAccesoPersona(2L, persona.getId());
        Persona bloqueada = personas.buscarPorDni(persona.getDni());
        assertEquals(EstadoPersona.INACTIVO, bloqueada.getEstado());
    }
}
