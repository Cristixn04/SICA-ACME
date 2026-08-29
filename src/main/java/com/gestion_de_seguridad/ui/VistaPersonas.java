package com.gestion_de_seguridad.ui;

import com.gestion_de_seguridad.personas.domain.model.Persona;
import com.gestion_de_seguridad.personas.domain.port.in.GestionarPersonaUseCase;
import com.gestion_de_seguridad.personas.infrastructure.config.PersonasContainer;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;

/**
 * Pantalla Gestión de Personal y Personas.
 *
 * Permite buscar, filtrar, dar de alta nuevos empleados/visitantes, y
 * bloquear/reactivar el acceso inmediatamente en todos los puntos de entrada.
 */
public final class VistaPersonas {

    private final GestionarPersonaUseCase personas = PersonasContainer.gestionar();

    @FXML
    private VBox raiz;
    @FXML
    private TextField buscador;
    @FXML
    private TableView<Persona> tabla;
    @FXML
    private TableColumn<Persona, String> colId;
    @FXML
    private TableColumn<Persona, String> colDni;
    @FXML
    private TableColumn<Persona, String> colNombre;
    @FXML
    private TableColumn<Persona, String> colPuesto;
    @FXML
    private TableColumn<Persona, String> colDepto;
    @FXML
    private TableColumn<Persona, String> colEmail;
    @FXML
    private TableColumn<Persona, String> colEstado;

    public Node crear() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/personas.fxml"));
            loader.setController(this);
            return loader.load();
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo cargar personas.fxml", ex);
        }
    }

    @FXML
    private void initialize() {
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        colDni.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDni()));
        colNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombreCompleto()));
        colPuesto.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPuesto() == null ? "Visitante" : c.getValue().getPuesto()));
        colDepto.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDepartamento() == null ? "General" : c.getValue().getDepartamento()));
        colEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmailCorporativo() == null ? "—" : c.getValue().getEmailCorporativo()));

        colEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstado().name()));
        colEstado.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(item);
                badge.getStyleClass().add("badge");
                switch (item) {
                    case "ACTIVO" -> badge.getStyleClass().add("badge-verde");
                    case "LICENCIA" -> badge.getStyleClass().add("badge-amarillo");
                    default -> badge.getStyleClass().add("badge-rojo");
                }
                setGraphic(badge);
            }
        });
        cargar("");
    }

    @FXML
    private void onBuscar() {
        cargar(buscador.getText());
    }

    @FXML
    private void onLimpiar() {
        buscador.clear();
        cargar("");
    }

    @FXML
    private void onAgregar() {
        agregarEmpleado();
    }

    @FXML
    private void onBloquear() {
        Persona seleccionada = tabla.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarMensaje(Alert.AlertType.WARNING, "Selección requerida", "Por favor seleccione una persona para bloquear su acceso.");
            return;
        }
        UiUtils.enHiloFondo(() -> {
            personas.bloquear(Sesion.obtener().idUsuario(), seleccionada.getId());
            return true;
        }, res -> {
            mostrarMensaje(Alert.AlertType.INFORMATION, "Acceso Bloqueado",
                    "El acceso de " + seleccionada.getNombreCompleto() + " ha sido bloqueado preventivamente.");
            cargar(buscador.getText());
        }, this::mostrarError);
    }

    @FXML
    private void onActivar() {
        Persona seleccionada = tabla.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarMensaje(Alert.AlertType.WARNING, "Selección requerida", "Por favor seleccione una persona para reactivar su acceso.");
            return;
        }
        UiUtils.enHiloFondo(() -> {
            personas.activar(Sesion.obtener().idUsuario(), seleccionada.getId());
            return true;
        }, res -> {
            mostrarMensaje(Alert.AlertType.INFORMATION, "Acceso Reactivado",
                    "El acceso de " + seleccionada.getNombreCompleto() + " se encuentra ACTIVO.");
            cargar(buscador.getText());
        }, this::mostrarError);
    }

    private void cargar(String texto) {
        String criterio = texto == null ? "" : texto.trim();
        UiUtils.enHiloFondo(
                () -> criterio.isBlank() ? personas.listarTodas() : personas.buscar(criterio),
                lista -> tabla.getItems().setAll(lista),
                ex -> {
                    tabla.getItems().clear();
                    mostrarError(ex.getMessage());
                });
    }

    private void agregarEmpleado() {
        TextField dni = new TextField(); dni.setPromptText("DNI (7-8 dígitos)");
        TextField nombre = new TextField(); nombre.setPromptText("Nombre completo");
        TextField puesto = new TextField(); puesto.setPromptText("Puesto / Rol (ej. Ingeniero, Visitante)");
        TextField departamento = new TextField(); departamento.setPromptText("Departamento (ej. TI, Operaciones)");
        TextField email = new TextField(); email.setPromptText("correo@empresa.com");

        VBox campos = new VBox(8,
                new Label("DNI:"), dni,
                new Label("Nombre completo:"), nombre,
                new Label("Puesto / Cargo:"), puesto,
                new Label("Departamento / Área:"), departamento,
                new Label("Email:"), email);
        campos.setPadding(new Insets(10));

        var alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Registrar Persona");
        alerta.setHeaderText("Alta de empleado, contratista o visitante");
        alerta.getDialogPane().setContent(campos);
        alerta.showAndWait().ifPresent(respuesta -> {
            if (respuesta.getButtonData().isDefaultButton()) {
                if (dni.getText() == null || dni.getText().isBlank() || nombre.getText() == null || nombre.getText().isBlank()) {
                    mostrarMensaje(Alert.AlertType.ERROR, "Validación", "DNI y Nombre completo son obligatorios.");
                    return;
                }
                UiUtils.enHiloFondo(
                        () -> personas.registrar(Sesion.obtener().idUsuario(), Persona.builder()
                                .dni(dni.getText().trim())
                                .nombreCompleto(nombre.getText().trim())
                                .puesto(puesto.getText())
                                .departamento(departamento.getText())
                                .emailCorporativo(email.getText())
                                .build()),
                        empleado -> cargar(""),
                        ex -> mostrarError(ex.getMessage()));
            }
        });
    }

    private void mostrarMensaje(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.show();
    }

    private void mostrarError(Throwable ex) {
        mostrarError(ex.getMessage());
    }

    private void mostrarError(String mensaje) {
        mostrarMensaje(Alert.AlertType.ERROR, "Error", mensaje == null ? "Error inesperado." : mensaje);
    }
}