package com.gestion_de_seguridad.usuarios.application.service;

import com.gestion_de_seguridad.shared.domain.EntidadNoEncontradaException;
import com.gestion_de_seguridad.shared.domain.ValidacionException;
import com.gestion_de_seguridad.usuarios.domain.model.Rol;
import com.gestion_de_seguridad.usuarios.domain.model.Usuario;
import com.gestion_de_seguridad.usuarios.domain.port.in.ConsultarUsuarioUseCase;
import com.gestion_de_seguridad.usuarios.domain.port.in.VerificarPermisoUseCase;
import com.gestion_de_seguridad.usuarios.domain.port.out.RolRepositoryPort;
import com.gestion_de_seguridad.usuarios.domain.port.out.UsuarioRepositoryPort;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

/**
 * Implementación del caso de uso de consulta y gestión de usuarios.
 */
public class ConsultarUsuarioService implements ConsultarUsuarioUseCase {

    private final UsuarioRepositoryPort usuarioRepository;
    private final RolRepositoryPort rolRepository;
    private final VerificarPermisoUseCase verificarPermiso;

    public ConsultarUsuarioService(UsuarioRepositoryPort usuarioRepository,
                                   RolRepositoryPort rolRepository,
                                   VerificarPermisoUseCase verificarPermiso) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.verificarPermiso = verificarPermiso;
    }

    @Override
    public String nombreUsuario(Long idUsuario) {
        if (idUsuario == null) {
            return null;
        }
        return usuarioRepository.buscarPorId(idUsuario)
                .map(Usuario::getNombreUsuario)
                .orElse(null);
    }

    @Override
    public List<Usuario> listarTodos() {
        return usuarioRepository.listarTodos();
    }

    @Override
    public Usuario registrarUsuario(Long idOperador, String nombreUsuario, String passwordPlano, String nombreRol) {
        if (idOperador != null) {
            verificarPermiso.verificar(idOperador, "crear_usuario");
        }
        if (nombreUsuario == null || nombreUsuario.isBlank()) {
            throw new ValidacionException("El nombre de usuario es obligatorio.");
        }
        if (passwordPlano == null || passwordPlano.length() < 4) {
            throw new ValidacionException("La contraseña debe tener al menos 4 caracteres.");
        }

        Rol rol = rolRepository.buscarPorNombre(nombreRol)
                .orElseThrow(() -> new EntidadNoEncontradaException("rol", nombreRol));

        String hash = BCrypt.hashpw(passwordPlano, BCrypt.gensalt(10));
        Usuario nuevo = new Usuario(null, nombreUsuario.trim(), hash, rol, null, true);
        return usuarioRepository.guardar(nuevo);
    }
}