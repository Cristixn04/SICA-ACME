package com.gestion_de_seguridad.usuarios.application.service;

import com.gestion_de_seguridad.shared.domain.PermisoDenegadoException;
import com.gestion_de_seguridad.usuarios.domain.model.Usuario;
import com.gestion_de_seguridad.usuarios.domain.port.in.VerificarPermisoUseCase;
import com.gestion_de_seguridad.usuarios.domain.port.out.RolRepositoryPort;
import com.gestion_de_seguridad.usuarios.domain.port.out.UsuarioRepositoryPort;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementacion de la verificacion de permisos (RBAC).
 *
 * Los permisos de cada rol se cargan desde la base de datos (RolRepositoryPort),
 * por lo que son configurables sin tocar codigo, tal como exige la especificacion.
 */
public class VerificarPermisoService implements VerificarPermisoUseCase {

    private final UsuarioRepositoryPort usuarioRepository;
    private final RolRepositoryPort rolRepository;

    public VerificarPermisoService(UsuarioRepositoryPort usuarioRepository,
                                   RolRepositoryPort rolRepository) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
    }

    @Override
    public void verificar(Long idUsuario, String codigoPermiso) {
        if (!tienePermiso(idUsuario, codigoPermiso)) {
            throw new PermisoDenegadoException(codigoPermiso);
        }
    }

    @Override
    public boolean tienePermiso(Long idUsuario, String codigoPermiso) {
        Optional<Usuario> usuario = usuarioRepository.buscarPorId(idUsuario);
        if (usuario.isEmpty() || !usuario.get().isActivo()) {
            return false;
        }
        Long rolId = usuario.get().getRol().getId();
        Set<String> permisos = rolRepository.permisosDeRol(rolId).stream()
                .map(p -> p.getCodigo())
                .collect(Collectors.toSet());
        return permisos.contains(codigoPermiso);
    }
}
