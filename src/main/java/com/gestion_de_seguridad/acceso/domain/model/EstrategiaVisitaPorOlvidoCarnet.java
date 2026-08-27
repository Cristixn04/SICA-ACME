package com.gestion_de_seguridad.acceso.domain.model;

/**
 * Estrategia de ingreso por olvido de carnet / documento (pase temporal,
 * seccion 2.3).
 *
 * Un trabajador llega sin su documento; el ingreso queda PENDIENTE_APROBACION
 * POR OLVIDO y es valido solo para ese dia.
 */
public class EstrategiaVisitaPorOlvidoCarnet implements EstrategiaCreacionVisita {

    @Override
    public EstadoVisita estadoInicial() {
        return EstadoVisita.PENDIENTE_APROBACION_POR_OLVIDO;
    }

    @Override
    public String nombreFlujo() {
        return "POR_OLVIDO_CARNET";
    }
}
