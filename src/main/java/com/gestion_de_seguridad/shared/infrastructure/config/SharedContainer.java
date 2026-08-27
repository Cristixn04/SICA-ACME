package com.gestion_de_seguridad.shared.infrastructure.config;

import com.gestion_de_seguridad.shared.domain.event.EventPublisher;

/**
 * Composition root de shared: expone el EventPublisher unico del sistema.
 *
 * Todas las slices comparten esta misma instancia, lo que habilita la
 * comunicacion por eventos de dominio entre slices (Observer) respetando el
 * desacople de la arquitectura hexagonal.
 */
public final class SharedContainer {

    private static final EventPublisher EVENT_PUBLISHER = new EventPublisher();

    private SharedContainer() {
    }

    public static EventPublisher eventPublisher() {
        return EVENT_PUBLISHER;
    }
}
