package com.gestion_de_seguridad.personas;

import com.gestion_de_seguridad.personas.domain.model.EstadoPersona;
import com.gestion_de_seguridad.personas.domain.model.Persona;
import com.gestion_de_seguridad.personas.domain.port.in.GestionarPersonaUseCase;
import com.gestion_de_seguridad.personas.infrastructure.config.PersonasContainer;
import com.gestion_de_seguridad.shared.domain.EntidadNoEncontradaException;
import com.gestion_de_seguridad.shared.domain.ValidacionException;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test de integracion del slice 'personas' contra la base real sica_db.
 *
 * El DNI de prueba usa un prefijo 9999 y un sufijo aleatorio para que cada
 * ejecucion sea independiente y no colisione con el seed.
 */
class PersonasIntegracionTest {

    private final GestionarPersonaUseCase gestionar = PersonasContainer.gestionar();

    @Test
    void registroBusquedaYBloqueo() {
        String dni = "9999" + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));

        Persona registrada = gestionar.registrar(1L, Persona.builder()
                .dni(dni)
                .nombreCompleto("Test Persona")
                .puesto("QA")
                .departamento("TI")
                .emailCorporativo("test.persona@acme.com")
                .build());
        assertNotNull(registrada.getId());

        Persona encontrada = gestionar.buscarPorDni(dni);
        assertEquals("Test Persona", encontrada.getNombreCompleto());

        gestionar.bloquear(1L, registrada.getId());
        Persona bloqueada = gestionar.buscarPorDni(dni);
        assertEquals(EstadoPersona.INACTIVO, bloqueada.getEstado());

        gestionar.activar(1L, registrada.getId());
        Persona reactivada = gestionar.buscarPorDni(dni);
        assertEquals(EstadoPersona.ACTIVO, reactivada.getEstado());
    }

    @Test
    void rojoDuplicadoInfringeReglaDeUnicidad() {
        limpiarPrueba("99999999");
        // DNI 99999999 fijo (no usado por el seed).
        gestionar.registrar(1L, Persona.builder()
                .dni("99999999").nombreCompleto("Primero").build());
        assertThrows(ValidacionException.class, () -> gestionar.registrar(1L, Persona.builder()
                .dni("99999999").nombreCompleto("Segundo").build()));
    }

    @Test
    void rechazaDNIInvalido() {
        assertThrows(ValidacionException.class, () ->
                Persona.builder().dni("abc").nombreCompleto("Invalido").build());
    }

    @Test
    void buscaPersonaInexistenteLanzaExcepcionClara() {
        assertThrows(EntidadNoEncontradaException.class,
                () -> gestionar.buscarPorDni("99990000"));
    }

    @Test
    void listaContieneDatosDelSeed() {
        assertTrue(gestionar.buscar("González").stream()
                .anyMatch(p -> p.getNombreCompleto().startsWith("María")));
    }

    private void limpiarPrueba(String dni) {
        try (java.sql.Connection c = com.gestion_de_seguridad.shared.infrastructure.persistence
                .PostgresDataSource.obtenerConexion();
             java.sql.PreparedStatement ps = c.prepareStatement("DELETE FROM persona WHERE dni = ?")) {
            ps.setString(1, dni);
            ps.executeUpdate();
        } catch (java.sql.SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
