package com.gestion_de_seguridad.usuarios.domain.model;

/**
 * Rol del sistema (Administrador, Guarda, Funcionario).
 *
 * Representa una agrupacion de permisos. La relacion rol-permiso vive en la
 * base de datos (tabla puente rol_permiso) y es configurable sin tocar codigo.
 */
public class Rol {

    private final Long id;
    private final String nombre;

    public Rol(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }
}
