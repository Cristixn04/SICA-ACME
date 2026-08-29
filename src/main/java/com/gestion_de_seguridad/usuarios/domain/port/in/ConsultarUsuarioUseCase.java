package com.gestion_de_seguridad.usuarios.domain.port.in;

/**
 * Caso de uso de entrada: consultas ligeras sobre usuarios.
 *
 * Permite a otros componentes (ej. el listener de auditoria) resolver el
 * nombre de usuario a partir de un id sin que la auditoria conozca la
 * persistencia del slice de usuarios.
 */
public interface ConsultarUsuarioUseCase {

    /**
     * Nombre de usuario asociado al id, o null si no existe (o id null).
     */
    String nombreUsuario(Long idUsuario);

    /**
     * Lista todos los usuarios registrados en el sistema.
     */
    java.util.List<com.gestion_de_seguridad.usuarios.domain.model.Usuario> listarTodos();

    /**
     * Registra un nuevo usuario del sistema con su rol correspondiente.
     */
    com.gestion_de_seguridad.usuarios.domain.model.Usuario registrarUsuario(
            Long idOperador, String nombreUsuario, String passwordPlano, String nombreRol);
}