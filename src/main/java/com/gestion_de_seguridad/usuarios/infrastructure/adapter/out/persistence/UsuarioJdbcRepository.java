package com.gestion_de_seguridad.usuarios.infrastructure.adapter.out.persistence;

import com.gestion_de_seguridad.usuarios.domain.model.Rol;
import com.gestion_de_seguridad.usuarios.domain.model.Usuario;
import com.gestion_de_seguridad.usuarios.domain.port.out.UsuarioRepositoryPort;
import com.gestion_de_seguridad.shared.infrastructure.persistence.PostgresDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador JDBC/PostgreSQL del repositorio de usuarios.
 *
 * Todo el acceso a la base de datos usa prepared statements para evitar
 * inyeccion SQL.
 */
public class UsuarioJdbcRepository implements UsuarioRepositoryPort {

    private static final String SELECT_BASE =
            "SELECT u.id, u.nombre_usuario, u.password_hash, u.persona_id, u.activo, " +
            "r.id AS rol_id, r.nombre AS rol_nombre " +
            "FROM usuario u JOIN rol r ON r.id = u.rol_id ";

    @Override
    public Optional<Usuario> buscarPorNombreUsuario(String nombreUsuario) {
        String sql = SELECT_BASE + "WHERE u.nombre_usuario = ?";
        try (Connection c = PostgresDataSource.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nombreUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapper(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar usuario por nombre.", e);
        }
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        String sql = SELECT_BASE + "WHERE u.id = ?";
        try (Connection c = PostgresDataSource.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapper(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar usuario por id.", e);
        }
    }

    @Override
    public List<Usuario> listarTodos() {
        String sql = SELECT_BASE + "ORDER BY u.nombre_usuario";
        List<Usuario> lista = new ArrayList<>();
        try (Connection c = PostgresDataSource.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapper(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar usuarios.", e);
        }
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        String sql = "INSERT INTO usuario (nombre_usuario, password_hash, rol_id, persona_id, activo) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection c = PostgresDataSource.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, usuario.getNombreUsuario());
            ps.setString(2, usuario.getPasswordHash());
            ps.setLong(3, usuario.getRol().getId());
            if (usuario.getPersonaId() != null) {
                ps.setLong(4, usuario.getPersonaId());
            } else {
                ps.setNull(4, java.sql.Types.BIGINT);
            }
            ps.setBoolean(5, usuario.isActivo());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return new Usuario(keys.getLong(1), usuario.getNombreUsuario(),
                            usuario.getPasswordHash(), usuario.getRol(),
                            usuario.getPersonaId(), usuario.isActivo());
                }
            }
            return usuario;
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar usuario.", e);
        }
    }

    private Usuario mapper(ResultSet rs) throws SQLException {
        Rol rol = new Rol(rs.getLong("rol_id"), rs.getString("rol_nombre"));
        Long personaId = rs.getLong("persona_id");
        if (rs.wasNull()) {
            personaId = null;
        }
        return new Usuario(
                rs.getLong("id"),
                rs.getString("nombre_usuario"),
                rs.getString("password_hash"),
                rol,
                personaId,
                rs.getBoolean("activo"));
    }
}
