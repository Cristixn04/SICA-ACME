package com.gestion_de_seguridad.personas.domain.port.in;

import com.gestion_de_seguridad.personas.domain.model.Persona;

import java.util.List;

/**
 * Caso de uso de entrada: gestion de personas (CRUD + bloqueo).
 *
 * Cada operacion critica verifica que el usuario tenga el permiso requerido
 * (RBAC) antes de ejecutarse.
 */
public interface GestionarPersonaUseCase {

    /**
     * Registra una nueva persona.
     *
     * @param idUsuario  usuario autenticado que ejecuta la operacion (permiso 'registrar_persona')
     * @param persona    datos validados de la persona
     */
    Persona registrar(Long idUsuario, Persona persona);

    Persona buscarPorDni(String dni);

    List<Persona> buscar(String texto);

    List<Persona> listarTodas();

    /**
     * Actualiza los datos de una persona (permiso 'editar_persona').
     */
    void actualizar(Long idUsuario, Persona persona);

    /**
     * Bloquea el acceso de una persona (permiso 'bloquear_persona'),
     * seteando su estado a INACTIVO.
     */
    void bloquear(Long idUsuario, Long idPersona);
}
