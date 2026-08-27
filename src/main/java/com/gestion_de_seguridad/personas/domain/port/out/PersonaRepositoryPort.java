package com.gestion_de_seguridad.personas.domain.port.out;

import com.gestion_de_seguridad.personas.domain.model.Persona;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para persistencia de personas.
 */
public interface PersonaRepositoryPort {

    Optional<Persona> buscarPorId(Long id);

    Optional<Persona> buscarPorDni(String dni);

    List<Persona> buscar(String texto);

    List<Persona> listarTodas();

    Persona guardar(Persona persona);

    void actualizar(Persona persona);

    void cambiarEstado(Long id, com.gestion_de_seguridad.personas.domain.model.EstadoPersona estado);
}
