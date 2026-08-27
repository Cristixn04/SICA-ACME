package com.gestion_de_seguridad.reportes.application.service;

import com.gestion_de_seguridad.acceso.domain.model.EstadoVisita;
import com.gestion_de_seguridad.acceso.domain.model.Visita;
import com.gestion_de_seguridad.incidentes.domain.model.EstadoIncidente;
import com.gestion_de_seguridad.incidentes.domain.model.Incidente;
import com.gestion_de_seguridad.incidentes.domain.model.SeveridadIncidente;
import com.gestion_de_seguridad.personas.domain.model.Persona;
import com.gestion_de_seguridad.reportes.domain.port.in.GenerarReporteUseCase;
import com.gestion_de_seguridad.reportes.domain.port.out.ReporteRepositoryPort;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio de aplicacion de reportes.
 *
 * Todas las metricas se calculan mediante la API Stream y lambdas de Java,
 * filtrando, contando y agrupando sobre los datos crudos provistos por el
 * puerto de salida.
 */
public class GenerarReporteService implements GenerarReporteUseCase {

    private final ReporteRepositoryPort reporteRepository;

    public GenerarReporteService(ReporteRepositoryPort reporteRepository) {
        this.reporteRepository = reporteRepository;
    }

    @Override
    public long personasEnComplejo() {
        return reporteRepository.todasLasVisitas().stream()
                .filter(v -> v.getEstado() == EstadoVisita.DENTRO
                        || v.getEstado() == EstadoVisita.CHECK_IN)
                .map(Visita::getPersonaId)
                .distinct()
                .count();
    }

    @Override
    public long visitasDelDia() {
        LocalDate hoy = LocalDate.now();
        return reporteRepository.todasLasVisitas().stream()
                .filter(v -> v.getFechaHoraVisita() != null
                        && v.getFechaHoraVisita().toLocalDate().equals(hoy))
                .count();
    }

    @Override
    public long incidentesAbiertos() {
        return reporteRepository.todosLosIncidentes().stream()
                .filter(i -> i.getEstado() == EstadoIncidente.ABIERTO)
                .count();
    }

    @Override
    public double tasaAprobacion() {
        long decididas = reporteRepository.todasLasVisitas().stream()
                .filter(v -> v.getEstado() == EstadoVisita.APROBADO
                        || v.getEstado() == EstadoVisita.RECHAZADO)
                .count();
        if (decididas == 0) {
            return 0.0;
        }
        long aprobadas = reporteRepository.todasLasVisitas().stream()
                .filter(v -> v.getEstado() == EstadoVisita.APROBADO)
                .count();
        return (double) aprobadas / decididas;
    }

    @Override
    public Map<EstadoVisita, Long> visitasPorEstado() {
        return reporteRepository.todasLasVisitas().stream()
                .collect(Collectors.groupingBy(Visita::getEstado, Collectors.counting()));
    }

    @Override
    public Map<SeveridadIncidente, Long> incidentesPorSeveridad() {
        return reporteRepository.todosLosIncidentes().stream()
                .collect(Collectors.groupingBy(Incidente::getSeveridad, Collectors.counting()));
    }

    @Override
    public List<Persona> personasActualmentoDentro() {
        List<Long> idsDentro = reporteRepository.todasLasVisitas().stream()
                .filter(v -> v.getEstado() == EstadoVisita.DENTRO
                        || v.getEstado() == EstadoVisita.CHECK_IN)
                .map(Visita::getPersonaId)
                .distinct()
                .collect(Collectors.toList());
        return reporteRepository.todasLasPersonas().stream()
                .filter(p -> idsDentro.contains(p.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Visita> visitasDelDiaDetalle() {
        LocalDate hoy = LocalDate.now();
        return reporteRepository.todasLasVisitas().stream()
                .filter(v -> v.getFechaHoraVisita() != null
                        && v.getFechaHoraVisita().toLocalDate().equals(hoy))
                .collect(Collectors.toList());
    }
}
