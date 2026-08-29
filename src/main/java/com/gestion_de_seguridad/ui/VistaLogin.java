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
    @FXML
    private Label linkSolicitar;

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
        linkSolicitar.setOnMouseClicked(e -> onSolicitarAcceso());
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

    private void onSolicitarAcceso() {
        var personasUc = com.gestion_de_seguridad.personas.infrastructure.config.PersonasContainer.gestionar();
        var accesoUc = com.gestion_de_seguridad.acceso.infrastructure.config.AccesoContainer.gestionar();

        TextField dniField = new TextField();
        dniField.setPromptText("DNI o Pasaporte (7-8 dígitos)");
        TextField nombreField = new TextField();
        nombreField.setPromptText("Nombre y apellidos completos");
        TextField empresaField = new TextField();
        empresaField.setPromptText("Empresa externa, contratista o particular");
        TextField emailField = new TextField();
        emailField.setPromptText("correo@ejemplo.com");

        javafx.scene.control.ComboBox<com.gestion_de_seguridad.personas.domain.model.Persona> comboAnfitrion = new javafx.scene.control.ComboBox<>();
        comboAnfitrion.setPromptText("Seleccione el funcionario que lo recibirá...");
        comboAnfitrion.setMaxWidth(Double.MAX_VALUE);

        // Cargar anfitriones disponibles
        UiUtils.enHiloFondo(
                () -> personasUc.listarTodas().stream()
                        .filter(p -> p.getEstado() == com.gestion_de_seguridad.personas.domain.model.EstadoPersona.ACTIVO)
                        .toList(),
                lista -> {
                    comboAnfitrion.getItems().setAll(lista);
                    comboAnfitrion.setConverter(new javafx.util.StringConverter<>() {
                        @Override
                        public String toString(com.gestion_de_seguridad.personas.domain.model.Persona p) {
                            if (p == null) return "";
                            String pto = p.getPuesto() != null ? " - " + p.getPuesto() : "";
                            return p.getNombreCompleto() + pto + " (" + p.getDni() + ")";
                        }

                        @Override
                        public com.gestion_de_seguridad.personas.domain.model.Persona fromString(String s) {
                            return null;
                        }
                    });
                    if (!lista.isEmpty()) {
                        comboAnfitrion.setValue(lista.get(0));
                    }
                },
                ex -> {}
        );

        TextField motivoField = new TextField();
        motivoField.setPromptText("Motivo del ingreso (ej. Reunión de trabajo, Auditoría)");
        javafx.scene.control.DatePicker fechaPicker = new javafx.scene.control.DatePicker(java.time.LocalDate.now());
        fechaPicker.setMaxWidth(Double.MAX_VALUE);

        VBox form = new VBox(8,
                new Label("DNI / Documento de Identidad:"), dniField,
                new Label("Nombre Completo del Solicitante:"), nombreField,
                new Label("Empresa / Procedencia:"), empresaField,
                new Label("Correo Electrónico de Contacto:"), emailField,
                new Label("Funcionario o Área Anfitriona a Visitar:"), comboAnfitrion,
                new Label("Motivo de la Visita:"), motivoField,
                new Label("Fecha Programada:"), fechaPicker
        );
        form.setPadding(new javafx.geometry.Insets(12));

        javafx.scene.control.Alert dialog = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Solicitud de Acceso - ZONA ACME");
        dialog.setHeaderText("Registro de Solicitud Externa de Ingreso");
        dialog.getDialogPane().setContent(form);

        dialog.showAndWait().ifPresent(res -> {
            if (res.getButtonData().isDefaultButton()) {
                String dniRaw = dniField.getText() == null ? "" : dniField.getText().trim();
                String nombreRaw = nombreField.getText() == null ? "" : nombreField.getText().trim();
                String empresaRaw = empresaField.getText() == null ? "" : empresaField.getText().trim();
                String emailRaw = emailField.getText() == null ? "" : emailField.getText().trim();
                String motivoRaw = motivoField.getText() == null ? "" : motivoField.getText().trim();
                com.gestion_de_seguridad.personas.domain.model.Persona anfitrion = comboAnfitrion.getValue();

                if (dniRaw.isBlank() || nombreRaw.isBlank()) {
                    javafx.scene.control.Alert err = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                    err.setTitle("Validación");
                    err.setHeaderText(null);
                    err.setContentText("El DNI y el Nombre Completo son datos obligatorios.");
                    err.show();
                    return;
                }

                final String finalDni = dniRaw;
                final String finalNombre = nombreRaw;
                final String finalEmpresa = empresaRaw.isBlank() ? "Particular" : empresaRaw;
                final String finalEmail = emailRaw;
                final String finalMotivo = motivoRaw.isBlank() ? "Acceso solicitado por portal" : motivoRaw;
                final com.gestion_de_seguridad.personas.domain.model.Persona finalAnfitrion = anfitrion;

                UiUtils.enHiloFondo(() -> {
                    com.gestion_de_seguridad.personas.domain.model.Persona p;
                    try {
                        p = personasUc.buscarPorDni(finalDni);
                    } catch (Exception notFound) {
                        p = personasUc.registrar(1L, com.gestion_de_seguridad.personas.domain.model.Persona.builder()
                                .dni(finalDni)
                                .nombreCompleto(finalNombre)
                                .puesto("Visitante (" + finalEmpresa + ")")
                                .departamento("Externo")
                                .emailCorporativo(finalEmail.isBlank() ? null : finalEmail)
                                .build());
                    }

                    java.time.LocalDateTime fHora = fechaPicker.getValue() != null
                            ? fechaPicker.getValue().atTime(java.time.LocalTime.of(9, 0))
                            : java.time.LocalDateTime.now();

                    com.gestion_de_seguridad.acceso.domain.model.Visita visita = com.gestion_de_seguridad.acceso.domain.model.Visita.builder()
                            .personaId(p.getId())
                            .personaVisitadaId(finalAnfitrion != null ? finalAnfitrion.getId() : null)
                            .empresaPropietariaId(finalAnfitrion != null ? finalAnfitrion.getEmpresaId() : null)
                            .motivo(finalMotivo)
                            .fechaHoraVisita(fHora)
                            .build();

                    return accesoUc.crearVisita(1L, com.gestion_de_seguridad.acceso.infrastructure.config.AccesoContainer.preRegistrada(), visita);
                }, (com.gestion_de_seguridad.acceso.domain.model.Visita nuevaVisita) -> {
                    javafx.scene.control.Alert exito = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                    exito.setTitle("Solicitud Registrada");
                    exito.setHeaderText("✓ Solicitud de Acceso Enviada");
                    exito.setContentText("Su solicitud ha sido registrada exitosamente con el ID #" + nuevaVisita.getId() + ".\n\n"
                            + "• Solicitante: " + finalNombre + " (DNI: " + finalDni + ")\n"
                            + "• Anfitrión: " + (finalAnfitrion != null ? finalAnfitrion.getNombreCompleto() : "General") + "\n"
                            + "• Estado inicial: PENDIENTE DE APROBACIÓN\n\n"
                            + "Cuando se presente en la garita de control de ZONA ACME, mencione su DNI para realizar el check-in una vez aprobada.");
                    exito.show();
                }, ex -> {
                    javafx.scene.control.Alert err = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                    err.setTitle("Error al Registrar");
                    err.setHeaderText("No se pudo procesar la solicitud");
                    err.setContentText(ex.getMessage() != null ? ex.getMessage() : "Error inesperado.");
                    err.show();
                });
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