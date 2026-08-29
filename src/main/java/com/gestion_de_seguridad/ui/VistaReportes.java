package com.gestion_de_seguridad.ui;

import com.gestion_de_seguridad.acceso.domain.model.EstadoVisita;
import com.gestion_de_seguridad.auditoria.domain.model.RegistroAuditoria;
import com.gestion_de_seguridad.auditoria.domain.port.in.RegistrarAuditoriaUseCase;
import com.gestion_de_seguridad.auditoria.infrastructure.config.AuditoriaContainer;
import com.gestion_de_seguridad.incidentes.domain.model.SeveridadIncidente;
import com.gestion_de_seguridad.personas.domain.model.Persona;
import com.gestion_de_seguridad.reportes.domain.port.in.GenerarReporteUseCase;
import com.gestion_de_seguridad.reportes.infrastructure.config.ReportesContainer;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Pantalla de Reportes, Métricas, Evacuación y Auditoría.
 *
 * Utiliza fuertemente Java Stream API y lambdas para calcular indicadores
 * en tiempo real sin bloquear el hilo de interfaz gráfica de usuario.
 */
public final class VistaReportes {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final GenerarReporteUseCase reportes = ReportesContainer.generar();
    private final RegistrarAuditoriaUseCase auditoria = AuditoriaContainer.usar();

    @FXML
    private FlowPane metricas;
    @FXML
    private VBox detalleVisitas;
    @FXML
    private VBox detalleIncidentes;

    @FXML
    private TableView<Persona> tablaEvacuacion;
    @FXML
    private TableColumn<Persona, String> colEvaDni;
    @FXML
    private TableColumn<Persona, String> colEvaNombre;
    @FXML
    private TableColumn<Persona, String> colEvaPuesto;
    @FXML
    private TableColumn<Persona, String> colEvaDepto;
    @FXML
    private TableColumn<Persona, String> colEvaEstado;

    @FXML
    private TableView<RegistroAuditoria> tablaAuditoria;
    @FXML
    private TableColumn<RegistroAuditoria, String> colAudId;
    @FXML
    private TableColumn<RegistroAuditoria, String> colAudFecha;
    @FXML
    private TableColumn<RegistroAuditoria, String> colAudUsuario;
    @FXML
    private TableColumn<RegistroAuditoria, String> colAudAccion;
    @FXML
    private TableColumn<RegistroAuditoria, String> colAudEntidad;
    @FXML
    private TableColumn<RegistroAuditoria, String> colAudDetalles;

    private List<Persona> listaDentroActual = List.of();

    private record ListaMetricas(
            long personasEnComplejo,
            long visitasDelDia,
            long incidentesAbiertos,
            double tasaAprobacion,
            Map<EstadoVisita, Long> visitasPorEstado,
            Map<SeveridadIncidente, Long> incidentesPorSeveridad,
            List<Persona> personasDentro,
            List<RegistroAuditoria> auditoriasRecientes) {
    }

