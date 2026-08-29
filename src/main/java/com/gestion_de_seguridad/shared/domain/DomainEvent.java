package com.gestion_de_seguridad.shared.domain;

/**
 * Interfaz base de todos los eventos de dominio del sistema.
 *
 * Los eventos de dominio permiten la comunicacion entre slices de forma
 * desacoplada: cada slice publica eventos sin conocer quien los escucha.
 * Esto respeta la arquitectura hexagonal (patron Observer) y evita imports
 * directos entre slices.
 */
public interface DomainEvent {

    /**
     * Momento (epoch millis) en el que ocurrio el evento.
     */
    long ocurridoEn();

    /**
     * Identificador opcional de la entidad relacionada, util para filtrar
     * eventos (ej. el id de una visita). Puede ser null segun el evento.
     */
    default Long entidadId() {
        return null;
    }

    /**
     * Identificador del usuario que ejecuto la accion que origina el evento.
     * Puede ser null cuando la accion no tiene actor humano (ej. cierres
     * automaticos del sistema).
     */
    default Long actorId() {
        return null;
    }
}
