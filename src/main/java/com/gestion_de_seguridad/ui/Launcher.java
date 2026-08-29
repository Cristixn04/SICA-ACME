package com.gestion_de_seguridad.ui;

import javafx.scene.text.Font;

/**
 * Punto de entrada de la aplicacion JavaFX.
 */
public class Launcher {
    public static void main(String[] args) {
        UiUtils.cargarFuentes();
        App.main(args);
    }
}