    public Node crear() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/reportes.fxml"));
            loader.setController(this);
            return loader.load();
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo cargar reportes.fxml", ex);
        }
    }

    @FXML
    private void initialize() {
        configurarTablas();
        recargar();
    }

    private void configurarTablas() {
        // Configurar tabla de evacuacion
        tablaEvacuacion.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        colEvaDni.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDni()));
        colEvaNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombreCompleto()));
        colEvaPuesto.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPuesto() == null ? "Visitante" : c.getValue().getPuesto()));
        colEvaDepto.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDepartamento() == null ? "General" : c.getValue().getDepartamento()));
        colEvaEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstado().name()));
        colEvaEstado.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(item);
                badge.getStyleClass().addAll("badge", "badge-azul");
                setGraphic(badge);
            }
        });

        // Configurar tabla de auditoria
        tablaAuditoria.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        colAudId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        colAudFecha.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFechaHora().format(FORMATO_FECHA)));
        colAudUsuario.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUsuario() == null ? "SISTEMA" : c.getValue().getUsuario()));
        colAudAccion.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAccion()));
        colAudEntidad.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEntidad() == null ? "—" : c.getValue().getEntidad()));
        colAudDetalles.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDetalles() == null ? "—" : c.getValue().getDetalles()));
    }

    @FXML
    private void onRefrescar() {
        recargar();
    }

    @FXML
    private void onExportarCsv() {
        if (listaDentroActual.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "No hay personas registradas dentro del complejo en este momento.").show();
            return;
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "evacuacion_zona_acme_" + timestamp + ".csv";
        File file = new File(System.getProperty("user.home"), filename);

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("DNI,Nombre Completo,Puesto,Departamento,Estado");
            for (Persona p : listaDentroActual) {
                String puesto = p.getPuesto() == null ? "Visitante" : p.getPuesto();
                String depto = p.getDepartamento() == null ? "General" : p.getDepartamento();
                writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                        p.getDni(), p.getNombreCompleto(), puesto, depto, p.getEstado().name());
            }
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Exportación Exitosa");
            alert.setHeaderText("Reporte de Evacuación Generado");
            alert.setContentText("El archivo se ha exportado correctamente en:\n" + file.getAbsolutePath());
            alert.show();
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Error al exportar archivo CSV: " + e.getMessage()).show();
        }
    }

    private void recargar() {
        UiUtils.enHiloFondo(this::calcularTodo, this::pintar, ex -> {
            // ante error se deja vacio
        });
    }

    private ListaMetricas calcularTodo() {
        return new ListaMetricas(
                reportes.personasEnComplejo(),
                reportes.visitasDelDia(),
                reportes.incidentesAbiertos(),
                reportes.tasaAprobacion(),
                reportes.visitasPorEstado(),
                reportes.incidentesPorSeveridad(),
                reportes.personasActualmentoDentro(),
                auditoria.listarRecientes(100));
    }

    private void pintar(ListaMetricas m) {
        listaDentroActual = m.personasDentro();

        metricas.getChildren().clear();
        metricas.getChildren().add(tarjetaMetrica("Personas en Complejo",
                String.valueOf(m.personasEnComplejo()), "metrica-valor"));
        metricas.getChildren().add(tarjetaMetrica("Visitas del Día",
                String.valueOf(m.visitasDelDia()), "metrica-valor-verde"));
        metricas.getChildren().add(tarjetaMetrica("Incidentes Abiertos",
                String.valueOf(m.incidentesAbiertos()), "metrica-valor-rojo"));
        metricas.getChildren().add(tarjetaMetrica("Tasa de Aprobación",
                String.format("%.0f%%", m.tasaAprobacion() * 100), "metrica-valor"));

        detalleVisitas.getChildren().clear();
        for (Map.Entry<EstadoVisita, Long> e : m.visitasPorEstado().entrySet()) {
            Label l = new Label(e.getKey() + ":  " + e.getValue());
            l.getStyleClass().add("info-valor");
            detalleVisitas.getChildren().add(l);
        }

        detalleIncidentes.getChildren().clear();
        for (Map.Entry<SeveridadIncidente, Long> e : m.incidentesPorSeveridad().entrySet()) {
            Label l = new Label(e.getKey() + ":  " + e.getValue());
            l.getStyleClass().add("info-valor");
            detalleIncidentes.getChildren().add(l);
        }

        tablaEvacuacion.setItems(FXCollections.observableArrayList(m.personasDentro()));
        tablaAuditoria.setItems(FXCollections.observableArrayList(m.auditoriasRecientes()));
    }

    private Node tarjetaMetrica(String etiqueta, String valor, String valorClase) {
        Label lblValor = new Label(valor);
        lblValor.getStyleClass().add(valorClase);
        Label lblEtiqueta = new Label(etiqueta);
        lblEtiqueta.getStyleClass().add("metrica-titulo");
        VBox tarjeta = new VBox(6, lblValor, lblEtiqueta);
        tarjeta.getStyleClass().add("metrica");
        tarjeta.setPrefWidth(200);
        tarjeta.setPrefHeight(105);
        tarjeta.setPadding(new Insets(14));
        tarjeta.setAlignment(Pos.CENTER_LEFT);
        return tarjeta;
    }
}