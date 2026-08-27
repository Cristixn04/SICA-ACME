package com.gestion_de_seguridad.auditoria.application.service;

import com.gestion_de_seguridad.auditoria.domain.model.RegistroAuditoria;
import com.gestion_de_seguridad.auditoria.domain.port.in.RegistrarAuditoriaUseCase;
import com.gestion_de_seguridad.auditoria.domain.port.out.AuditoriaRepositoryPort;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio de aplicacion de auditoria.
 *
 * La insercion de registros se realiza aqui (capa de servicio Java), no en
 * triggers de base de datos. Garantiza que toda accion critica quede
 * registrada de forma inmutable.
 */
public class RegistrarAuditoriaService implements RegistrarAuditoriaUseCase {

    private final AuditoriaRepositoryPort auditoriaRepository;

    public RegistrarAuditoriaService(AuditoriaRepositoryPort auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    @Override
    public void registrar(String usuario, String accion, String entidad, Long entidadId, String detalles) {
        RegistroAuditoria registro = new RegistroAuditoria(
                null, LocalDateTime.now(), usuario, accion, entidad, entidadId, detalles);
        auditoriaRepository.guardar(registro);
    }

    @Override
    public List<RegistroAuditoria> listarRecientes(int limite) {
        return auditoriaRepository.listarRecientes(limite);
    }
}
