package com.gestion_de_seguridad.usuarios.domain.model;

/**
 * Usuario de la aplicacion, asociado a un Rol.
 *
 * Conserva el hash de la contrasena (nunca la contrasena en texto plano).
 * La validacion de la contrasena se delega a la capa de aplicacion, que usa
 * BCrypt.
 */
public class Usuario {

    private final Long id;
    private final String nombreUsuario;
    private final String passwordHash;
    private final Rol rol;
    private final Long personaId;
    private final boolean activo;

    public Usuario(Long id, String nombreUsuario, String passwordHash, Rol rol,
                   Long personaId, boolean activo) {
        this.id = id;
        this.nombreUsuario = nombreUsuario;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.personaId = personaId;
        this.activo = activo;
    }

    public Long getId() {
        return id;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Rol getRol() {
        return rol;
    }

    public Long getPersonaId() {
        return personaId;
    }

    public boolean isActivo() {
        return activo;
    }
}
