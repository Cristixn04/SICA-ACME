package com.gestion_de_seguridad.usuarios.domain.port.out;

import com.gestion_de_seguridad.usuarios.domain.model.Usuario;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para persistencia de usuarios.
 *
 * Lo implementan los adaptadores de infraestructura (JDBC/PostgreSQL). El
 * dominio depende de esta interfaz, no de detalles tecnologicos.
 */
public interface UsuarioRepositoryPort {

    Optional<Usuario> buscarPorNombreUsuario(String nombreUsuario);

    Optional<Usuario> buscarPorId(Long id);

    List<Usuario> listarTodos();

    Usuario guardar(Usuario usuario);
}
