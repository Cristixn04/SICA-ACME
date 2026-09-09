package com.gestion_de_seguridad.reportes.domain.port.in;

import com.gestion_de_seguridad.acceso.domain.model.EstadoVisita;
import com.gestion_de_seguridad.acceso.domain.model.Visita;
import com.gestion_de_seguridad.incidentes.domain.model.SeveridadIncidente;
import com.gestion_de_seguridad.personas.domain.model.Persona;

import java.util.List;
import java.util.Map;

/**
 * Caso de uso de entrada: generacion de reportes y metricas.
 *
 * El calculo de todas las metricas se realiza con la API Stream y lambdas de
 * Java, tal como exige la especificacion (seccion 4.6).
 */
public interface GenerarReporteUseCase {

    /**
     * Cantidad de personas actualmente dentro del complejo (estado DENTRO o CHECK_IN).
     */
    long personasEnComplejo();

    /**
     * Cantidad de visitas registradas en el dia de hoy.
     */
    long visitasDelDia();

    /**
     * Cantidad de incidentes en estado ABIERTO.
     */
    long incidentesAbiertos();

    /**
     * Proporcion de visitas aprobadas sobre el total de visitas decididas (0..1).
     */
    double tasaAprobacion();

    /**
     * Conteo de visitas agrupadas por estado.
     */
    Map<EstadoVisita, Long> visitasPorEstado();

    /**
     * Conteo de incidentes agrupados por severidad.
     */
    Map<SeveridadIncidente, Long> incidentesPorSeveridad();

    /**
     * Detalle de las personas actualmente dentro del complejo.
     */
    List<Persona> personasActualmentoDentro();

    /**
     * Detalle de las visitas del dia de hoy.
     */
    List<Visita> visitasDelDiaDetalle();

    // =========================================================================
    // [EXAMEN - FUNCION 5: Analítica de Departamento Más Frecuente con Stream API]
    // =========================================================================
    /**
     * Retorna el nombre del departamento con mayor cantidad de visitas registradas.
     * Si no hay visitas registradas o no hay datos, retorna "Ninguno".
     */
    String departamentoMasVisitado();
}
