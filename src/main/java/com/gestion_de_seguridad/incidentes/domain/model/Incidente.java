package com.gestion_de_seguridad.incidentes.domain.model;

import com.gestion_de_seguridad.shared.domain.Validacion;
import com.gestion_de_seguridad.shared.domain.ValidacionException;

import java.time.LocalDateTime;

/**
 * Entidad de negocio: Incidente de seguridad.
 *
 * Puede asociarse a una persona. La especificacion (seccion 4.5) exige que el
 * moderador pueda marcar a una persona con restriccion de acceso de forma
 * inmediata; esa accion es responsabilidad de la capa de aplicacion, que
 * colabora con el slice de personas via eventos/permisos.
 */
public class Incidente {

    private final Long id;
    private final Long personaId;
    private final String tipo;
    private final String descripcion;
    private final SeveridadIncidente severidad;
    private final EstadoIncidente estado;
    private final LocalDateTime fechaRegistro;

    public Incidente(Long id, Long personaId, String tipo, String descripcion,
                     SeveridadIncidente severidad, EstadoIncidente estado, LocalDateTime fechaRegistro) {
        if (personaId == null) {
            throw new ValidacionException("El incidente debe estar asociado a una persona.");
        }
        this.personaId = personaId;
        this.tipo = Validacion.requerido(tipo, "Tipo de incidente");
        this.descripcion = descripcion;
        this.severidad = severidad == null ? SeveridadIncidente.MEDIO : severidad;
        this.estado = estado == null ? EstadoIncidente.ABIERTO : estado;
        this.fechaRegistro = fechaRegistro == null ? LocalDateTime.now() : fechaRegistro;
        this.id = id;
    }

    public Incidente cambiarEstado(EstadoIncidente nuevo) {
        return new Incidente(this.id, this.personaId, this.tipo, this.descripcion,
                this.severidad, nuevo, this.fechaRegistro);
    }

    public Long getId() {
        return id;
    }

    public Long getPersonaId() {
        return personaId;
    }

    public String getTipo() {
        return tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public SeveridadIncidente getSeveridad() {
        return severidad;
    }

    public EstadoIncidente getEstado() {
        return estado;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }
}
