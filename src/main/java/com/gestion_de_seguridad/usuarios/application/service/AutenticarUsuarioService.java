package com.gestion_de_seguridad.usuarios.application.service;

import com.gestion_de_seguridad.usuarios.domain.event.LoginExitosoEvent;
import com.gestion_de_seguridad.usuarios.domain.event.LoginFallidoEvent;
import com.gestion_de_seguridad.usuarios.domain.model.Usuario;
import com.gestion_de_seguridad.usuarios.domain.port.in.AutenticarUsuarioUseCase;
import com.gestion_de_seguridad.usuarios.domain.port.out.UsuarioRepositoryPort;
import com.gestion_de_seguridad.shared.domain.event.EventPublisher;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

/**
 * Implementacion del caso de uso de autenticacion.
 *
 * Aplica el principio de Responsabilidad Unica: este servicio solo se ocupa de
 * autenticar. La verificacion del hash de contrasena se realiza con BCrypt.
 * Publica eventos de dominio (LOGIN_OK / LOGIN_FALLIDO) para que el slice de
 * auditoria los registre sin acoplar a ambos slices.
 */
public class AutenticarUsuarioService implements AutenticarUsuarioUseCase {

    /**
     * Hash BCrypt ficticio. Cuando el usuario no existe (o esta inactivo) se
     * verifica la clave contra este hash para que el tiempo de respuesta no
     * revele si el usuario existe (mitiga ataques de timing / enumeracion).
     */
    private static final String HASH_DUMMY =
            "$2a$10$c6GZARr01JIHNGmIVv7xROt3vRcjEI3WSmoCusso6mPunif/YCnnq";

    private final UsuarioRepositoryPort usuarioRepository;
    private final EventPublisher eventPublisher;

    public AutenticarUsuarioService(UsuarioRepositoryPort usuarioRepository,
                                    EventPublisher eventPublisher) {
        this.usuarioRepository = usuarioRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Usuario autenticar(String nombreUsuario, String contrasena) {
        if (nombreUsuario == null || contrasena == null) {
            return null;
        }
        String usuarioNormalizado = nombreUsuario.trim();
        Optional<Usuario> usuario = usuarioRepository.buscarPorNombreUsuario(usuarioNormalizado);
        if (usuario.isEmpty()) {
            BCrypt.checkpw(contrasena, HASH_DUMMY);
            eventPublisher.publicar(new LoginFallidoEvent(usuarioNormalizado));
            return null;
        }
        Usuario u = usuario.get();
        if (!u.isActivo()) {
            BCrypt.checkpw(contrasena, HASH_DUMMY);
            eventPublisher.publicar(new LoginFallidoEvent(usuarioNormalizado));
            return null;
        }
        if (!BCrypt.checkpw(contrasena, u.getPasswordHash())) {
            eventPublisher.publicar(new LoginFallidoEvent(usuarioNormalizado));
            return null;
        }
        eventPublisher.publicar(new LoginExitosoEvent(u.getId(), u.getNombreUsuario()));
        return u;
    }

    @Override
    public boolean usuarioActivo(Long idUsuario) {
        return usuarioRepository.buscarPorId(idUsuario)
                .map(Usuario::isActivo)
                .orElse(false);
    }
}
