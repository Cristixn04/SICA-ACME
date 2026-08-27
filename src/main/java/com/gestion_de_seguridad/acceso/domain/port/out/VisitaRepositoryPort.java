package com.gestion_de_seguridad.acceso.domain.port.out;

import com.gestion_de_seguridad.acceso.domain.model.Visita;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para persistencia de visitas.
 */
public interface VisitaRepositoryPort {

    Visita guardar(Visita visita);

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
