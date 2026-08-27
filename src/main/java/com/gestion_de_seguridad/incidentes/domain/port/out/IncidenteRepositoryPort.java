package com.gestion_de_seguridad.incidentes.domain.port.out;

import com.gestion_de_seguridad.incidentes.domain.model.EstadoIncidente;
import com.gestion_de_seguridad.incidentes.domain.model.Incidente;
import com.gestion_de_seguridad.incidentes.domain.model.SeveridadIncidente;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para persistencia de incidentes.
 */
public interface IncidenteRepositoryPort {

    Incidente guardar(Incidente incidente);

    void actualizar(Incidente incidente);

    Optional<Incidente> buscarPorId(Long id);

    List<Incidente> listar();

    /**
     * Filtra incidentes por estado y/o severidad (cualquiera puede ser null
     * para no filtrar ese criterio).
     */
    List<Incidente> filtrar(EstadoIncidente estado, SeveridadIncidente severidad);
}
