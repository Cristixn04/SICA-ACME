package com.gestion_de_seguridad.shared.infrastructure.persistence;

import com.gestion_de_seguridad.shared.infrastructure.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gestiona las conexiones JDBC a PostgreSQL.
 *
 * Mientras se trabaja con JDBC puro, cada repositorio pide una conexion
 * mediante obtenerConexion(); la gestion de pool de conexiones (HikariCP)
 * podria incorporarse mas adelante sin cambiar la firma de este metodo.
 */
public final class PostgresDataSource {

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("No se encontro el driver JDBC de PostgreSQL.", e);
        }
    }

    private PostgresDataSource() {
    }

    /**
     * Obtiene una conexion a la base de datos configurada.
     *
     * @throws SQLException si no es posible conectar (servidor caido,
     *                      credenciales incorrectas, base inexistente).
     */
    public static Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(
                DatabaseConfig.JDBC_URL,
                DatabaseConfig.DB_USER,
                DatabaseConfig.DB_PASSWORD);
    }
}
