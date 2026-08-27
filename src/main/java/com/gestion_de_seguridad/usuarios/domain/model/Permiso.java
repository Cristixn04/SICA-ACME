package com.gestion_de_seguridad.usuarios.domain.model;

/**
 * Permiso granular del sistema (ej. 'crear_visita', 'generar_reporte_mensual').
 *
 * Cada accion critica es un permiso individual. La logica de autorizacion
 * verifica contra estos permisos, que viven en la base de datos y son
 * configurables sin modificar codigo.
 */
public class Permiso {

    private final Long id;
    private final String codigo;
    private final String modulo;

    public Permiso(Long id, String codigo, String modulo) {
        this.id = id;
        this.codigo = codigo;
        this.modulo = modulo;
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getModulo() {
        return modulo;
    }
}
