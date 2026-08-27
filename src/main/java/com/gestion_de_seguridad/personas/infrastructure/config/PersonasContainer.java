package com.gestion_de_seguridad.personas.infrastructure.config;

import com.gestion_de_seguridad.personas.application.service.GestionarPersonaService;
import com.gestion_de_seguridad.personas.domain.port.in.GestionarPersonaUseCase;
import com.gestion_de_seguridad.personas.infrastructure.adapter.out.persistence.PersonaJdbcRepository;
import com.gestion_de_seguridad.shared.infrastructure.config.SharedContainer;
import com.gestion_de_seguridad.usuarios.infrastructure.config.UsuariosContainer;

/**
 * Contenedor de dependencias del slice 'personas'.
 *
 * Cablea el repositorio JDBC, la verificacion de permisos (slice usuarios) y
 * el EventPublisher compartido (slice shared).
 */
public final class PersonasContainer {

    private static final GestionarPersonaUseCase GESTIONAR = new GestionarPersonaService(
            new PersonaJdbcRepository(),
            UsuariosContainer.verificarPermiso(),
            SharedContainer.eventPublisher());

    private PersonasContainer() {
    }

    public static GestionarPersonaUseCase gestionar() {
        return GESTIONAR;
    }
}
