package com.gestion_de_seguridad.usuarios.domain.port.in;

/**
 * Caso de uso de entrada: verificar permisos de un usuario (RBAC).
 *
 * Antes de ejecutar cualquier operacion critica, el sistema verifica si el
 * rol del usuario autenticado posee el permiso requerido. Si no lo posee, se
 * lanza PermisoDenegadoException.
 */
public interface VerificarPermisoUseCase {

    /**
     * Verifica que el usuario posea el permiso indicado.
     *
     * @param idUsuario  id del usuario autenticado
     * @param codigoPermiso codigo del permiso requerido (ej. 'crear_visita')
     * @throws com.gestion_de_seguridad.shared.domain.PermisoDenegadoException
     *         si el rol del usuario no tiene el permiso.
     */
    void verificar(Long idUsuario, String codigoPermiso);

    /**
     * Verifica sin lanzar excepcion, devolviendo boolean.
     */
    boolean tienePermiso(Long idUsuario, String codigoPermiso);
}
