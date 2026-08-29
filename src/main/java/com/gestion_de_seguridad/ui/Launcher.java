package com.gestion_de_seguridad.ui;

import javafx.scene.text.Font;

/**
 * Punto de entrada de la aplicacion JavaFX.
 */
public class Launcher {
    public static void main(String[] args) {
        cargarFuenteEmoji();
        App.main(args);
    }

    /**
     * Carga la fuente Noto Emoji (incluida en resources/fonts/) para que
     * JavaFX pueda renderizar los emojis de la interfaz, ya que la fuente del
     * sistema por defecto no los soporta.
     */
    private static void cargarFuenteEmoji() {
        try {
            Font.loadFont(
                    Launcher.class.getResourceAsStream("/fonts/NotoEmoji-Variable.ttf"),
                    16);
        } catch (RuntimeException ex) {
            System.err.println("No se pudo cargar la fuente de emojis: " + ex.getMessage());
        }
    }
}
