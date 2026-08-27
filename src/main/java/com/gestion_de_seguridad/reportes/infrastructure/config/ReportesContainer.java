package com.gestion_de_seguridad.reportes.infrastructure.config;

import com.gestion_de_seguridad.reportes.application.service.GenerarReporteService;
import com.gestion_de_seguridad.reportes.domain.port.in.GenerarReporteUseCase;
import com.gestion_de_seguridad.reportes.infrastructure.adapter.out.persistence.ReporteJdbcRepository;

/**
 * Contenedor de dependencias del slice 'reportes'.
 *
 * Cablea el servicio con el adaptador JDBC que entrega los datos crudos.
 */
public final class ReportesContainer {

    private static final GenerarReporteUseCase GENERAR =
            new GenerarReporteService(new ReporteJdbcRepository());

    private ReportesContainer() {
    }

    public static GenerarReporteUseCase generar() {
        return GENERAR;
    }
}
