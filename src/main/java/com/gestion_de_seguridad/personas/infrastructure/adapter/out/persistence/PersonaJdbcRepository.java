package com.gestion_de_seguridad.personas.infrastructure.adapter.out.persistence;

import com.gestion_de_seguridad.personas.domain.model.EstadoPersona;
import com.gestion_de_seguridad.personas.domain.model.Persona;
import com.gestion_de_seguridad.personas.domain.port.out.PersonaRepositoryPort;
import com.gestion_de_seguridad.shared.domain.ValidacionException;
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
 * Adaptador JDBC/PostgreSQL del repositorio de personas.
 *
 * Usa prepared statements y traduce violaciones de unicidad (DNI repetido) a
 * un mensaje claro de negocio.
 */
public class PersonaJdbcRepository implements PersonaRepositoryPort {

    private static final String COLUMNAS =
            "id, dni, nombre_completo, puesto, departamento, email_corporativo, foto_url, estado, empresa_id";

    @Override
    public Optional<Persona> buscarPorId(Long id) {
        String sql = "SELECT " + COLUMNAS + " FROM persona WHERE id = ?";
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
            throw new RuntimeException("Error al buscar persona por id.", e);
        }
    }

    @Override
    public Optional<Persona> buscarPorDni(String dni) {
        String sql = "SELECT " + COLUMNAS + " FROM persona WHERE dni = ?";
        try (Connection c = PostgresDataSource.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, dni);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapper(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar persona por DNI.", e);
        }
    }

    @Override
    public List<Persona> buscar(String texto) {
        String sql = "SELECT " + COLUMNAS + " FROM persona " +
                "WHERE dni ILIKE ? OR nombre_completo ILIKE ? OR departamento ILIKE ? " +
                "ORDER BY nombre_completo";
        String patron = "%" + texto + "%";
        List<Persona> lista = new ArrayList<>();
        try (Connection c = PostgresDataSource.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, patron);
            ps.setString(2, patron);
            ps.setString(3, patron);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapper(rs));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar personas.", e);
        }
    }

    @Override
    public List<Persona> listarTodas() {
        String sql = "SELECT " + COLUMNAS + " FROM persona ORDER BY nombre_completo";
        List<Persona> lista = new ArrayList<>();
        try (Connection c = PostgresDataSource.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapper(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar personas.", e);
        }
    }

    @Override
    public Persona guardar(Persona persona) {
        String sql = "INSERT INTO persona (dni, nombre_completo, puesto, departamento, " +
                "email_corporativo, foto_url, estado, empresa_id) VALUES (?, ?, ?, ?, ?, ?, ?::estado_persona, ?)";
        try (Connection c = PostgresDataSource.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, persona.getDni());
            ps.setString(2, persona.getNombreCompleto());
            setNullable(ps, 3, persona.getPuesto());
            setNullable(ps, 4, persona.getDepartamento());
            setNullable(ps, 5, persona.getEmailCorporativo());
            setNullable(ps, 6, persona.getFotoUrl());
            ps.setString(7, persona.getEstado().name());
            setNullableLong(ps, 8, persona.getEmpresaId());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return crearConId(keys.getLong(1), persona);
                }
            }
            return persona;
        } catch (SQLException e) {
            if (esViolacionUnicidad(e)) {
                throw new ValidacionException("Ya existe una persona registrada con ese DNI.");
            }
            throw new RuntimeException("Error al guardar persona.", e);
        }
    }

    @Override
    public void actualizar(Persona persona) {
        String sql = "UPDATE persona SET dni=?, nombre_completo=?, puesto=?, departamento=?, " +
                "email_corporativo=?, foto_url=?, empresa_id=? WHERE id=?";
        try (Connection c = PostgresDataSource.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, persona.getDni());
            ps.setString(2, persona.getNombreCompleto());
            setNullable(ps, 3, persona.getPuesto());
            setNullable(ps, 4, persona.getDepartamento());
            setNullable(ps, 5, persona.getEmailCorporativo());
            setNullable(ps, 6, persona.getFotoUrl());
            setNullableLong(ps, 7, persona.getEmpresaId());
            ps.setLong(8, persona.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            if (esViolacionUnicidad(e)) {
                throw new ValidacionException("Ya existe una persona registrada con ese DNI.");
            }
            throw new RuntimeException("Error al actualizar persona.", e);
        }
    }

    @Override
    public void cambiarEstado(Long id, EstadoPersona estado) {
        String sql = "UPDATE persona SET estado=?::estado_persona WHERE id=?";
        try (Connection c = PostgresDataSource.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, estado.name());
            ps.setLong(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al cambiar estado de persona.", e);
        }
    }

    private Persona crearConId(Long id, Persona original) {
        return Persona.builder()
                .id(id)
                .dni(original.getDni())
                .nombreCompleto(original.getNombreCompleto())
                .puesto(original.getPuesto())
                .departamento(original.getDepartamento())
                .emailCorporativo(original.getEmailCorporativo())
                .fotoUrl(original.getFotoUrl())
                .estado(original.getEstado())
                .empresaId(original.getEmpresaId())
                .build();
    }

    private Persona mapper(ResultSet rs) throws SQLException {
        Long empresaId = rs.getLong("empresa_id");
        if (rs.wasNull()) {
            empresaId = null;
        }
        return Persona.builder()
                .id(rs.getLong("id"))
                .dni(rs.getString("dni"))
                .nombreCompleto(rs.getString("nombre_completo"))
                .puesto(rs.getString("puesto"))
                .departamento(rs.getString("departamento"))
                .emailCorporativo(rs.getString("email_corporativo"))
                .fotoUrl(rs.getString("foto_url"))
                .estado(EstadoPersona.valueOf(rs.getString("estado")))
                .empresaId(empresaId)
                .build();
    }

    private void setNullable(PreparedStatement ps, int idx, String valor) throws SQLException {
        if (valor == null) {
            ps.setNull(idx, java.sql.Types.VARCHAR);
        } else {
            ps.setString(idx, valor);
        }
    }

    private void setNullableLong(PreparedStatement ps, int idx, Long valor) throws SQLException {
        if (valor == null) {
            ps.setNull(idx, java.sql.Types.BIGINT);
        } else {
            ps.setLong(idx, valor);
        }
    }

    private boolean esViolacionUnicidad(SQLException e) {
        return "23505".equals(e.getSQLState());
    }
}
