package com.gestion_de_seguridad.acceso.domain.model;

import com.gestion_de_seguridad.shared.domain.EstadoInvalidoException;
import com.gestion_de_seguridad.shared.domain.Validacion;

import java.time.LocalDateTime;

/**
 * Entidad agregada raiz del modulo de acceso: una Visita.
 *
 * Encapsula la maquina de estados (patron State). Los cambios de estado solo
 * pueden realizarse mediante sus metodos, que validan la transicion y
 * devuelven una nueva instancia inmutable (una visita cerrada nunca debe
 * volver a un estado anterior).
 */
public class Visita {

    private final Long id;
    private final Long personaId;
    private final Long empresaPropietariaId;
    private final Long personaVisitadaId;
    private final String motivo;
    private final LocalDateTime fechaHoraVisita;
    private final EstadoVisita estado;
    private final LocalDateTime fechaHoraCheckin;
    private final LocalDateTime fechaHoraCheckout;
    private final Long guardaId;
    private final String motivoCierre;

    private Visita(Long id, Long personaId, Long empresaPropietariaId, Long personaVisitadaId,
                   String motivo, LocalDateTime fechaHoraVisita, EstadoVisita estado,
                   LocalDateTime fechaHoraCheckin, LocalDateTime fechaHoraCheckout,
                   Long guardaId, String motivoCierre) {
        this.id = id;
        this.personaId = personaId;
        this.empresaPropietariaId = empresaPropietariaId;
        this.personaVisitadaId = personaVisitadaId;
        this.motivo = motivo;
        this.fechaHoraVisita = fechaHoraVisita;
        this.estado = estado;
        this.fechaHoraCheckin = fechaHoraCheckin;
        this.fechaHoraCheckout = fechaHoraCheckout;
        this.guardaId = guardaId;
        this.motivoCierre = motivoCierre;
    }

    // ---- Transiciones de estado (patron State) ----

    public Visita aprobar() {
        validar("APROBADO");
        return copiarConEstado(EstadoVisita.APROBADO, true);
    }

    public Visita aprobarPorOlvidoFilial() {
        validar("APROBADO");
        return copiarConEstado(EstadoVisita.APROBADO, true);
    }

    public Visita rechazar() {
        validar("RECHAZADO");
        return copiarConEstado(EstadoVisita.RECHAZADO, true);
    }

    public Visita realizarCheckIn(Long idGuarda) {
        validar("CHECK_IN");
        return new Visita(id, personaId, empresaPropietariaId, personaVisitadaId, motivo,
                fechaHoraVisita, EstadoVisita.CHECK_IN, LocalDateTime.now(), fechaHoraCheckout,
                idGuarda, null);
    }

    public Visita confirmarDentro() {
        validar("DENTRO");
        return copiarConEstado(EstadoVisita.DENTRO, false);
    }

    public Visita realizarCheckOut() {
        validar("CHECK_OUT");
        return new Visita(id, personaId, empresaPropietariaId, personaVisitadaId, motivo,
                fechaHoraVisita, EstadoVisita.CHECK_OUT, fechaHoraCheckin, LocalDateTime.now(),
                guardaId, null);
    }

    /**
     * Cierre automatico por el sistema (salida olvidada). Es la unica
     * transicion sin intervencion humana. Queda auditada como evento distinto
     * de un check-out normal.
     */
    public Visita cerrarPorSistema(String motivo) {
        validar("CERRADA_POR_SISTEMA");
        return new Visita(id, personaId, empresaPropietariaId, personaVisitadaId, motivo,
                fechaHoraVisita, EstadoVisita.CERRADA_POR_SISTEMA, fechaHoraCheckin,
                LocalDateTime.now(), guardaId, motivo);
    }

    private Visita copiarConEstado(EstadoVisita nuevo, boolean resetTiempos) {
        return new Visita(id, personaId, empresaPropietariaId, personaVisitadaId, motivo,
                fechaHoraVisita, nuevo,
                resetTiempos ? null : fechaHoraCheckin,
                resetTiempos ? null : fechaHoraCheckout,
                guardaId, motivoCierre);
    }

    private void validar(String destino) {
        if (estado == null) {
            throw new EstadoInvalidoException("La visita no tiene un estado valido.");
        }
        if (!estado.permiteTransicion(destino)) {
            throw new EstadoInvalidoException(
                    "Transicion de estado invalida: de '" + estado.name() + "' a '" + destino + "'.");
        }
    }

    // ---- Getters ----

    public Long getId() {
        return id;
    }

    public Long getPersonaId() {
        return personaId;
    }

    public Long getEmpresaPropietariaId() {
        return empresaPropietariaId;
    }

    public Long getPersonaVisitadaId() {
        return personaVisitadaId;
    }

    public String getMotivo() {
        return motivo;
    }

    public LocalDateTime getFechaHoraVisita() {
        return fechaHoraVisita;
    }

    public EstadoVisita getEstado() {
        return estado;
    }

    public LocalDateTime getFechaHoraCheckin() {
        return fechaHoraCheckin;
    }

    public LocalDateTime getFechaHoraCheckout() {
        return fechaHoraCheckout;
    }

    public Long getGuardaId() {
        return guardaId;
    }

    public String getMotivoCierre() {
        return motivoCierre;
    }

    // ---- Builder con validacion de motivo ----

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Long id;
        private Long personaId;
        private Long empresaPropietariaId;
        private Long personaVisitadaId;
        private String motivo;
        private LocalDateTime fechaHoraVisita;
        private EstadoVisita estado = EstadoVisita.PENDIENTE_APROBACION;
        private LocalDateTime fechaHoraCheckin;
        private LocalDateTime fechaHoraCheckout;
        private Long guardaId;
        private String motivoCierre;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder personaId(Long personaId) {
            this.personaId = personaId;
            return this;
        }

        public Builder empresaPropietariaId(Long empresaPropietariaId) {
            this.empresaPropietariaId = empresaPropietariaId;
            return this;
        }

        public Builder personaVisitadaId(Long personaVisitadaId) {
            this.personaVisitadaId = personaVisitadaId;
            return this;
        }

        public Builder motivo(String motivo) {
            this.motivo = motivo;
            return this;
        }

        public Builder fechaHoraVisita(LocalDateTime fechaHoraVisita) {
            this.fechaHoraVisita = fechaHoraVisita;
            return this;
        }

        public Builder estado(EstadoVisita estado) {
            this.estado = estado;
            return this;
        }

        public Builder fechaHoraCheckin(LocalDateTime fechaHoraCheckin) {
            this.fechaHoraCheckin = fechaHoraCheckin;
            return this;
        }

        public Builder fechaHoraCheckout(LocalDateTime fechaHoraCheckout) {
            this.fechaHoraCheckout = fechaHoraCheckout;
            return this;
        }

        public Builder guardaId(Long guardaId) {
            this.guardaId = guardaId;
            return this;
        }

        public Builder motivoCierre(String motivoCierre) {
            this.motivoCierre = motivoCierre;
            return this;
        }

        public Visita build() {
            String motivoValidado = Validacion.requerido(motivo, "Motivo de la visita");
            if (personaId == null) {
                throw new com.gestion_de_seguridad.shared.domain.ValidacionException(
                        "La visita debe estar asociada a una persona.");
            }
            return new Visita(id, personaId, empresaPropietariaId, personaVisitadaId,
                    motivoValidado, fechaHoraVisita, estado, fechaHoraCheckin, fechaHoraCheckout,
                    guardaId, motivoCierre);
        }
    }
}
