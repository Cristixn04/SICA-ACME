package com.gestion_de_seguridad.acceso.domain.model;

/**
 * Estrategia de visita no anunciada (tiempo real, seccion 2.2).
 *
 * El guarda registra al invitado sin previo aviso; queda PENDIENTE_APROBACION
 * y se notifica al funcionario para su decision.
 */
public class EstrategiaVisitaNoAnunciada implements EstrategiaCreacionVisita {

    @Override
    public EstadoVisita estadoInicial() {
        return EstadoVisita.PENDIENTE_APROBACION;
    }

    @Override
    public String nombreFlujo() {
        return "NO_ANUNCIADA";
    }
}
