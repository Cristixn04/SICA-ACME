package com.gestion_de_seguridad.auditoria.infrastructure.config;

import com.gestion_de_seguridad.auditoria.application.service.RegistrarAuditoriaService;
import com.gestion_de_seguridad.auditoria.domain.port.in.RegistrarAuditoriaUseCase;
import com.gestion_de_seguridad.auditoria.infrastructure.adapter.in.AuditoriaEventListener;
import com.gestion_de_seguridad.auditoria.infrastructure.adapter.out.persistence.AuditoriaJdbcRepository;
import com.gestion_de_seguridad.shared.infrastructure.config.SharedContainer;
import com.gestion_de_seguridad.usuarios.domain.port.in.ConsultarUsuarioUseCase;
import com.gestion_de_seguridad.usuarios.infrastructure.config.UsuariosContainer;

/**
 * Contenedor de dependencias del slice 'auditoria'.
 *
 * Al inicializarse, registra el listener en el EventPublisher compartido para
 * que la auditoria quede suscrita a los eventos de los demas slices. Debe
 * inicializarse en el arranque de la aplicacion (App.inicializarContenedores)
 * para que la bitacora funcione en produccion.
 */
public final class AuditoriaContainer {

    private static final AuditoriaJdbcRepository REPO = new AuditoriaJdbcRepository();
    private static final RegistrarAuditoriaUseCase AUDITORIA = new RegistrarAuditoriaService(REPO);

    static {
        ConsultarUsuarioUseCase consultar = UsuariosContainer.consultar();
        new AuditoriaEventListener(AUDITORIA, consultar).registrar(SharedContainer.eventPublisher());
    }

    private AuditoriaContainer() {
    }

    public static RegistrarAuditoriaUseCase usar() {
        return AUDITORIA;
    }
}
