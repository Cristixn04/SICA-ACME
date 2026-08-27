package com.gestion_de_seguridad.reportes.domain.port.out;

import com.gestion_de_seguridad.acceso.domain.model.Visita;
import com.gestion_de_seguridad.incidentes.domain.model.Incidente;
import com.gestion_de_seguridad.personas.domain.model.Persona;

import java.util.List;

/**
 * Puerto de salida del modulo de reportes.
 *
 * Entrega datos crudos (solo lectura) de los distintos slices para que el
 * servicio de reportes los procese con lambdas y la API Stream de Java.
 * El slice de reportes lee los modelos de dominio de otros slices, pero nunca
 * dependen de sus infraestructuras.
 */
public interface ReporteRepositoryPort {

    List<Visita> todasLasVisitas();

    List<Persona> todasLasPersonas();

    List<Incidente> todosLosIncidentes();
}
