package com.gestion_de_seguridad.usuarios.infrastructure.adapter.out.persistence;

import com.gestion_de_seguridad.usuarios.domain.model.Permiso;
import com.gestion_de_seguridad.usuarios.domain.model.Rol;
import com.gestion_de_seguridad.usuarios.domain.port.out.RolRepositoryPort;
import com.gestion_de_seguridad.shared.infrastructure.persistence.PostgresDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Adaptador JDBC/PostgreSQL del repositorio de roles y permisos.
 *
 * Los permisos se leen desde la tabla 'permiso' unida a 'rol_permiso', por lo
 * que son totalmente configurables en la base de datos.
 */
public class RolJdbcRepository implements RolRepositoryPort {

    @Override
    public Optional<Rol> buscarPorId(Long id) {
        String sql = "SELECT id, nombre FROM rol WHERE id = ?";
        try (Connection c = PostgresDataSource.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Rol(rs.getLong("id"), rs.getString("nombre")));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar rol por id.", e);
        }
    }

    @Override
    public Optional<Rol> buscarPorNombre(String nombre) {
        String sql = "SELECT id, nombre FROM rol WHERE nombre = ?";
        try (Connection c = PostgresDataSource.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Rol(rs.getLong("id"), rs.getString("nombre")));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar rol por nombre.", e);
        }
    }

    @Override
    public List<Rol> listarRoles() {
        String sql = "SELECT id, nombre FROM rol ORDER BY id";
        List<Rol> lista = new ArrayList<>();
        try (Connection c = PostgresDataSource.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Rol(rs.getLong("id"), rs.getString("nombre")));
            }
            return lista;
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar roles.", e);
        }
    }

    @Override
    public Set<Permiso> permisosDeRol(Long rolId) {
        String sql = "SELECT p.id, p.codigo, p.modulo FROM permiso p " +
                     "JOIN rol_permiso rp ON rp.permiso_id = p.id " +
                     "WHERE rp.rol_id = ? ORDER BY p.modulo, p.codigo";
        Set<Permiso> set = new HashSet<>();
        try (Connection c = PostgresDataSource.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, rolId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    set.add(new Permiso(rs.getLong("id"), rs.getString("codigo"), rs.getString("modulo")));
                }
            }
            return set;
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar permisos del rol.", e);
        }
    }
}
