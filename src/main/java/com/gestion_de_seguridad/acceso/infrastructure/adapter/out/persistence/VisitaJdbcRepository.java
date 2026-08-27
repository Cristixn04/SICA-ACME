package com.gestion_de_seguridad.acceso.infrastructure.adapter.out.persistence;

import com.gestion_de_seguridad.acceso.domain.model.EstadoVisita;
import com.gestion_de_seguridad.acceso.domain.model.Visita;
import com.gestion_de_seguridad.acceso.domain.port.out.VisitaRepositoryPort;
import com.gestion_de_seguridad.shared.infrastructure.persistence.PostgresDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador JDBC/PostgreSQL del repositorio de visitas.
 *
 * Usa prepared statements. Las busquedas por texto se resuelven con un JOIN a
 * la tabla persona (DNI/nombre) sin acoplar el dominio de acceso al dominio
 * de personas: solo accede a datos via SQL.
 */
public class VisitaJdbcRepository implements VisitaRepositoryPort {

    private static final String COLUMNAS =
            "v.id, v.persona_id, v.empresa_propietaria_id, v.persona_visitada_id, v.motivo, " +
            "v.fecha_hora_visita, v.estado, v.fecha_hora_checkin, v.fecha_hora_checkout, " +
            "v.guarda_id, v.motivo_cierre";

    @Override
    public Visita guardar(Visita visita) {
        String sql = "INSERT INTO visita (persona_id, empresa_propietaria_id, persona_visitada_id, " +
                "motivo, fecha_hora_visita, estado) VALUES (?, ?, ?, ?, ?, ?::estado_visita)";
        try (Connection c = PostgresDataSource.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, visita.getPersonaId());
            setLong(ps, 2, visita.getEmpresaPropietariaId());
            setLong(ps, 3, visita.getPersonaVisitadaId());
            ps.setString(4, visita.getMotivo());
            setTimestamp(ps, 5, visita.getFechaHoraVisita());
            ps.setString(6, visita.getEstado().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return reconstruirConId(keys.getLong(1), visita);
                }
            }
            return visita;
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar visita.", e);
        }
    }

    @Override
    public void actualizar(Visita visita) {
        String sql = "UPDATE visita SET estado=?::estado_visita, fecha_hora_checkin=?, " +
                "fecha_hora_checkout=?, guarda_id=?, motivo_cierre=? WHERE id=?";
        try (Connection c = PostgresDataSource.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, visita.getEstado().name());
            setTimestamp(ps, 2, visita.getFechaHoraCheckin());
            setTimestamp(ps, 3, visita.getFechaHoraCheckout());
            setLong(ps, 4, visita.getGuardaId());
            ps.setString(5, visita.getMotivoCierre());
            ps.setLong(6, visita.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar visita.", e);
        }
    }

    @Override
    public Optional<Visita> buscarPorId(Long id) {
        String sql = "SELECT " + COLUMNAS + " FROM visita v WHERE v.id = ?";
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
            throw new RuntimeException("Error al buscar visita por id.", e);
        }
    }

    @Override
    public List<Visita> buscarPorTexto(String texto) {
        String sql = "SELECT " + COLUMNAS + " FROM visita v " +
                "JOIN persona p ON p.id = v.persona_id " +
                "WHERE p.dni ILIKE ? OR p.nombre_completo ILIKE ? " +
                "ORDER BY v.id DESC";
        String patron = "%" + texto + "%";
        List<Visita> lista = new ArrayList<>();
        try (Connection c = PostgresDataSource.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, patron);
            ps.setString(2, patron);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapper(rs));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar visitas por texto.", e);
        }
    }

    @Override
    public Optional<Visita> ultimaVisitaActivaDePersona(Long personaId) {
        String sql = "SELECT " + COLUMNAS + " FROM visita v " +
                "WHERE v.persona_id = ? AND v.estado IN ('CHECK_IN','DENTRO') " +
                "ORDER BY v.id DESC LIMIT 1";
        try (Connection c = PostgresDataSource.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, personaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapper(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar visita activa de persona.", e);
        }
    }

    @Override
    public List<Visita> listar() {
        String sql = "SELECT " + COLUMNAS + " FROM visita v ORDER BY v.id DESC";
        List<Visita> lista = new ArrayList<>();
        try (Connection c = PostgresDataSource.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapper(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar visitas.", e);
        }
    }

    private Visita reconstruirConId(Long id, Visita original) {
        return Visita.builder()
                .id(id)
                .personaId(original.getPersonaId())
                .empresaPropietariaId(original.getEmpresaPropietariaId())
                .personaVisitadaId(original.getPersonaVisitadaId())
                .motivo(original.getMotivo())
                .fechaHoraVisita(original.getFechaHoraVisita())
                .estado(original.getEstado())
                .build();
    }

    private Visita mapper(ResultSet rs) throws SQLException {
        return Visita.builder()
                .id(rs.getLong("id"))
                .personaId(rs.getLong("persona_id"))
                .empresaPropietariaId(rs.getLong("empresa_propietaria_id") == 0
                        ? null : rs.getLong("empresa_propietaria_id"))
                .personaVisitadaId(rs.getLong("persona_visitada_id") == 0
                        ? null : rs.getLong("persona_visitada_id"))
                .motivo(rs.getString("motivo"))
                .fechaHoraVisita(rs.getTimestamp("fecha_hora_visita") == null
                        ? null : rs.getTimestamp("fecha_hora_visita").toLocalDateTime())
                .estado(EstadoVisita.valueOf(rs.getString("estado")))
                .fechaHoraCheckin(rs.getTimestamp("fecha_hora_checkin") == null
                        ? null : rs.getTimestamp("fecha_hora_checkin").toLocalDateTime())
                .fechaHoraCheckout(rs.getTimestamp("fecha_hora_checkout") == null
                        ? null : rs.getTimestamp("fecha_hora_checkout").toLocalDateTime())
                .guardaId(rs.getLong("guarda_id") == 0 ? null : rs.getLong("guarda_id"))
                .motivoCierre(rs.getString("motivo_cierre"))
                .build();
    }

    private void setLong(PreparedStatement ps, int idx, Long valor) throws SQLException {
        if (valor == null) {
            ps.setNull(idx, java.sql.Types.BIGINT);
        } else {
            ps.setLong(idx, valor);
        }
    }

    private void setTimestamp(PreparedStatement ps, int idx, LocalDateTime valor) throws SQLException {
        if (valor == null) {
            ps.setNull(idx, java.sql.Types.TIMESTAMP);
        } else {
            ps.setTimestamp(idx, Timestamp.valueOf(valor));
        }
    }
}
