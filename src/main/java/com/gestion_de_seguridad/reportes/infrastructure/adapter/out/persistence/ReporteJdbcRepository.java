package com.gestion_de_seguridad.reportes.infrastructure.adapter.out.persistence;

import com.gestion_de_seguridad.acceso.domain.model.Visita;
import com.gestion_de_seguridad.acceso.domain.port.out.VisitaRepositoryPort;
import com.gestion_de_seguridad.acceso.infrastructure.adapter.out.persistence.VisitaJdbcRepository;
import com.gestion_de_seguridad.incidentes.domain.model.Incidente;
import com.gestion_de_seguridad.incidentes.domain.port.out.IncidenteRepositoryPort;
import com.gestion_de_seguridad.incidentes.infrastructure.adapter.out.persistence.IncidenteJdbcRepository;
import com.gestion_de_seguridad.personas.domain.model.Persona;
import com.gestion_de_seguridad.personas.domain.port.out.PersonaRepositoryPort;
import com.gestion_de_seguridad.personas.infrastructure.adapter.out.persistence.PersonaJdbcRepository;
import com.gestion_de_seguridad.reportes.domain.port.out.ReporteRepositoryPort;

import java.util.List;

/**
 * Adaptador JDBC/PostgreSQL del modulo de reportes.
 *
 * Obtiene los datos crudos (solo lectura) delegando en los repositorios de
 * lectura de los slices acceso, personas e incidentes. El modulo de reportes
 * depende de estas interfaces de puerto, no de implementaciones concretas de
 * los otros slices.
 */
public class ReporteJdbcRepository implements ReporteRepositoryPort {

    private final VisitaRepositoryPort visitas = new VisitaJdbcRepository();
    private final PersonaRepositoryPort personas = new PersonaJdbcRepository();
    private final IncidenteRepositoryPort incidentes = new IncidenteJdbcRepository();

    @Override
    public List<Visita> todasLasVisitas() {
        return visitas.listar();
    }

    @Override
    public List<Persona> todasLasPersonas() {
        return personas.listarTodas();
    }

    @Override
    public List<Incidente> todosLosIncidentes() {
        return incidentes.listar();
    }
}
