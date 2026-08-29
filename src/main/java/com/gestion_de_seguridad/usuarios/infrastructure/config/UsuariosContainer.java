package com.gestion_de_seguridad.usuarios.infrastructure.config;

import com.gestion_de_seguridad.shared.infrastructure.config.SharedContainer;
import com.gestion_de_seguridad.usuarios.application.service.AutenticarUsuarioService;
import com.gestion_de_seguridad.usuarios.application.service.ConsultarUsuarioService;
import com.gestion_de_seguridad.usuarios.application.service.VerificarPermisoService;
import com.gestion_de_seguridad.usuarios.domain.port.in.AutenticarUsuarioUseCase;
import com.gestion_de_seguridad.usuarios.domain.port.in.ConsultarUsuarioUseCase;
import com.gestion_de_seguridad.usuarios.domain.port.in.VerificarPermisoUseCase;
import com.gestion_de_seguridad.usuarios.infrastructure.adapter.out.persistence.RolJdbcRepository;
import com.gestion_de_seguridad.usuarios.infrastructure.adapter.out.persistence.UsuarioJdbcRepository;

/**
 * Contenedor simple de dependencias del slice 'usuarios'.
 *
 * Expone los casos de uso (puertos de entrada) correctamente cableados con sus
 * adaptadores JDBC. Actua como composition root para que la UI (u otros
 * componentes) no conozca detalles de construccion de objetos.
 */
public final class UsuariosContainer {

    private static final UsuarioJdbcRepository USUARIO_REPO = new UsuarioJdbcRepository();
    private static final RolJdbcRepository ROL_REPO = new RolJdbcRepository();

    private static final AutenticarUsuarioUseCase AUTENTICAR =
            new AutenticarUsuarioService(USUARIO_REPO, SharedContainer.eventPublisher());

    private static final VerificarPermisoUseCase VERIFICAR =
            new VerificarPermisoService(USUARIO_REPO, ROL_REPO);

    private static final ConsultarUsuarioUseCase CONSULTAR =
            new ConsultarUsuarioService(USUARIO_REPO, ROL_REPO, VERIFICAR);

    private UsuariosContainer() {
    }

    public static AutenticarUsuarioUseCase autenticar() {
        return AUTENTICAR;
    }

    public static VerificarPermisoUseCase verificarPermiso() {
        return VERIFICAR;
    }

    public static ConsultarUsuarioUseCase consultar() {
        return CONSULTAR;
    }
}
