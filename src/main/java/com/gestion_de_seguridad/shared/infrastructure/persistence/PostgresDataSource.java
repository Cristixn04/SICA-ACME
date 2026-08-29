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
        try {
            return DriverManager.getConnection(
                    DatabaseConfig.JDBC_URL,
                    DatabaseConfig.DB_USER,
                    DatabaseConfig.DB_PASSWORD);
        } catch (SQLException ex) {
            // Intentos de fallback en entorno de desarrollo local (ident / socket local)
            if ("localhost".equalsIgnoreCase(DatabaseConfig.DB_HOST) || "127.0.0.1".equals(DatabaseConfig.DB_HOST)) {
                try {
                    return DriverManager.getConnection(DatabaseConfig.JDBC_URL, System.getProperty("user.name"), "");
                } catch (SQLException ignored) {}
                try {
                    return DriverManager.getConnection(DatabaseConfig.JDBC_URL, DatabaseConfig.DB_USER, "");
                } catch (SQLException ignored) {}
            }
            throw new SQLException(
                    "No se pudo conectar a la base de datos PostgreSQL en " + DatabaseConfig.JDBC_URL
                    + " con usuario '" + DatabaseConfig.DB_USER + "'.\n"
                    + "Verifique que el servicio PostgreSQL esté activo y que la base de datos 'sica_db' haya sido creada e inicializada.\n"
                    + "Puede configurar las credenciales en 'src/main/resources/application.properties' o variables SICA_DB_USER / SICA_DB_PASSWORD.\n"
                    + "Error original: " + ex.getMessage(), ex);
        }
    }
}
