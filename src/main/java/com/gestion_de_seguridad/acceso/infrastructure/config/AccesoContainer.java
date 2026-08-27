package com.gestion_de_seguridad.acceso.infrastructure.config;

import com.gestion_de_seguridad.acceso.application.service.GestionarVisitaService;
import com.gestion_de_seguridad.acceso.domain.port.in.GestionarVisitaUseCase;
import com.gestion_de_seguridad.acceso.infrastructure.adapter.out.persistence.VisitaJdbcRepository;
import com.gestion_de_seguridad.acceso.domain.model.EstrategiaCreacionVisita;
import com.gestion_de_seguridad.acceso.domain.model.EstrategiaVisitaNoAnunciada;
import com.gestion_de_seguridad.acceso.domain.model.EstrategiaVisitaPorOlvidoCarnet;
import com.gestion_de_seguridad.acceso.domain.model.EstrategiaVisitaPreRegistrada;
import com.gestion_de_seguridad.shared.infrastructure.config.SharedContainer;
import com.gestion_de_seguridad.usuarios.infrastructure.config.UsuariosContainer;

/**
 * Contenedor de dependencias del slice 'acceso'.
 *
 * Tambien expone las estrategias de creacion de visita (patron Strategy) para
 * que la UI las seleccione segun el flujo de ingreso.
 */
public final class AccesoContainer {

    private static final GestionarVisitaUseCase GESTIONAR = new GestionarVisitaService(
            new VisitaJdbcRepository(),
            UsuariosContainer.verificarPermiso(),
            SharedContainer.eventPublisher());

    private AccesoContainer() {
    }

    public static GestionarVisitaUseCase gestionar() {
        return GESTIONAR;
    }

    public static EstrategiaCreacionVisita preRegistrada() {
        return new EstrategiaVisitaPreRegistrada();
    }

    public static EstrategiaCreacionVisita noAnunciada() {
        return new EstrategiaVisitaNoAnunciada();
    }

    public static EstrategiaCreacionVisita porOlvidoCarnet() {
        return new EstrategiaVisitaPorOlvidoCarnet();
    }
}
