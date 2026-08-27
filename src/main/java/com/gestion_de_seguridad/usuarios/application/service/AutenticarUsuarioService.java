package com.gestion_de_seguridad.usuarios.application.service;

import com.gestion_de_seguridad.usuarios.domain.model.Usuario;
import com.gestion_de_seguridad.usuarios.domain.port.in.AutenticarUsuarioUseCase;
import com.gestion_de_seguridad.usuarios.domain.port.out.UsuarioRepositoryPort;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

/**
 * Implementacion del caso de uso de autenticacion.
 *
 * Aplica el principio de Responsabilidad Unica: este servicio solo se ocupa de
 * autenticar. La verificacion del hash de contrasena se realiza con BCrypt.
 */
public class AutenticarUsuarioService implements AutenticarUsuarioUseCase {

    private final UsuarioRepositoryPort usuarioRepository;

    public AutenticarUsuarioService(UsuarioRepositoryPort usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Usuario autenticar(String nombreUsuario, String contrasena) {
        if (nombreUsuario == null || contrasena == null) {
            return null;
        }
        Optional<Usuario> usuario = usuarioRepository.buscarPorNombreUsuario(nombreUsuario.trim());
        if (usuario.isEmpty()) {
            return null;
        }
        Usuario u = usuario.get();
        if (!u.isActivo()) {
            return null;
        }
        if (!BCrypt.checkpw(contrasena, u.getPasswordHash())) {
            return null;
        }
        return u;
    }

    @Override
    public boolean usuarioActivo(Long idUsuario) {
        return usuarioRepository.buscarPorId(idUsuario)
                .map(Usuario::isActivo)
                .orElse(false);
    }
}
