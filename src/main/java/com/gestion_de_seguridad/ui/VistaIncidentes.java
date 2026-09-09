package com.gestion_de_seguridad.ui;

import com.gestion_de_seguridad.incidentes.domain.model.EstadoIncidente;
import com.gestion_de_seguridad.incidentes.domain.model.Incidente;
import com.gestion_de_seguridad.incidentes.domain.model.SeveridadIncidente;
import com.gestion_de_seguridad.incidentes.domain.port.in.GestionarIncidenteUseCase;
import com.gestion_de_seguridad.incidentes.infrastructure.config.IncidentesContainer;
import com.gestion_de_seguridad.personas.domain.model.Persona;
import com.gestion_de_seguridad.personas.domain.port.in.GestionarPersonaUseCase;
import com.gestion_de_seguridad.personas.infrastructure.config.PersonasContainer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pantalla Gestión de Incidentes.
 *
 * Permite reportar eventos anómalos o brechas de seguridad, categorizados
 * por severidad, con opción de bloqueo preventivo de acceso inmediato a
 * través de todos los puntos de entrada del complejo.
 */
public final class VistaIncidentes {

    private final GestionarIncidenteUseCase incidentes = IncidentesContainer.gestionar();
    private final GestionarPersonaUseCase personas = PersonasContainer.gestionar();

    @FXML
    private FlowPane grilla;

