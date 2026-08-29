package com.gestion_de_seguridad.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;

import java.io.IOException;

/**
 * Pantalla Recuperar Contraseña.
 *
 * El layout declarativo (candado cartoon con anillo animado, campo de correo
 * corporativo con icono, boton "Enviar Enlace") vive en
 * resources/fxml/recuperar.fxml. Operacion de momento simulada: valida el
 * campo y muestra la confirmacion usando las clases de estilo del tema
 * (texto-error / texto-exito) en lugar de estilos inline.
 */
public final class VistaRecuperar {

    private static final double ANCHO = 900;
    private static final double ALTO = 640;

    private final Runnable volverAlLogin;

    @FXML
    private StackPane raiz;
    @FXML
    private StackPane logoSica;
    @FXML
    private Circle argolla;
    @FXML
    private TextField campoEmail;
    @FXML
    private Label mensaje;
    @FXML
    private Label volver;

    public VistaRecuperar(Runnable volverAlLogin) {
        this.volverAlLogin = volverAlLogin;
    }

    public Scene crear() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/recuperar.fxml"));
            loader.setController(this);
            Scene scene = new Scene(loader.load(), ANCHO, ALTO);
            UiUtils.aplicarTema(scene);
            return scene;
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo cargar recuperar.fxml", ex);
        }
    }

    @FXML
    private void initialize() {
        raiz.getChildren().add(0, UiUtils.fondoBlueprint(ANCHO, ALTO));
        logoSica.getChildren().add(UiUtils.crearLogoSica());
        UiUtils.pulso(argolla);
        volver.setOnMouseClicked(e -> volverAlLogin.run());
    }

    @FXML
    private void onEnviar() {
        String email = campoEmail.getText();
        if (email == null || email.isBlank()) {
            mostrarMensaje("Ingrese un correo electrónico.", true);
            return;
        }
        mostrarMensaje("Se envió el enlace de recuperación a " + email.trim() + ".", false);
    }

    private void mostrarMensaje(String texto, boolean error) {
        mensaje.getStyleClass().removeAll("texto-error", "texto-exito");
        mensaje.getStyleClass().add(error ? "texto-error" : "texto-exito");
        mensaje.setText(texto);
        mensaje.setVisible(true);
    }
}