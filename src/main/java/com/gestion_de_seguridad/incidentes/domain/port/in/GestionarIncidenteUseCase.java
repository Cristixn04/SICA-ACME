package com.gestion_de_seguridad.incidentes.domain.port.in;

import com.gestion_de_seguridad.incidentes.domain.model.EstadoIncidente;
import com.gestion_de_seguridad.incidentes.domain.model.Incidente;
import com.gestion_de_seguridad.incidentes.domain.model.SeveridadIncidente;

import java.util.List;

/**
 * Caso de uso de entrada: gestion de incidentes de seguridad.
 *
 * Incluye el registro de incidentes y la capacidad de marcar a una persona con
 * restriccion de acceso de forma inmediata (seccion 4.5).
 */
public interface GestionarIncidenteUseCase {

    /**
     * Reporta un incidente (permiso 'reportar_incidente').
     */
    Incidente reportar(Long idUsuario, Incidente incidente);

    /**
     * Cambia el estado de un incidente (permiso 'gestionar_incidente').
     */
    void cambiarEstado(Long idUsuario, Long idIncidente, EstadoIncidente nuevoEstado);

    /**
     * Bloquea el acceso de una persona de forma inmediata y queda asociado al
     * incidente (permiso 'bloquear_persona'). Delega en el slice de personas
     * mediante su puerto de entrada, sin acoplarse a su implementacion.
     */
    void bloquearAccesoPersona(Long idUsuario, Long idPersona);

    List<Incidente> listar();

    List<Incidente> filtrar(EstadoIncidente estado, SeveridadIncidente severidad);

    Incidente buscarPorId(Long id);
}
