package com.gestion_de_seguridad.auditoria.domain.port.out;

import com.gestion_de_seguridad.auditoria.domain.model.RegistroAuditoria;

import java.util.List;

/**
 * Puerto de salida de la bitacora de auditoria.
 *
 * Solo expone operaciones de insercion y consulta (lectura). La bitacora es
 * inmutable: no existe operacion de actualizacion ni borrado desde la capa de
 * aplicacion.
 */
public interface AuditoriaRepositoryPort {

    RegistroAuditoria guardar(RegistroAuditoria registro);

    /**
     * Consulta (solo lectura) los ultimos N registros, mas recientes primero.
     */
    List<RegistroAuditoria> listarRecientes(int limite);
}
