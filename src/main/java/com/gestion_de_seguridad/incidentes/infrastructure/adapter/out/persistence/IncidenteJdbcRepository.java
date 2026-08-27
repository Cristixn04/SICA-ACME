package com.gestion_de_seguridad.incidentes.infrastructure.adapter.out.persistence;

import com.gestion_de_seguridad.incidentes.domain.model.EstadoIncidente;
import com.gestion_de_seguridad.incidentes.domain.model.Incidente;
import com.gestion_de_seguridad.incidentes.domain.model.SeveridadIncidente;
import com.gestion_de_seguridad.incidentes.domain.port.out.IncidenteRepositoryPort;
import com.gestion_de_seguridad.shared.infrastructure.persistence.PostgresDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador JDBC/PostgreSQL del repositorio de incidentes.
 *
 * Usa prepared statements y cast de enums de PostgreSQL (::severidad_incidente,
 * ::estado_incidente).
 */
public class IncidenteJdbcRepository implements IncidenteRepositoryPort {

    private static final String COLUMNAS =
            "id, persona_id, tipo, descripcion, severidad, estado, fecha_registro";

    @Override
    public Incidente guardar(Incidente incidente) {
        String sql = "INSERT INTO incidente (persona_id, tipo, descripcion, severidad, estado, fecha_registro) " +
                "VALUES (?, ?, ?, ?::severidad_incidente, ?::estado_incidente, ?)";
        try (Connection c = PostgresDataSource.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, incidente.getPersonaId());
            ps.setString(2, incidente.getTipo());
            ps.setString(3, incidente.getDescripcion());
            ps.setString(4, incidente.getSeveridad().name());
            ps.setString(5, incidente.getEstado().name());
            ps.setTimestamp(6, Timestamp.valueOf(incidente.getFechaRegistro()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return new Incidente(keys.getLong(1), incidente.getPersonaId(),
                            incidente.getTipo(), incidente.getDescripcion(),
                            incidente.getSeveridad(), incidente.getEstado(),
                            incidente.getFechaRegistro());
                }
            }
            return incidente;
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar incidente.", e);
        }
    }

    @Override
    public void actualizar(Incidente incidente) {
        String sql = "UPDATE incidente SET estado=?::estado_incidente, descripcion=? WHERE id=?";
        try (Connection c = PostgresDataSource.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, incidente.getEstado().name());
            ps.setString(2, incidente.getDescripcion());
            ps.setLong(3, incidente.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar incidente.", e);
        }
    }

    @Override
    public Optional<Incidente> buscarPorId(Long id) {
        String sql = "SELECT " + COLUMNAS + " FROM incidente WHERE id = ?";
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
            throw new RuntimeException("Error al buscar incidente por id.", e);
        }
    }

    @Override
    public List<Incidente> listar() {
        String sql = "SELECT " + COLUMNAS + " FROM incidente ORDER BY id DESC";
        return ejecutarLista(sql, List.of());
    }

    @Override
    public List<Incidente> filtrar(EstadoIncidente estado, SeveridadIncidente severidad) {
        StringBuilder sql = new StringBuilder("SELECT " + COLUMNAS + " FROM incidente WHERE 1=1 ");
        List<Object> parametros = new ArrayList<>();
        if (estado != null) {
            sql.append("AND estado = ?::estado_incidente ");
            parametros.add(estado.name());
        }
        if (severidad != null) {
            sql.append("AND severidad = ?::severidad_incidente ");
            parametros.add(severidad.name());
        }
        sql.append("ORDER BY id DESC");
        return ejecutarLista(sql.toString(), parametros);
    }

    private List<Incidente> ejecutarLista(String sql, List<Object> parametros) {
        List<Incidente> lista = new ArrayList<>();
        try (Connection c = PostgresDataSource.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapper(rs));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar incidentes.", e);
        }
    }

    private Incidente mapper(ResultSet rs) throws SQLException {
        return new Incidente(
                rs.getLong("id"),
                rs.getLong("persona_id"),
                rs.getString("tipo"),
                rs.getString("descripcion"),
                SeveridadIncidente.valueOf(rs.getString("severidad")),
                EstadoIncidente.valueOf(rs.getString("estado")),
                rs.getTimestamp("fecha_registro").toLocalDateTime());
    }
}
