package com.gestion_de_seguridad.ui;

import com.gestion_de_seguridad.acceso.infrastructure.config.AccesoContainer;
import com.gestion_de_seguridad.auditoria.infrastructure.config.AuditoriaContainer;
import com.gestion_de_seguridad.incidentes.infrastructure.config.IncidentesContainer;
import com.gestion_de_seguridad.personas.infrastructure.config.PersonasContainer;
import com.gestion_de_seguridad.reportes.infrastructure.config.ReportesContainer;
import com.gestion_de_seguridad.usuarios.domain.model.Rol;
import com.gestion_de_seguridad.usuarios.infrastructure.config.UsuariosContainer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Aplicacion JavaFX de SICA.
 *
 * Orquesta la navegacion entre el login y el panel principal, construyendo los
 * items del sidebar (con su icono) segun el rol del usuario autenticado.
 *
 * En {@link #inicializarContenedores()} se fuerzan los composition root de cada
 * slice: el bloque estatico de AuditoriaContainer registra el listener de
 * auditoria en el EventPublisher, de modo que la bitacora funcione tambien en
 * produccion (no solo en los tests).
 */
public final class App extends Application {

    private Stage stage;
    private PanelPrincipal panel;
    private AutoCloseable vistaActiva;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() {
        UiUtils.cargarFuentes();
    }

    @Override
    public void start(Stage stage) {
        UiUtils.cargarFuentes();
        inicializarContenedores();
        this.stage = stage;
        stage.setTitle("SICA - Sistema Integrado de Control de Acceso (Zona ACME)");
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        mostrarLogin();
        stage.show();
    }

    /**
     * Fuerza la inicializacion de todos los composition root. La auditoria
     * (Observer) depende de que AuditoriaContainer quede cargado para suscribir
     * su listener a los eventos de dominio.
     */
    private void inicializarContenedores() {
        UsuariosContainer.autenticar();
        PersonasContainer.gestionar();
        AccesoContainer.gestionar();
        IncidentesContainer.gestionar();
        AuditoriaContainer.usar();
        ReportesContainer.generar();
    }

    private void mostrarLogin() {
        Runnable alLoginear = this::mostrarPanelPrincipal;
        Scene login = new VistaLogin(alLoginear, this::mostrarRecuperar).crear();
        stage.setScene(login);
    }

    private void mostrarRecuperar() {
        Scene recuperar = new VistaRecuperar(this::mostrarLogin).crear();
        stage.setScene(recuperar);
    }

    private void mostrarPanelPrincipal() {
        panel = new PanelPrincipal(this::cerrarSesion);
        panel.crear();
        panel.configurarNavegacion(itemsPorRol());

        mostrarCheckIn();

        Scene interna = new Scene(panel.raiz(), 1100, 700);
        UiUtils.aplicarTema(interna);
        stage.setScene(interna);
    }

    private void mostrarCheckIn() {
        VistaCheckIn v = new VistaCheckIn();
        mostrarContenido(v.crear(), v);
    }

    private void mostrarContenido(javafx.scene.Node nodo) {
        cerrarVistaActiva();
        if (panel != null) {
            panel.establecerContenido(nodo);
        }
    }

    private void mostrarContenido(javafx.scene.Node nodo, AutoCloseable cerrarAlSalir) {
        cerrarVistaActiva();
        vistaActiva = cerrarAlSalir;
        if (panel != null) {
            panel.establecerContenido(nodo);
        }
    }

    private void cerrarVistaActiva() {
        if (vistaActiva != null) {
            try {
                vistaActiva.close();
            } catch (Exception ignorado) {
                // la UI ya no se puede desuscribir; se ignora
            }
            vistaActiva = null;
        }
    }

    private PanelPrincipal.ItemNavegacion[] itemsPorRol() {
        String rol = Sesion.obtener().nombreRol();
        if ("ADMINISTRADOR".equalsIgnoreCase(rol)) {
            return new PanelPrincipal.ItemNavegacion[]{
                    new PanelPrincipal.ItemNavegacion("📅", "Visitas / Check-in", this::mostrarCheckIn),
                    new PanelPrincipal.ItemNavegacion("👤", "Personas", () ->
                            mostrarContenido(new VistaPersonas().crear())),
                    new PanelPrincipal.ItemNavegacion("⚠️", "Incidentes", () ->
                            mostrarContenido(new VistaIncidentes().crear())),
                    new PanelPrincipal.ItemNavegacion("📊", "Reportes", () ->
                            mostrarContenido(new VistaReportes().crear())),
                    new PanelPrincipal.ItemNavegacion("⚙️", "Usuarios / Permisos", () ->
                            mostrarContenido(new VistaUsuarios().crear())),
            };
        }
        if ("GUARDA".equalsIgnoreCase(rol)) {
            return new PanelPrincipal.ItemNavegacion[]{
                    new PanelPrincipal.ItemNavegacion("📅", "Visitas / Check-in", this::mostrarCheckIn),
                    new PanelPrincipal.ItemNavegacion("👤", "Personas", () ->
                            mostrarContenido(new VistaPersonas().crear())),
                    new PanelPrincipal.ItemNavegacion("⚠️", "Incidentes", () ->
                            mostrarContenido(new VistaIncidentes().crear())),
            };
        }
        // FUNCIONARIO
        return new PanelPrincipal.ItemNavegacion[]{
                new PanelPrincipal.ItemNavegacion("📅", "Visitas / Check-in", this::mostrarCheckIn),
        };
    }

    private void cerrarSesion() {
        cerrarVistaActiva();
        Sesion.obtener().cerrar();
        mostrarLogin();
    }
}
