package com.gestion_de_seguridad.auditoria.domain.port.in;

import com.gestion_de_seguridad.auditoria.domain.model.RegistroAuditoria;

import java.util.List;

/**
 * Caso de uso de entrada: registrar eventos en la bitacora de auditoria.
 *
 * La logica de insercion vive en la capa de servicio de Java (no en triggers
 * de BD), tal como exige la especificacion en la seccion 4.2.
 */
public interface RegistrarAuditoriaUseCase {

    /**
     * Registra una accion en la bitacora.
     *
     * @param usuario  nombre del usuario que ejecuto la accion (puede ser null)
     * @param accion   nombre de la accion (ej. 'LOGIN', 'REGISTRAR_PERSONA')
     * @param entidad  tipo de entidad afectada (ej. 'Persona')
     * @param entidadId id de la entidad (puede ser null)
     * @param detalles texto libre con contexto adicional (puede ser null)
     */
    void registrar(String usuario, String accion, String entidad, Long entidadId, String detalles);

    /**
     * Consulta los ultimos registros de la bitacora (para reportes/auditoria visual).
     */
    List<RegistroAuditoria> listarRecientes(int limite);
}
