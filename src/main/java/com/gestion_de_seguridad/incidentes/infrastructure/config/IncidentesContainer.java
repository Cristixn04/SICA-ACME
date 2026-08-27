package com.gestion_de_seguridad.incidentes.infrastructure.config;

import com.gestion_de_seguridad.incidentes.application.service.GestionarIncidenteService;
import com.gestion_de_seguridad.incidentes.domain.port.in.GestionarIncidenteUseCase;
import com.gestion_de_seguridad.incidentes.infrastructure.adapter.out.persistence.IncidenteJdbcRepository;
import com.gestion_de_seguridad.personas.infrastructure.config.PersonasContainer;
import com.gestion_de_seguridad.shared.infrastructure.config.SharedContainer;
import com.gestion_de_seguridad.usuarios.infrastructure.config.UsuariosContainer;

/**
 * Contenedor de dependencias del slice 'incidentes'.
 *
 * Cablea el repositorio JDBC, la verificacion de permisos, el puerto de
 * entrada de personas (para el bloqueo de acceso) y el EventPublisher
 * compartido.
 */
public final class IncidentesContainer {

    private static final GestionarIncidenteUseCase GESTIONAR = new GestionarIncidenteService(
            new IncidenteJdbcRepository(),
            UsuariosContainer.verificarPermiso(),
            PersonasContainer.gestionar(),
            SharedContainer.eventPublisher());

    private IncidentesContainer() {
    }

    public static GestionarIncidenteUseCase gestionar() {
        return GESTIONAR;
    }
}
