package com.gestion_de_seguridad.usuarios.domain.port.out;

import com.gestion_de_seguridad.usuarios.domain.model.Permiso;
import com.gestion_de_seguridad.usuarios.domain.model.Rol;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Puerto de salida para roles y permisos.
 *
 * Los permisos viven en base de datos y son configurables sin tocar codigo.
 */
public interface RolRepositoryPort {

    Optional<Rol> buscarPorId(Long id);

    Optional<Rol> buscarPorNombre(String nombre);

    List<Rol> listarRoles();

    /**
     * Permisos asociados a un rol por su id.
     */
    Set<Permiso> permisosDeRol(Long rolId);
}
