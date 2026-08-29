package com.gestion_de_seguridad.acceso.domain.port.out;

import com.gestion_de_seguridad.acceso.domain.model.Visita;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para persistencia de visitas.
 */
public interface VisitaRepositoryPort {

    Visita guardar(Visita visita);

    /**
     * Guarda una nueva visita y, opcionalmente, cierra una visita activa previa
     * (regularizacion por "salida olvidada") de forma ATOMICA: ambas escrituras
     * se ejecutan en una unica transaccion. Si el INSERT falla, el UPDATE del
     * cierre se deshace (rollback).
     *
     * @param anteriorActiva visita activa a cerrar como CERRADA_POR_SISTEMA
     *                       (puede ser null si no hay)
     * @param nueva          visita a insertar
     * @return la visita insertada con su id asignado
     */
    Visita guardarRegularizando(Visita anteriorActiva, Visita nueva);

    void actualizar(Visita visita);

    Optional<Visita> buscarPorId(Long id);

    /**
     * Busca visitas por texto de la persona (DNI, nombre) o empresa, para la
     * pantalla de check-in del guarda.
     */
    List<Visita> buscarPorTexto(String texto);

    /**
     * Ultima visita aun activa (estado DENTRO o CHECK_IN) de una persona.
     * Usada para detectar la "salida olvidada" que debe regularizarse.
     */
    Optional<Visita> ultimaVisitaActivaDePersona(Long personaId);

    List<Visita> listar();
}
