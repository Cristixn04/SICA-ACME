package com.gestion_de_seguridad.acceso.domain.model;

/**
 * Interfaz del patron Strategy: define el estado inicial de una visita segun
 * su tipo de flujo de ingreso.
 *
 * Cada estrategia concreta representa una regla de negocio de la seccion 2:
 * pre-registrada, no anunciada, o por olvido de carnet.
 */
public interface EstrategiaCreacionVisita {

    /**
     * Estado inicial que toma una visita creada con esta estrategia.
     */
    EstadoVisita estadoInicial();

    /**
     * Nombre legible del flujo (para fines de auditoria).
     */
    String nombreFlujo();
}
