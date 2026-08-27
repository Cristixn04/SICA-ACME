package com.gestion_de_seguridad.auditoria.infrastructure.adapter.out.persistence;

import com.gestion_de_seguridad.auditoria.domain.model.RegistroAuditoria;
import com.gestion_de_seguridad.auditoria.domain.port.out.AuditoriaRepositoryPort;
import com.gestion_de_seguridad.shared.infrastructure.persistence.PostgresDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador JDBC/PostgreSQL de la bitacora de auditoria.
 *
 * Solo realiza INSERT y consultas de lectura (SELECT). Nunca UPDATE ni DELETE,
 * preservando la inmutabilidad del registro de auditoria.
 */
public class AuditoriaJdbcRepository implements AuditoriaRepositoryPort {

    @Override
    public RegistroAuditoria guardar(RegistroAuditoria registro) {
        String sql = "INSERT INTO bitacora_auditoria (fecha_hora, usuario, accion, entidad, " +
                "entidad_id, detalles) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection c = PostgresDataSource.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setTimestamp(1, Timestamp.valueOf(registro.getFechaHora()));
            ps.setString(2, registro.getUsuario());
            ps.setString(3, registro.getAccion());
            ps.setString(4, registro.getEntidad());
            if (registro.getEntidadId() != null) {
                ps.setLong(5, registro.getEntidadId());
            } else {
                ps.setNull(5, java.sql.Types.BIGINT);
            }
            ps.setString(6, registro.getDetalles());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return new RegistroAuditoria(keys.getLong(1), registro.getFechaHora(),
                            registro.getUsuario(), registro.getAccion(), registro.getEntidad(),
                            registro.getEntidadId(), registro.getDetalles());
                }
            }
            return registro;
        } catch (SQLException e) {
            throw new RuntimeException("Error al registrar en la bitacora de auditoria.", e);
        }
    }

    @Override
    public List<RegistroAuditoria> listarRecientes(int limite) {
        String sql = "SELECT id, fecha_hora, usuario, accion, entidad, entidad_id, detalles " +
                "FROM bitacora_auditoria ORDER BY id DESC LIMIT ?";
        List<RegistroAuditoria> lista = new ArrayList<>();
        try (Connection c = PostgresDataSource.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Long entidadId = rs.getLong("entidad_id");
                    if (rs.wasNull()) {
                        entidadId = null;
                    }
                    lista.add(new RegistroAuditoria(
                            rs.getLong("id"),
                            rs.getTimestamp("fecha_hora").toLocalDateTime(),
                            rs.getString("usuario"),
                            rs.getString("accion"),
                            rs.getString("entidad"),
                            entidadId,
                            rs.getString("detalles")));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar registros de auditoria.", e);
        }
    }
}
