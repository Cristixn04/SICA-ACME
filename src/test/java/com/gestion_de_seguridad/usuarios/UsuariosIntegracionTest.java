package com.gestion_de_seguridad.usuarios;

import com.gestion_de_seguridad.shared.domain.PermisoDenegadoException;
import com.gestion_de_seguridad.usuarios.domain.model.Usuario;
import com.gestion_de_seguridad.usuarios.domain.port.in.AutenticarUsuarioUseCase;
import com.gestion_de_seguridad.usuarios.domain.port.in.VerificarPermisoUseCase;
import com.gestion_de_seguridad.usuarios.infrastructure.config.UsuariosContainer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test de integracion del slice 'usuarios' contra la base de datos real
 * (requiere sica_db poblada con data.sql).
 */
class UsuariosIntegracionTest {

    private final AutenticarUsuarioUseCase autenticar = UsuariosContainer.autenticar();
    private final VerificarPermisoUseCase verificar = UsuariosContainer.verificarPermiso();

    @Test
    void autenticaAdminConCredencialesValidas() {
        Usuario admin = autenticar.autenticar("admin", "1234");
        assertNotNull(admin);
        assertEquals("ADMINISTRADOR", admin.getRol().getNombre());
    }

    @Test
    void rechazaContrasenaInvalida() {
        assertNull(autenticar.autenticar("admin", "incorrecta"));
    }

    @Test
    void rechazaUsuarioInexistente() {
        assertNull(autenticar.autenticar("no_existe", "1234"));
    }

    @Test
    void adminTienePermisoDeCrearUsuario() {
        assertTrue(verificar.tienePermiso(1L, "crear_usuario"));
    }

    @Test
    void funcionarioNoPoseePermisoDeAdministrador() {
        assertThrows(PermisoDenegadoException.class,
                () -> verificar.verificar(3L, "modificar_permisos"));
    }

    @Test
    void guardaTienePermisoDeCheckin() {
        assertTrue(verificar.tienePermiso(2L, "registrar_checkin"));
    }

    @Test
    void listarUsuariosYRegistrarNuevo() {
        var consultar = UsuariosContainer.consultar();
        var listaInicial = consultar.listarTodos();
        assertNotNull(listaInicial);
        assertTrue(listaInicial.size() >= 3);

        String usernamePrueba = "operador_test_" + System.currentTimeMillis();
        Usuario nuevo = consultar.registrarUsuario(1L, usernamePrueba, "pass1234", "GUARDA");
        assertNotNull(nuevo.getId());
        assertEquals(usernamePrueba, nuevo.getNombreUsuario());
        assertEquals("GUARDA", nuevo.getRol().getNombre());

        Usuario autenticado = autenticar.autenticar(usernamePrueba, "pass1234");
        assertNotNull(autenticado);
        assertEquals(nuevo.getId(), autenticado.getId());
    }
}
