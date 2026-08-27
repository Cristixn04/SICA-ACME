package com.gestion_de_seguridad.usuarios.infrastructure.config;

import com.gestion_de_seguridad.usuarios.application.service.AutenticarUsuarioService;
import com.gestion_de_seguridad.usuarios.application.service.VerificarPermisoService;
import com.gestion_de_seguridad.usuarios.domain.port.in.AutenticarUsuarioUseCase;
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
            new AutenticarUsuarioService(USUARIO_REPO);

    private static final VerificarPermisoUseCase VERIFICAR =
            new VerificarPermisoService(USUARIO_REPO, ROL_REPO);

    private UsuariosContainer() {
    }

    public static AutenticarUsuarioUseCase autenticar() {
        return AUTENTICAR;
    }

    public static VerificarPermisoUseCase verificarPermiso() {
        return VERIFICAR;
    }
}
