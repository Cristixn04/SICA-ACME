package com.gestion_de_seguridad.acceso.domain.model;

import com.gestion_de_seguridad.shared.domain.EstadoInvalidoException;

import java.util.Set;

/**
 * Estados posibles de una Visita (patron State).
 *
 * Cada estado declara a que estados puede transicionar. Cualquier transicion
 * fuera de este mapa lanza EstadoInvalidoException, garantizando la integridad
 * de la maquina de estados definida en las especificaciones.
 */
public enum EstadoVisita {
    PENDIENTE_APROBACION(Set.of("APROBADO", "RECHAZADO")),
    PENDIENTE_APROBACION_POR_OLVIDO(Set.of("APROBADO", "RECHAZADO")),
    APROBADO(Set.of("CHECK_IN")),
    RECHAZADO(Set.of()),
    CHECK_IN(Set.of("DENTRO")),
    DENTRO(Set.of("CHECK_OUT", "CERRADA_POR_SISTEMA")),
    CHECK_OUT(Set.of()),
    CERRADA_POR_SISTEMA(Set.of());

    private final Set<String> transicionesPermitidas;

    EstadoVisita(Set<String> transicionesPermitidas) {
        this.transicionesPermitidas = transicionesPermitidas;
    }

    /**
     * Determina si se permite pasar a {@code destino} desde este estado.
     */
    public boolean permiteTransicion(String destino) {
        return transicionesPermitidas.contains(destino);
    }

    /**
     * Valida una transicion; lanza EstadoInvalidoException si no es permitida.
     */
    public void validarTransicion(EstadoVisita destino) {
        if (!permiteTransicion(destino.name())) {
            throw new EstadoInvalidoException(
                    "Transicion de estado invalida: de '" + this.name() + "' a '" + destino.name() + "'.");
        }
    }
}
