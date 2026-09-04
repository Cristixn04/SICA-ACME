package com.gestion_de_seguridad.personas.domain.model;

import com.gestion_de_seguridad.shared.domain.Validacion;

/**
 * Persona registrada en el sistema (empleado o visitante frecuente),
 * asociada opcionalmente a una empresa.
 *
 * Valida sus datos en el dominio (principio: el dominio es el encargado de
 * las reglas de negocio). Utiliza la utilidad compartida {@link Validacion}.
 */
public class Persona {

    private final Long id;
    private final String dni;
    private final String nombreCompleto;
    private final String puesto;
    private final String departamento;
    private final String emailCorporativo;
    private final String fotoUrl;
    private final EstadoPersona estado;
    private final Long empresaId;

    private Persona(Builder b) {
        this.id = b.id;
        this.dni = Validacion.dni(b.dni);
        this.nombreCompleto = Validacion.nombre(b.nombreCompleto, "Nombre Completo");
        this.puesto = (b.puesto == null || b.puesto.isBlank()) ? null : Validacion.textoDescriptivo(b.puesto, "Puesto");
        this.departamento = (b.departamento == null || b.departamento.isBlank()) ? null : Validacion.textoDescriptivo(b.departamento, "Departamento");
        this.emailCorporativo = (b.emailCorporativo == null || b.emailCorporativo.isBlank()) ? null
                : Validacion.email(b.emailCorporativo, "Correo Corporativo");
        this.fotoUrl = b.fotoUrl;
        this.estado = b.estado == null ? EstadoPersona.ACTIVO : b.estado;
        this.empresaId = b.empresaId;
    }

    public Long getId() {
        return id;
    }

    public String getDni() {
        return dni;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getPuesto() {
        return puesto;
    }

    public String getDepartamento() {
        return departamento;
    }

    public String getEmailCorporativo() {
        return emailCorporativo;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public EstadoPersona getEstado() {
        return estado;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder para crear una Persona de forma legible y validada.
     */
    public static final class Builder {
        private Long id;
        private String dni;
        private String nombreCompleto;
        private String puesto;
        private String departamento;
        private String emailCorporativo;
        private String fotoUrl;
        private EstadoPersona estado;
        private Long empresaId;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder dni(String dni) {
            this.dni = dni;
            return this;
        }

        public Builder nombreCompleto(String nombreCompleto) {
            this.nombreCompleto = nombreCompleto;
            return this;
        }

        public Builder puesto(String puesto) {
            this.puesto = puesto;
            return this;
        }

        public Builder departamento(String departamento) {
            this.departamento = departamento;
            return this;
        }

        public Builder emailCorporativo(String email) {
            this.emailCorporativo = email;
            return this;
        }

        public Builder fotoUrl(String fotoUrl) {
            this.fotoUrl = fotoUrl;
            return this;
        }

        public Builder estado(EstadoPersona estado) {
            this.estado = estado;
            return this;
        }

        public Builder empresaId(Long empresaId) {
            this.empresaId = empresaId;
            return this;
        }

        public Persona build() {
            return new Persona(this);
        }
    }
}
