package com.gestion_de_seguridad.ui;

import com.gestion_de_seguridad.usuarios.domain.model.Usuario;
import com.gestion_de_seguridad.usuarios.domain.port.in.AutenticarUsuarioUseCase;
import com.gestion_de_seguridad.usuarios.infrastructure.config.UsuariosContainer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 * Pantalla de Login (Inicio de Sesion).
 *
 * El layout declarativo vive en resources/fxml/login.fxml; este controlador
 * inyecta el fondo blueprint, conecta el selector de rol y la autenticacion
 * (BCrypt) se ejecuta en un hilo de fondo para no congelar el hilo JavaFX.
 * Valida que el rol elegido coincida con el del usuario antes de navegar.
 */
public final class VistaLogin {

    private static final double ANCHO = 900;
    private static final double ALTO = 640;

    private final AutenticarUsuarioUseCase autenticar = UsuariosContainer.autenticar();
    private final Runnable alLoginear;
    private final Runnable alOlvidarPassword;

    @FXML
    private StackPane raiz;
    @FXML
    private VBox tarjeta;
    @FXML
    private StackPane logoSica;
    @FXML
    private HBox cajaUsuario;
    @FXML
    private HBox cajaPass;
    @FXML
    private TextField campoUsuario;
    @FXML
    private PasswordField campoPass;
    @FXML
    private Label error;
    @FXML
    private ToggleGroup grupoRol;
    @FXML
    private Label linkOlvido;

    public VistaLogin(Runnable alLoginear, Runnable alOlvidarPassword) {
        this.alLoginear = alLoginear;
        this.alOlvidarPassword = alOlvidarPassword;
    }

    public Scene crear() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            loader.setController(this);
            Scene scene = new Scene(loader.load(), ANCHO, ALTO);
            UiUtils.aplicarTema(scene);
            return scene;
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo cargar login.fxml", ex);
        }
    }

    @FXML
    private void initialize() {
        raiz.getChildren().add(0, UiUtils.fondoBlueprint(ANCHO, ALTO));
        logoSica.getChildren().add(UiUtils.crearLogoSica());
        linkOlvido.setOnMouseClicked(e -> alOlvidarPassword.run());
        configurarGlow(cajaUsuario, campoUsuario);
        configurarGlow(cajaPass, campoPass);

        // Auto-llenado de credenciales segun el rol seleccionado para facilitar las pruebas
        campoUsuario.setText("guarda");
        campoPass.setText("1234");

        grupoRol.selectedToggleProperty().addListener((obs, antes, seleccionado) -> {
            if (seleccionado != null) {
                String rol = rolSeleccionado(grupoRol);
                if ("ADMINISTRADOR".equalsIgnoreCase(rol)) {
                    campoUsuario.setText("admin");
                    campoPass.setText("1234");
                } else if ("GUARDA".equalsIgnoreCase(rol)) {
                    campoUsuario.setText("guarda");
                    campoPass.setText("1234");
                } else if ("FUNCIONARIO".equalsIgnoreCase(rol)) {
                    campoUsuario.setText("funcionario");
                    campoPass.setText("1234");
                }
            }
        });
    }

    @FXML
    private void onIngresar() {
        String usuarioIngreso = campoUsuario.getText();
        String pass = campoPass.getText();
        if (usuarioIngreso == null || usuarioIngreso.isBlank() || pass == null || pass.isBlank()) {
            mostrarError("Ingrese usuario y contraseña.");
            return;
        }
        UiUtils.enHiloFondo(
                () -> autenticar.autenticar(usuarioIngreso.trim(), pass),
                this::procesarAutenticacion,
                ex -> mostrarError("No se pudo autenticar. Intente nuevamente."));
    }

    private void procesarAutenticacion(Usuario usuario) {
        if (usuario == null) {
            mostrarError("Credenciales inválidas o usuario inactivo.");
            return;
        }
        String rolElegido = rolSeleccionado(grupoRol);
        if (!usuario.getRol().getNombre().equalsIgnoreCase(rolElegido)) {
            mostrarError("El rol seleccionado no coincide con el de este usuario.");
            return;
        }
        Sesion.obtener().iniciar(usuario);
        UiUtils.animarEntrada(tarjeta);
        alLoginear.run();
    }

    private void mostrarError(String texto) {
        error.setText(texto);
        error.setVisible(true);
    }

    private void configurarGlow(HBox caja, TextInputControl campo) {
        campo.focusedProperty().addListener((obs, antes, ahora) -> {
            if (ahora) {
                caja.getStyleClass().add("campo-caja-focus");
            } else {
                caja.getStyleClass().remove("campo-caja-focus");
            }
        });
    }

    private String rolSeleccionado(ToggleGroup grupo) {
        ToggleButton seleccionado = (ToggleButton) grupo.getSelectedToggle();
        if (seleccionado == null) {
            return "";
        }
        String t = seleccionado.getText();
        if (t.contains("Funcionario")) {
            return "FUNCIONARIO";
        }
        if (t.contains("Guarda")) {
            return "GUARDA";
        }
        return "ADMINISTRADOR";
    }
}