    public Node crear() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/incidentes.fxml"));
            loader.setController(this);
            return loader.load();
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo cargar incidentes.fxml", ex);
        }
    }

    @FXML
    private void initialize() {
        recargar();
    }

    @FXML
    private void onReportar() {
        reportarIncidente();
    }

    // =========================================================================
    // [EXAMEN - FUNCION 4: Exportar Incidentes a CSV con Stream API y Files NIO]
    // =========================================================================
    @FXML
    private void onExportarCsv() {
        UiUtils.enHiloFondo(() -> {
            List<Incidente> lista = incidentes.listar();
            if (lista.isEmpty()) {
                throw new IllegalStateException("No hay incidentes registrados para exportar.");
            }

            StringBuilder csv = new StringBuilder();
            csv.append("ID,Tipo,Severidad,Estado,PersonaID,Descripcion\n");

            // Uso de Stream API para transformar los incidentes en lineas CSV
            lista.stream().forEach(inc -> {
                String descLimpia = inc.getDescripcion() != null ? inc.getDescripcion().replace("\"", "\"\"").replace("\n", " ") : "";
                csv.append(inc.getId()).append(",")
                   .append("\"").append(inc.getTipo() != null ? inc.getTipo() : "").append("\",")
                   .append(inc.getSeveridad() != null ? inc.getSeveridad().name() : "").append(",")
                   .append(inc.getEstado() != null ? inc.getEstado().name() : "").append(",")
                   .append(inc.getPersonaId() != null ? inc.getPersonaId() : "").append(",")
                   .append("\"").append(descLimpia).append("\"\n");
            });

            String nombreArchivo = "incidentes_exportados.csv";
            java.nio.file.Path ruta = java.nio.file.Paths.get(System.getProperty("user.home"), nombreArchivo);
            java.nio.file.Files.writeString(ruta, csv.toString(), java.nio.charset.StandardCharsets.UTF_8);
            return ruta.toAbsolutePath().toString();
        }, rutaArchivo -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Exportación Exitosa");
            alert.setHeaderText("Incidentes exportados correctamente");
            alert.setContentText("El archivo CSV ha sido generado en:\n" + rutaArchivo);
            alert.show();
        }, this::mostrarError);
    }

    private void recargar() {
        UiUtils.enHiloFondo(() -> {
            List<Incidente> lista = incidentes.listar();
            Map<Long, String> nombres = new HashMap<>();
            for (Incidente inc : lista) {
                if (inc.getPersonaId() != null) {
                    nombres.put(inc.getPersonaId(), personaNombre(inc.getPersonaId()));
                }
            }
            return Map.entry(lista, nombres);
        }, resultado -> {
            grilla.getChildren().clear();
            for (Incidente inc : resultado.getKey()) {
                String nombre = resultado.getValue().getOrDefault(
                        inc.getPersonaId(), "Persona #" + inc.getPersonaId());
                grilla.getChildren().add(tarjeta(inc, nombre));
            }
        }, ex -> {
            grilla.getChildren().clear();
        });
    }

    private Node tarjeta(Incidente inc, String nombrePersona) {
        Label severidad = new Label("Severidad: " + inc.getSeveridad().name());
        severidad.getStyleClass().add("badge");
        switch (inc.getSeveridad()) {
            case ALTO -> severidad.getStyleClass().add("badge-rojo");
            case MEDIO -> severidad.getStyleClass().add("badge-naranja");
            default -> severidad.getStyleClass().add("badge-amarillo");
        }

        Label estado = new Label(inc.getEstado().name());
        estado.getStyleClass().add("badge");
        if (inc.getEstado() == EstadoIncidente.CERRADO) {
            estado.getStyleClass().add("badge-verde");
        } else if (inc.getEstado() == EstadoIncidente.EN_PROGRESO) {
            estado.getStyleClass().add("badge-amarillo");
        } else {
            estado.getStyleClass().add("badge-azul");
        }

        Label persona = new Label("Involucrado: " + nombrePersona);
        persona.getStyleClass().add("info-campo");
        Label tipo = new Label(inc.getTipo());
        tipo.getStyleClass().add("acme-titulo-seccion");
        Label desc = new Label(inc.getDescripcion());
        desc.setWrapText(true);
        desc.getStyleClass().add("info-valor");

        HBox encabezado = new HBox(10, severidad, estado);
        encabezado.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Botones de accion sobre el incidente
        Button btnProgreso = new Button("⏳ En Progreso");
        btnProgreso.getStyleClass().add("btn-gris");
        btnProgreso.setOnAction(e -> cambiarEstadoIncidente(inc.getId(), EstadoIncidente.EN_PROGRESO));

        Button btnCerrar = new Button("✓ Cerrar");
        btnCerrar.getStyleClass().add("btn-verde");
        btnCerrar.setOnAction(e -> cambiarEstadoIncidente(inc.getId(), EstadoIncidente.CERRADO));

        if (inc.getEstado() == EstadoIncidente.CERRADO) {
            btnProgreso.setDisable(true);
            btnCerrar.setDisable(true);
        }

        HBox acciones = new HBox(8, btnProgreso, btnCerrar);
        acciones.setPadding(new Insets(6, 0, 0, 0));

        VBox tarjeta = new VBox(8, encabezado, tipo, persona, desc, acciones);
        tarjeta.getStyleClass().add("info-tarjeta");
        tarjeta.setPrefWidth(320);
        tarjeta.setPadding(new Insets(14));
        return tarjeta;
    }

    private void cambiarEstadoIncidente(Long idIncidente, EstadoIncidente nuevoEstado) {
        UiUtils.enHiloFondo(() -> {
            incidentes.cambiarEstado(Sesion.obtener().idUsuario(), idIncidente, nuevoEstado);
            return true;
        }, res -> recargar(), this::mostrarError);
    }

    private String personaNombre(Long personaId) {
        if (personaId == null) {
            return "Sin asociar";
        }
        try {
            Persona p = personas.buscarPorId(personaId);
            return p == null ? ("Persona #" + personaId) : (p.getNombreCompleto() + " (" + p.getDni() + ")");
        } catch (Exception ex) {
            return "Persona #" + personaId;
        }
    }

    private void reportarIncidente() {
        TextField dni = new TextField(); dni.setPromptText("DNI de la persona (7-8 dígitos)");
        ComboBox<SeveridadIncidente> severidad = new ComboBox<>();
        severidad.getItems().setAll(SeveridadIncidente.values());
        severidad.setValue(SeveridadIncidente.MEDIO);
        TextField tipo = new TextField(); tipo.setPromptText("Tipo de incidente (ej. Acceso no autorizado, Daños)");
        TextArea desc = new TextArea(); desc.setPromptText("Descripción detallada del suceso..."); desc.setPrefRowCount(3);
        CheckBox chkBloquear = new CheckBox("Bloquear acceso preventivo de esta persona inmediatamente");
        chkBloquear.setSelected(true);

        VBox campos = new VBox(8,
                new Label("DNI de la Persona implicada:"), dni,
                new Label("Severidad:"), severidad,
                new Label("Tipo de Incidente:"), tipo,
                new Label("Descripción:"), desc,
                chkBloquear);
        campos.setPadding(new Insets(10));

        var alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Reportar Incidente");
        alerta.setHeaderText("Registro de nuevo incidente de seguridad");
        alerta.getDialogPane().setContent(campos);
        alerta.showAndWait().ifPresent(respuesta -> {
            if (respuesta.getButtonData().isDefaultButton()) {
                UiUtils.enHiloFondo(() -> {
                    if (dni.getText() == null || dni.getText().isBlank()) {
                        throw new IllegalStateException("Indique el DNI de la persona implicada.");
                    }
                    Persona p = personas.buscarPorDni(dni.getText().trim());
                    if (p == null) {
                        throw new IllegalStateException("No existe una persona con ese DNI.");
                    }
                    Incidente inc = new Incidente(null, p.getId(), tipo.getText(), desc.getText(),
                            severidad.getValue(), null, null);
                    incidentes.reportar(Sesion.obtener().idUsuario(), inc);

                    if (chkBloquear.isSelected()) {
                        incidentes.bloquearAccesoPersona(Sesion.obtener().idUsuario(), p.getId());
                    }
                    return inc;
                }, inc -> {
                    new Alert(Alert.AlertType.INFORMATION, "Incidente registrado exitosamente.").show();
                    recargar();
                }, this::mostrarError);
            }
        });
    }

    private void mostrarError(Throwable ex) {
        String texto = ex.getMessage() == null ? "Error inesperado." : ex.getMessage();
        new Alert(Alert.AlertType.ERROR, texto).show();
    }
}