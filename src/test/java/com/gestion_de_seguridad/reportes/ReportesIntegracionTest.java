package com.gestion_de_seguridad.reportes;

import com.gestion_de_seguridad.acceso.domain.model.EstadoVisita;
import com.gestion_de_seguridad.acceso.domain.model.Visita;
import com.gestion_de_seguridad.acceso.domain.port.in.GestionarVisitaUseCase;
import com.gestion_de_seguridad.acceso.infrastructure.config.AccesoContainer;
import com.gestion_de_seguridad.incidentes.domain.model.EstadoIncidente;
import com.gestion_de_seguridad.incidentes.domain.model.Incidente;
import com.gestion_de_seguridad.incidentes.domain.model.SeveridadIncidente;
import com.gestion_de_seguridad.incidentes.domain.port.in.GestionarIncidenteUseCase;
import com.gestion_de_seguridad.incidentes.infrastructure.config.IncidentesContainer;
import com.gestion_de_seguridad.personas.domain.model.Persona;
import com.gestion_de_seguridad.personas.domain.port.in.GestionarPersonaUseCase;
import com.gestion_de_seguridad.personas.infrastructure.config.PersonasContainer;
import com.gestion_de_seguridad.reportes.domain.port.in.GenerarReporteUseCase;
import com.gestion_de_seguridad.reportes.infrastructure.config.ReportesContainer;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test de integracion del slice 'reportes' contra la base real sica_db.
 *
 * Verifica el uso de la API Stream para agrupar y contar sobre las metricas.
 */
class ReportesIntegracionTest {

    private final GestionarVisitaUseCase acceso = AccesoContainer.gestionar();
    private final GestionarIncidenteUseCase incidentes = IncidentesContainer.gestionar();
    private final GestionarPersonaUseCase personas = PersonasContainer.gestionar();
    private final GenerarReporteUseCase reportes = ReportesContainer.generar();

    private Persona nuevaPersona() {
        String dni = "9994" + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
        return personas.registrar(1L, Persona.builder()
                .dni(dni).nombreCompleto("Reporte Test")
                .emailCorporativo("rep.test@acme.com").build());
    }

    @Test
    void cuentaPersonasDentroLuegoDeUnCheckIn() {
        long antes = reportes.personasEnComplejo();

        Persona persona = nuevaPersona();
        Visita visita = acceso.crearVisita(1L, AccesoContainer.preRegistrada(),
                Visita.builder().personaId(persona.getId()).motivo("Ingreso para reporte").build());
        acceso.registrarCheckIn(2L, visita.getId());

        assertTrue(reportes.personasEnComplejo() >= antes + 1);
    }

    @Test
    void agrupaVisitasPorEstado() {
        var porEstado = reportes.visitasPorEstado();
        // El seed tiene al menos una visita en estado DENTRO o APROBADO.
        assertTrue(porEstado.values().stream().mapToLong(Long::longValue).sum() >= 2);
    }

    @Test
    void agrupaIncidentesPorSeveridad() {
        var porSeveridad = reportes.incidentesPorSeveridad();
        assertTrue(porSeveridad.getOrDefault(SeveridadIncidente.ALTO, 0L) >= 1);
    }

    @Test
    void cuentaIncidentesAbiertos() {
        // El seed tiene al menos un incidente ABIERTO.
        assertTrue(reportes.incidentesAbiertos() >= 1);
    }

    @Test
    void tasaAprobacionEstaEntreCeroYUno() {
        double tasa = reportes.tasaAprobacion();
        assertTrue(tasa >= 0.0 && tasa <= 1.0);
    }

    // =========================================================================
    // [EXAMEN - TEST FUNCION 5: Analítica de Departamento Más Visitado]
    // =========================================================================
    @Test
    void departamentoMasVisitadoRetornaValorValido() {
        String depto = reportes.departamentoMasVisitado();
        assertNotNull(depto);
        assertTrue(!depto.isBlank());
    }
}
