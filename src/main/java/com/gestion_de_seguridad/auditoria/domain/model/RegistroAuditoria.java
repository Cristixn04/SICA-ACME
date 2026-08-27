package com.gestion_de_seguridad.auditoria.domain.model;

import java.time.LocalDateTime;

/**
 * Registro inmutable de la bitacora de auditoria.
 *
 * La especificacion exige que la bitacora sea inmutable (solo permitir INSERT,
 * nunca UPDATE ni DELETE). Esta clase no expone setters y sus campos son
 * finales; una vez creado, un registro no puede modificarse.
 */
public class RegistroAuditoria {

    private final Long id;
    private final LocalDateTime fechaHora;
    private final String usuario;
    private final String accion;
    private final String entidad;
    private final Long entidadId;
    private final String detalles;

    public RegistroAuditoria(Long id, LocalDateTime fechaHora, String usuario, String accion,
                             String entidad, Long entidadId, String detalles) {
        this.id = id;
        this.fechaHora = fechaHora;
        this.usuario = usuario;
        this.accion = accion;
        this.entidad = entidad;
        this.entidadId = entidadId;
        this.detalles = detalles;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getAccion() {
        return accion;
    }

    public String getEntidad() {
        return entidad;
    }

    public Long getEntidadId() {
        return entidadId;
    }

    public String getDetalles() {
        return detalles;
    }
}
