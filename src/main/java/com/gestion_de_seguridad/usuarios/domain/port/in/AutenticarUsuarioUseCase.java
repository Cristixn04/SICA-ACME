package com.gestion_de_seguridad.usuarios.domain.port.in;

import com.gestion_de_seguridad.usuarios.domain.model.Usuario;

/**
 * Caso de uso de entrada: autenticar un usuario.
 *
 * El dominio no expone la contrasena; la capa de aplicacion verifica el hash.
 */
public interface AutenticarUsuarioUseCase {

    /**
     * Autentica un usuario por nombre de usuario y contrasena.
     *
     * @return el Usuario autenticado, o null si las credenciales son invalidas
     *         o el usuario esta inactivo.
     */
    Usuario autenticar(String nombreUsuario, String contrasena);

    /**
     * Valida que un usuario (por id) exista y este activo.
     */
    boolean usuarioActivo(Long idUsuario);
}
