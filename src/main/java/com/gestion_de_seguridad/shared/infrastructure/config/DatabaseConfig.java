package com.gestion_de_seguridad.shared.infrastructure.config;

/**
 * Configuracion central de la conexion a la base de datos PostgreSQL.
 *
 * Estos valores pueden sobrescribirse mediante variables de entorno para
 * facilitar su despliegue sin tocar codigo (SICA_CONFIG_DB_NAME, etc.).
 */
public final class DatabaseConfig {

    public static final String DB_HOST = env("SICA_DB_HOST", "localhost");
    public static final String DB_PORT = env("SICA_DB_PORT", "5432");
    public static final String DB_NAME = env("SICA_DB_NAME", "sica_db");
    public static final String DB_USER = env("SICA_DB_USER", "postgres");
    public static final String DB_PASSWORD = env("SICA_DB_PASSWORD", "postgres");

    public static final String JDBC_URL =
            "jdbc:postgresql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;

    private DatabaseConfig() {
    }

    private static String env(String clave, String porDefecto) {
        String valor = System.getenv(clave);
        return (valor == null || valor.isBlank()) ? porDefecto : valor;
    }
}
