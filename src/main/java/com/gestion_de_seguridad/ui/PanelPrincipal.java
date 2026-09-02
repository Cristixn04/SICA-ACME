package com.gestion_de_seguridad.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 * Esqueleto del panel principal: barra superior (titulo SICA, campana con
 * pulso, avatar + usuario) y sidebar de navegacion por rol -con logo ACME e
 * iconos por item-, con un area central de contenido reemplazable.
 *
 * El esqueleto se define en resources/fxml/panel_principal.fxml; este
 * controlador puebla la campana/avatar/sidebar segun la sesion activa y los
 * items de navegacion (por rol) provistos por el orquestador.
 */
public final class PanelPrincipal {

    private final Runnable alCerrarSesion;
    private boolean cargado;

    @FXML
    private BorderPane raiz;
    @FXML
    private VBox sidebar;
    @FXML
    private StackPane contenido;
    @FXML
    private MenuButton usuarioMenu;
    @FXML
    private StackPane campanaSlot;

    public PanelPrincipal(Runnable alCerrarSesion) {
        this.alCerrarSesion = alCerrarSesion;
    }

    /**
     * Nodo raiz del panel (barra superior + sidebar + contenido).
     */
    public Parent raiz() {
        return raiz;
    }

    /**
     * Carga el esqueleto FXML y lo deja listo para {@link #configurarNavegacion}.
     */
    public Parent crear() {
        if (cargado) {
            return raiz;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/panel_principal.fxml"));
            loader.setController(this);
            Parent nodo = loader.load();
            cargado = true;
            return nodo;
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo cargar panel_principal.fxml", ex);
        }
    }

    @FXML
    private void initialize() {
        campanaSlot.getChildren().add(UiUtils.campanaConPulso());
        usuarioMenu.setText(Sesion.obtener().nombreUsuario()
                + " (" + Sesion.obtener().nombreRol() + ")");
        usuarioMenu.setGraphic(UiUtils.avatar(Sesion.obtener().nombreUsuario(), 12));
        MenuItem cerrar = new MenuItem("Cerrar sesión");
        cerrar.setOnAction(e -> alCerrarSesion.run());
        usuarioMenu.getItems().add(cerrar);
    }

    /**
     * Construye el sidebar con los items de navegacion del rol activo. Cada
     * item llama a un callback (Runnable) para cargar su vista.
     */
    public void configurarNavegacion(ItemNavegacion... items) {
        sidebar.getChildren().clear();
        sidebar.getChildren().add(UiUtils.crearLogoCompacto());

        ToggleGroup grupo = new ToggleGroup();
        for (ItemNavegacion item : items) {
            ToggleButton bt = new ToggleButton(item.icono() + "   " + item.etiqueta());
            bt.getStyleClass().add("item-nav");
            bt.setToggleGroup(grupo);
            bt.setMaxWidth(Double.MAX_VALUE);
            bt.setOnAction(e -> item.accion().run());
            sidebar.getChildren().add(bt);
        }
        if (grupo.getToggles().size() > 0) {
            grupo.getToggles().get(0).setSelected(true);
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        Button btnAyuda = new Button("❓   Ayuda y Soporte");
        btnAyuda.getStyleClass().add("item-nav");
        btnAyuda.setMaxWidth(Double.MAX_VALUE);
        btnAyuda.setOnAction(e -> VistaAyuda.mostrarDialogoAyuda());

        sidebar.getChildren().addAll(spacer, btnAyuda);

        contenido.getChildren().clear();
        Label bienvenida = new Label("Bienvenido, " + Sesion.obtener().nombreUsuario());
        bienvenida.getStyleClass().add("acme-titulo");
        contenido.getChildren().add(bienvenida);
    }

    public void establecerContenido(javafx.scene.Node nodo) {
        contenido.getChildren().clear();
        if (nodo != null) {
            contenido.getChildren().add(nodo);
        }
        UiUtils.animarEntrada(nodo);
    }

    /**
     * Item de navegacion: icono (emoji), etiqueta visible y accion al
     * seleccionarlo.
     */
    public record ItemNavegacion(String icono, String etiqueta, Runnable accion) {
    }
}