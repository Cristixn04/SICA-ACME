package com.gestion_de_seguridad.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Módulo interactivo de Ayuda, Soporte Técnico y Directorio de Emergencias.
 *
 * Permite a cualquier usuario consultar su manual según su rol activo,
 * ver números de emergencia del complejo Zona ACME y enviar mensajes / tickets
 * de soporte al equipo técnico de seguridad.
 */
public final class VistaAyuda {

    private VistaAyuda() {
    }

    public static void mostrarDialogoAyuda() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Centro de Ayuda y Asistencia - ZONA ACME");
        dialog.setHeaderText(null);

        // Header con logo
        Node logo = UiUtils.crearIconoAcme(40);
        Label titulo = new Label("Centro de Ayuda y Soporte Técnico SICA");
        titulo.setStyle("-fx-font-size: 16px; -fx-font-weight: 900; -fx-text-fill: #ffffff;");
        Label subtitulo = new Label("Complejo Empresarial Zona ACME — Asistencia al Operador");
        subtitulo.setStyle("-fx-font-size: 12px; -fx-text-fill: #9aa5b1;");

        VBox titulosBox = new VBox(2, titulo, subtitulo);
        HBox header = new HBox(12, logo, titulosBox);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10, 14, 10, 14));
        header.setStyle("-fx-background-color: #1b212b; -fx-background-radius: 8px; -fx-border-color: #f28c28; -fx-border-width: 0 0 2 0;");

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setPrefWidth(560);
        tabPane.setPrefHeight(380);

        // Tab 1: Guia de Rol
        Tab tabGuia = new Tab("📖 Guía de Operación", crearContenidoGuia());
        // Tab 2: Directorio de Emergencia
        Tab tabDirectorio = new Tab("📞 Directorio ACME", crearContenidoDirectorio());
        // Tab 3: Enviar Mensaje
        Tab tabMensaje = new Tab("📨 Dejar un Mensaje", crearContenidoMensaje());

        tabPane.getTabs().addAll(tabGuia, tabDirectorio, tabMensaje);

        VBox raiz = new VBox(10, header, tabPane);
        raiz.setPadding(new Insets(10));
        raiz.setStyle("-fx-background-color: #14181f;");

        dialog.getDialogPane().setContent(raiz);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setStyle("-fx-background-color: #14181f;");

        // Aplicar estilos
        if (dialog.getDialogPane().getScene() != null) {
            UiUtils.aplicarTema(dialog.getDialogPane().getScene());
        }

        dialog.showAndWait();
    }

    private static VBox crearContenidoGuia() {
        String rol = Sesion.obtener().nombreRol();
        String usuario = Sesion.obtener().nombreUsuario();

        Label lblRol = new Label("Rol Activo: " + rol + " (" + usuario + ")");
        lblRol.setStyle("-fx-font-size: 13px; -fx-font-weight: 800; -fx-text-fill: #ffb347;");

        VBox infoRol = new VBox(6);
        infoRol.setPadding(new Insets(8));
        infoRol.setStyle("-fx-background-color: #1c222c; -fx-background-radius: 6px; -fx-border-color: #2a3342; -fx-border-width: 1px;");

        if ("GUARDA".equalsIgnoreCase(rol)) {
            infoRol.getChildren().addAll(
                    new Label("🛡️ Funciones del Guarda de Seguridad:"),
                    crearItemGuia("• Check-in / Check-out:", "Valide la identidad de personas aprobadas y registre su entrada o salida en torniquetes."),
                    crearItemGuia("• Invitado No Anunciado:", "Registre a visitantes que llegan sin cita previa; quedarán pendientes de aprobación del anfitrión."),
                    crearItemGuia("• Pase Temporal (Olvido):", "Expida pases a empleados que olvidaron su carnet (el sistema cerrará automáticamente visitas previas)."),
                    crearItemGuia("• Incidentes y Bloqueos:", "Reporte cualquier anomalía y bloquee preventivamente a infractores de seguridad.")
            );
        } else if ("FUNCIONARIO".equalsIgnoreCase(rol)) {
            infoRol.getChildren().addAll(
                    new Label("🏢 Funciones del Funcionario de Empresa:"),
                    crearItemGuia("• Pre-registrar Visitas:", "Agende con anticipación las visitas de clientes, contratistas o invitados a su departamento."),
                    crearItemGuia("• Aprobación de Accesos:", "Revise y apruebe o rechace solicitudes de ingreso pendientes para su área."),
                    crearItemGuia("• Gestión de Personal:", "Registre nuevos empleados o personal a su cargo en el módulo de Personas."),
                    crearItemGuia("• Reportar Incidentes:", "Notifique de inmediato situaciones de riesgo o irregularidades en su zona.")
            );
        } else {
            infoRol.getChildren().addAll(
                    new Label("🔑 Funciones del Administrador:"),
                    crearItemGuia("• Control Total RBAC:", "Creación y administración de usuarios, roles y asignación de permisos."),
                    crearItemGuia("• Gestión Integral de Personas:", "Altas, bajas, reactivaciones y bloqueos preventivos de acceso."),
                    crearItemGuia("• Auditoría y Evacuación:", "Consulta de la bitácora inmutable de eventos y exportación de listas de evacuación en CSV."),
                    crearItemGuia("• Incidentes y Accesos:", "Resolución y cierre formal de brechas e incidentes de seguridad.")
            );
        }

        VBox contenedor = new VBox(10, lblRol, infoRol);
        contenedor.setPadding(new Insets(12));
        return contenedor;
    }

    private static Node crearItemGuia(String titulo, String descripcion) {
        Label t = new Label(titulo);
        t.setStyle("-fx-font-weight: 800; -fx-text-fill: #e6e9ef;");
        Label d = new Label(descripcion);
        d.setWrapText(true);
        d.setStyle("-fx-font-size: 11px; -fx-text-fill: #9aa5b1;");
        return new VBox(1, t, d);
    }

    private static VBox crearContenidoDirectorio() {
        VBox contactos = new VBox(8);
        contactos.setPadding(new Insets(8));
        contactos.setStyle("-fx-background-color: #1c222c; -fx-background-radius: 6px; -fx-border-color: #2a3342; -fx-border-width: 1px;");

        contactos.getChildren().addAll(
                crearFilaDirectorio("🛡️ Central de Monitoreo y Seguridad:", "Extensión 101 | +57 (601) 555-0101"),
                crearFilaDirectorio("🚪 Portería y Control de Acceso Principal:", "Extensión 102 | Garita Norte"),
                crearFilaDirectorio("🚑 Brigada Médica y Primeros Auxilios:", "Extensión 103 | Radio Frecuencia #2"),
                crearFilaDirectorio("🔥 Brigada Contra Incendios / Evacuación:", "Extensión 104 | Alarma General"),
                crearFilaDirectorio("💻 Mesa de Ayuda TI y Soporte SICA:", "Extensión 200 | soporte.sica@acme.com")
        );

        Label nota = new Label("ℹ️ En caso de siniestro o emergencia grave, active la alarma de evacuación y diríjase al punto de encuentro.");
        nota.setWrapText(true);
        nota.setStyle("-fx-font-size: 11px; -fx-text-fill: #ffb347;");

        VBox contenedor = new VBox(10, contactos, nota);
        contenedor.setPadding(new Insets(12));
        return contenedor;
    }

    private static Node crearFilaDirectorio(String area, String contacto) {
        Label lblArea = new Label(area);
        lblArea.setStyle("-fx-font-weight: 800; -fx-text-fill: #ffffff; -fx-font-size: 12px;");
        Label lblContacto = new Label(contacto);
        lblContacto.setStyle("-fx-text-fill: #2fae63; -fx-font-weight: 700; -fx-font-size: 11px;");
        return new VBox(1, lblArea, lblContacto);
    }

    private static VBox crearContenidoMensaje() {
        TextField campoAsunto = new TextField();
        campoAsunto.setPromptText("Asunto de su mensaje...");

        ComboBox<String> comboCategoria = new ComboBox<>();
        comboCategoria.getItems().setAll(
                "Soporte Técnico / Error en Sistema",
                "Problema con Registro de Visita",
                "Falla en Torniquete o Lector de Acceso",
                "Solicitud de Ajuste de Permisos",
                "Consulta Operativa General",
                "Sugerencia de Mejora"
        );
        comboCategoria.setValue("Soporte Técnico / Error en Sistema");
        comboCategoria.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> comboPrioridad = new ComboBox<>();
        comboPrioridad.getItems().setAll("Baja", "Normal", "Alta", "Urgente");
        comboPrioridad.setValue("Normal");
        comboPrioridad.setMaxWidth(Double.MAX_VALUE);

        TextArea areaMensaje = new TextArea();
        areaMensaje.setPromptText("Escriba detalladamente su mensaje, consulta o reporte aquí...");
        areaMensaje.setPrefRowCount(4);
        areaMensaje.setWrapText(true);

        Button btnEnviar = new Button("📨 Enviar Mensaje a Soporte ZONA ACME");
        btnEnviar.getStyleClass().add("btn-verde");
        btnEnviar.setMaxWidth(Double.MAX_VALUE);

        Label lblFeedback = new Label();
        lblFeedback.setVisible(false);
        lblFeedback.setWrapText(true);

        btnEnviar.setOnAction(e -> {
            String asunto = campoAsunto.getText() == null ? "" : campoAsunto.getText().trim();
            String mensaje = areaMensaje.getText() == null ? "" : areaMensaje.getText().trim();

            if (asunto.isBlank() || mensaje.isBlank()) {
                lblFeedback.setText("⚠️ Por favor complete el asunto y el mensaje antes de enviar.");
                lblFeedback.setStyle("-fx-text-fill: #ff6b5e; -fx-font-weight: 700;");
                lblFeedback.setVisible(true);
                return;
            }

            long ticketNum = System.currentTimeMillis() % 100000;
            String ticketId = "TK-ACME-" + ticketNum;
            String hora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

            Alert exito = new Alert(Alert.AlertType.INFORMATION);
            exito.setTitle("Mensaje Enviado");
            exito.setHeaderText("✓ Mensaje de Soporte Registrado");
            exito.setContentText("Su mensaje ha sido enviado exitosamente al equipo de soporte de ZONA ACME.\n\n"
                    + "• Ticket ID: #" + ticketId + "\n"
                    + "• Remitente: " + Sesion.obtener().nombreUsuario() + " (" + Sesion.obtener().nombreRol() + ")\n"
                    + "• Categoría: " + comboCategoria.getValue() + "\n"
                    + "• Prioridad: " + comboPrioridad.getValue() + "\n"
                    + "• Fecha y Hora: " + hora + "\n\n"
                    + "El personal de seguridad y sistemas atenderá su solicitud a la brevedad.");
            exito.show();

            campoAsunto.clear();
            areaMensaje.clear();
            lblFeedback.setText("✓ Mensaje enviado con ticket #" + ticketId);
            lblFeedback.setStyle("-fx-text-fill: #2fae63; -fx-font-weight: 700;");
            lblFeedback.setVisible(true);
        });

        VBox form = new VBox(6,
                new Label("Asunto:"), campoAsunto,
                new Label("Categoría:"), comboCategoria,
                new Label("Prioridad:"), comboPrioridad,
                new Label("Mensaje / Detalle:"), areaMensaje,
                btnEnviar,
                lblFeedback
        );
        form.setPadding(new Insets(10));
        return form;
    }
}
