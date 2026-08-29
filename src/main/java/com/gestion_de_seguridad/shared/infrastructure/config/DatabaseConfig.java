package com.gestion_de_seguridad.shared.infrastructure.config;

import java.io.InputStream;
import java.util.Properties;

/**
 * Configuración central de la conexión a la base de datos PostgreSQL.
 *
 * Prioridad de resolución:
 * 1. Propiedades de Sistema (-Dsica.db.user=..., -Ddb.user=...)
 * 2. Variables de Entorno (SICA_DB_USER, DB_USER, etc.)
 * 3. Archivo classpath 'application.properties'
 * 4. Valores por defecto (localhost:5432/sica_db, postgres/postgres)
 */
public final class DatabaseConfig {

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream is = DatabaseConfig.class.getResourceAsStream("/application.properties")) {
            if (is != null) {
                PROPS.load(is);
            }
        } catch (Exception e) {
            // Se continúa con valores por defecto y variables de entorno
        }
    }

    public static final String DB_HOST = resolver("SICA_DB_HOST", "db.host", "localhost");
    public static final String DB_PORT = resolver("SICA_DB_PORT", "db.port", "5432");
    public static final String DB_NAME = resolver("SICA_DB_NAME", "db.name", "sica_db");
    public static final String DB_USER = resolver("SICA_DB_USER", "db.user", "postgres");
    public static final String DB_PASSWORD = resolver("SICA_DB_PASSWORD", "db.password", "postgres");

    public static final String JDBC_URL =
            "jdbc:postgresql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;

    private DatabaseConfig() {
    }

    private static String resolver(String envKey, String propKey, String defVal) {
        String sysProp = System.getProperty("sica." + propKey);
        if (sysProp != null && !sysProp.isBlank()) return sysProp.trim();

        sysProp = System.getProperty(propKey);
        if (sysProp != null && !sysProp.isBlank()) return sysProp.trim();

        String envVal = System.getenv(envKey);
        if (envVal != null && !envVal.isBlank()) return envVal.trim();

        String fileVal = PROPS.getProperty(propKey);
        if (fileVal != null && !fileVal.isBlank()) return fileVal.trim();

        return defVal;
    }
}
