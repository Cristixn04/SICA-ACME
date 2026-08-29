package com.gestion_de_seguridad.ui;

import com.gestion_de_seguridad.usuarios.domain.model.Usuario;

/**
 * Estado de sesion de la aplicacion (un solo proceso JavaFX multi-ventana).
 *
 * Guarda el usuario autenticado para que las vistas conozcan el rol activo y
 * verifiquen permisos de forma centralizada.
 */
public final class Sesion {

    private static final Sesion INSTANCIA = new Sesion();
    private Usuario usuario;

    private Sesion() {
    }

    public static Sesion obtener() {
        return INSTANCIA;
    }

    public void iniciar(Usuario usuario) {
        this.usuario = usuario;
    }

    public void cerrar() {
        this.usuario = null;
    }

    public boolean haySesion() {
        return usuario != null;
    }

    public Usuario usuario() {
        return usuario;
    }

    public Long idUsuario() {
        return usuario == null ? null : usuario.getId();
    }

    public String nombreUsuario() {
        return usuario == null ? "Invitalo" : usuario.getNombreUsuario();
    }

    public String nombreRol() {
        return usuario == null ? "" : usuario.getRol().getNombre();
    }
}
