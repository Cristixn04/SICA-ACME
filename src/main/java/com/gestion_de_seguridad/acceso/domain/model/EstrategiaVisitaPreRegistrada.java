package com.gestion_de_seguridad.acceso.domain.model;

/**
 * Estrategia de visita pre-registrada (flujo ideal, seccion 2.1).
 *
 * El Funcionario registra la visita con antelacion y el estado queda APROBADO.
 */
public class EstrategiaVisitaPreRegistrada implements EstrategiaCreacionVisita {

    @Override
    public EstadoVisita estadoInicial() {
        return EstadoVisita.APROBADO;
    }

    @Override
    public String nombreFlujo() {
        return "PRE_REGISTRADA";
    }
}
