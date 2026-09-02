package com.gestion_de_seguridad.ui;

import com.gestion_de_seguridad.acceso.domain.event.CheckInRealizadoEvent;
import com.gestion_de_seguridad.acceso.domain.event.CheckOutRealizadoEvent;
import com.gestion_de_seguridad.acceso.domain.event.VisitaAprobadaEvent;
import com.gestion_de_seguridad.acceso.domain.event.VisitaCerradaPorSistemaEvent;
import com.gestion_de_seguridad.acceso.domain.event.VisitaCreadaEvent;
import com.gestion_de_seguridad.acceso.domain.event.VisitaRechazadaEvent;
import com.gestion_de_seguridad.acceso.domain.model.EstadoVisita;
import com.gestion_de_seguridad.acceso.domain.model.Visita;
import com.gestion_de_seguridad.acceso.domain.port.in.GestionarVisitaUseCase;
import com.gestion_de_seguridad.acceso.infrastructure.config.AccesoContainer;
import com.gestion_de_seguridad.personas.domain.model.Persona;
import com.gestion_de_seguridad.personas.domain.port.in.GestionarPersonaUseCase;
import com.gestion_de_seguridad.personas.infrastructure.config.PersonasContainer;
import com.gestion_de_seguridad.shared.infrastructure.config.SharedContainer;
import com.gestion_de_seguridad.usuarios.infrastructure.config.UsuariosContainer;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Pantalla principal de Control de Acceso y Check-in (Guarda / Funcionario / Admin).
 *
 * Resuelve de forma interactiva y en tiempo real todos los escenarios de acceso:
 * 1. Invitado Pre-registrado (Check-in directo).
 * 2. Invitado No Anunciado (Creación -> Notificación en tiempo real -> Aprobación -> Check-in).
 * 3. Trabajador con Carnet Olvidado (Pase Temporal).
 * 4. Salida Olvidada (Regularización automática y auditoría).
 *
 * Utiliza el patrón Observer para refrescarse reactivamente ante cualquier
 * evento publicado en el sistema.
 */
public final class VistaCheckIn implements AutoCloseable {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final GestionarVisitaUseCase acceso = AccesoContainer.gestionar();
    private final GestionarPersonaUseCase personas = PersonasContainer.gestionar();

    // Listeners del patron Observer para actualizar la UI en tiempo real
    private final Consumer<VisitaAprobadaEvent> onAprobada = e -> Platform.runLater(this::recargar);
    private final Consumer<VisitaRechazadaEvent> onRechazada = e -> Platform.runLater(this::recargar);
    private final Consumer<VisitaCreadaEvent> onCreada = e -> Platform.runLater(this::recargar);
    private final Consumer<CheckInRealizadoEvent> onCheckinEvt = e -> Platform.runLater(this::recargar);
    private final Consumer<CheckOutRealizadoEvent> onCheckoutEvt = e -> Platform.runLater(this::recargar);
    private final Consumer<VisitaCerradaPorSistemaEvent> onCerradaEvt = e -> Platform.runLater(this::recargar);

    @FXML
    private VBox raiz;
    @FXML
    private TextField buscador;
    @FXML
    private ComboBox<String> comboFiltro;
    @FXML
    private Button btnBuscar;
    @FXML
    private Button btnPreRegistrar;
    @FXML
    private Button btnNoAnunciado;
    @FXML
    private Button btnOlvidoCarnet;
    @FXML
    private Button btnRefrescar;

    @FXML
    private TableView<Visita> tablaVisitas;
    @FXML
    private TableColumn<Visita, String> colId;
    @FXML
    private TableColumn<Visita, String> colPersona;
    @FXML
    private TableColumn<Visita, String> colAnfitrion;
    @FXML
    private TableColumn<Visita, String> colMotivo;
    @FXML
    private TableColumn<Visita, String> colEstado;
    @FXML
    private TableColumn<Visita, String> colFecha;

    @FXML
    private VBox panelDetalle;
    @FXML
    private StackPane avatarSlot;
    @FXML
    private Label lblNombrePersona;
    @FXML
    private Label lblDniPersona;
    @FXML
    private Label lblEmpresaPersona;
    @FXML
    private Label lblAnfitrion;
    @FXML
    private Label lblMotivo;
    @FXML
    private Label lblEstado;
    @FXML
    private Label lblHorarios;

    @FXML
    private Button btnCheckin;
    @FXML
    private Button btnCheckout;
    @FXML
    private Button btnAprobar;
    @FXML
    private Button btnRechazar;

    private final ObservableList<Visita> datosTabla = FXCollections.observableArrayList();
    private final Map<Long, Persona> cachePersonas = new HashMap<>();
    private Visita visitaActual;

    public Node crear() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/checkin.fxml"));
            loader.setController(this);
            return loader.load();
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo cargar checkin.fxml", ex);
        }
    }

    @FXML
    private void initialize() {
        configurarFiltros();
        configurarPermisosPorRol();
        configurarTabla();
        deshabilitarAcciones();
        mostrarDetalleVacio();

        // Suscripcion a eventos de dominio en tiempo real
        SharedContainer.eventPublisher().suscribir(VisitaAprobadaEvent.class, onAprobada);
        SharedContainer.eventPublisher().suscribir(VisitaRechazadaEvent.class, onRechazada);
        SharedContainer.eventPublisher().suscribir(VisitaCreadaEvent.class, onCreada);
        SharedContainer.eventPublisher().suscribir(CheckInRealizadoEvent.class, onCheckinEvt);
        SharedContainer.eventPublisher().suscribir(CheckOutRealizadoEvent.class, onCheckoutEvt);
        SharedContainer.eventPublisher().suscribir(VisitaCerradaPorSistemaEvent.class, onCerradaEvt);

        // Carga inicial
        recargar();
    }

    private void configurarPermisosPorRol() {
        String rol = Sesion.obtener().nombreRol();
        if ("GUARDA".equalsIgnoreCase(rol)) {
            // El Guarda de Seguridad solo puede registrar visitas de invitado no anunciado y pase temporal por olvido
            btnPreRegistrar.setVisible(false);
            btnPreRegistrar.setManaged(false);
            btnNoAnunciado.setVisible(true);
            btnNoAnunciado.setManaged(true);
            btnOlvidoCarnet.setVisible(true);
            btnOlvidoCarnet.setManaged(true);
        } else if ("FUNCIONARIO".equalsIgnoreCase(rol)) {
            // El Funcionario pre-registra visitas programadas para sus invitados
            btnPreRegistrar.setVisible(true);
            btnPreRegistrar.setManaged(true);
            btnNoAnunciado.setVisible(false);
            btnNoAnunciado.setManaged(false);
            btnOlvidoCarnet.setVisible(false);
            btnOlvidoCarnet.setManaged(false);
        } else {
            // ADMINISTRADOR: acceso total a las 3 acciones
            btnPreRegistrar.setVisible(true);
            btnPreRegistrar.setManaged(true);
            btnNoAnunciado.setVisible(true);
            btnNoAnunciado.setManaged(true);
            btnOlvidoCarnet.setVisible(true);
            btnOlvidoCarnet.setManaged(true);
        }
    }

    private void configurarFiltros() {
        comboFiltro.getItems().setAll(
                "Todas las Visitas",
                "⏳ Pendientes de Aprobación",
                "🏢 Personas Dentro Hoy",
                "✓ Visitas Aprobadas"
        );
        comboFiltro.setValue("Todas las Visitas");
    }

    private void configurarTabla() {
        tablaVisitas.setItems(datosTabla);
        tablaVisitas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        colPersona.setCellValueFactory(c -> {
            Persona p = cachePersonas.get(c.getValue().getPersonaId());
            String desc = (p != null) ? (p.getNombreCompleto() + " (" + p.getDni() + ")") : ("Persona #" + c.getValue().getPersonaId());
            return new SimpleStringProperty(desc);
        });
        colAnfitrion.setCellValueFactory(c -> {
            Long anfitrionId = c.getValue().getPersonaVisitadaId();
            if (anfitrionId == null) return new SimpleStringProperty("—");
            Persona p = cachePersonas.get(anfitrionId);
            return new SimpleStringProperty(p != null ? p.getNombreCompleto() : ("ID #" + anfitrionId));
        });
        colMotivo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMotivo() == null ? "—" : c.getValue().getMotivo()));
        colFecha.setCellValueFactory(c -> {
            LocalDateTime f = c.getValue().getFechaHoraVisita();
            return new SimpleStringProperty(f != null ? f.format(FORMATO_FECHA) : "—");
        });

        colEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstado().name()));
        colEstado.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(formatearNombreEstado(item));
                badge.getStyleClass().add("badge");
                asignarClaseBadge(badge, item);
                setGraphic(badge);
            }
        });

        tablaVisitas.getSelectionModel().selectedItemProperty().addListener((obs, antes, seleccionado) -> {
            if (seleccionado != null) {
                visitaActual = seleccionado;
                mostrarDetalle(seleccionado);
            }
        });
    }

    @FXML
    private void onBuscar() {
        recargar();
    }

    @FXML
    private void onFiltroCambiado() {
        recargar();
    }

    @FXML
    private void onRefrescar() {
        recargar();
    }

    @FXML
    private void onCheckin() {
        if (visitaActual == null) return;
        ejecutarAccion(() -> acceso.registrarCheckIn(Sesion.obtener().idUsuario(), visitaActual.getId()),
                "Check-in registrado exitosamente. La persona ha ingresado al complejo.");
    }

    @FXML
    private void onCheckout() {
        if (visitaActual == null) return;
        ejecutarAccion(() -> acceso.registrarCheckOut(Sesion.obtener().idUsuario(), visitaActual.getId()),
                "Check-out registrado exitosamente. Salida completada.");
    }

    @FXML
    private void onAprobar() {
        if (visitaActual == null) return;
        ejecutarAccion(() -> acceso.aprobar(Sesion.obtener().idUsuario(), visitaActual.getId()),
                "Visita aprobada exitosamente. El guarda ya puede realizar el check-in.");
    }

    @FXML
    private void onRechazar() {
        if (visitaActual == null) return;
        ejecutarAccion(() -> acceso.rechazar(Sesion.obtener().idUsuario(), visitaActual.getId()),
                "Visita rechazada.");
    }

    /**
     * Dialogo interactivo para el Escenario 1: Invitado Pre-registrado.
     * Solo disponible para Funcionarios y Administradores.
     */
    @FXML
    private void onPreRegistrar() {
        if ("GUARDA".equalsIgnoreCase(Sesion.obtener().nombreRol())) {
            mostrarAlerta(Alert.AlertType.ERROR, "Acceso Denegado",
                    "El rol Guarda de Seguridad solo tiene permitido registrar visitas no anunciadas y pases temporales por olvido de carnet.");
            return;
        }
        mostrarDialogoCrearVisita("Pre-registrar Invitado",
                "Registro previo de visita (Estado quedará como APROBADO)",
                AccesoContainer.preRegistrada(), false);
    }

    /**
     * Dialogo interactivo para el Escenario 2: Invitado No Anunciado.
     */
    @FXML
    private void onNoAnunciado() {
        mostrarDialogoCrearVisita("Invitado No Anunciado",
                "Llegada sin cita previa (Estado quedará como PENDIENTE DE APROBACIÓN)",
                AccesoContainer.noAnunciada(), false);
    }

    /**
     * Dialogo interactivo para el Escenario 3: Trabajador con Carnet Olvidado.
     */
    @FXML
    private void onOlvidoCarnet() {
        mostrarDialogoCrearVisita("Pase Temporal - Carnet Olvidado",
                "Solicitud de ingreso puntual para trabajador sin documento",
                AccesoContainer.porOlvidoCarnet(), true);
    }

    private void mostrarDialogoCrearVisita(String titulo, String subtitulo,
                                          com.gestion_de_seguridad.acceso.domain.model.EstrategiaCreacionVisita estrategia,
                                          boolean soloTrabajadores) {
        TextField dniField = new TextField();
        dniField.setPromptText("DNI de la Persona (7-8 dígitos)");
        TextField nombreField = new TextField();
        nombreField.setPromptText("Nombre completo (si es visitante nuevo)");
        TextField motivoField = new TextField();
        motivoField.setPromptText("Motivo de la visita / trabajo");
        DatePicker fechaPicker = new DatePicker(LocalDate.now());

        ComboBox<Persona> comboAnfitrion = new ComboBox<>();
        comboAnfitrion.setPromptText("Seleccione funcionario anfitrión");
        comboAnfitrion.setPrefWidth(350);

        List<Persona> todas = personas.listarTodas();
        comboAnfitrion.getItems().setAll(todas.stream()
                .filter(p -> p.getPuesto() != null && !p.getPuesto().isBlank())
                .collect(Collectors.toList()));
        comboAnfitrion.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Persona p) {
                return p == null ? "" : (p.getNombreCompleto() + " (" + p.getPuesto() + ")");
            }
            @Override
            public Persona fromString(String string) { return null; }
        });

        VBox content = new VBox(10);
        content.setPadding(new Insets(12));
        content.getChildren().addAll(
                new Label("DNI:"), dniField,
                new Label("Nombre completo (para nuevos):"), nombreField,
                new Label("Anfitrión / Funcionario a visitar:"), comboAnfitrion,
                new Label("Motivo:"), motivoField,
                new Label("Fecha de la visita:"), fechaPicker
        );

        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle(titulo);
        dialog.setHeaderText(subtitulo);
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait().ifPresent(response -> {
            if (response.getButtonData().isDefaultButton()) {
                String dniRaw = dniField.getText() == null ? "" : dniField.getText().trim();
                String nombreRaw = nombreField.getText() == null ? "" : nombreField.getText().trim();
                String motivoRaw = motivoField.getText() == null ? "" : motivoField.getText().trim();
                Persona anfitrion = comboAnfitrion.getValue();

                if (dniRaw.isBlank()) {
                    mostrarAlerta(Alert.AlertType.ERROR, "Validación", "Debe ingresar el DNI de la persona.");
                    return;
                }

                final String finalDni = dniRaw;
                final String finalNombre = nombreRaw.isBlank() ? ("Visitante DNI " + dniRaw) : nombreRaw;
                final String finalMotivo = motivoRaw.isBlank() ? "Acceso al complejo" : motivoRaw;
                final Persona finalAnfitrion = anfitrion;

                UiUtils.enHiloFondo(() -> {
                    Persona p = personas.buscarPorDni(finalDni);
                    if (p == null) {
                        p = personas.registrar(Sesion.obtener().idUsuario(), Persona.builder()
                                .dni(finalDni)
                                .nombreCompleto(finalNombre)
                                .puesto(soloTrabajadores ? "Empleado" : "Visitante")
                                .build());
                    }

                    LocalDateTime fHora = fechaPicker.getValue() != null
                            ? fechaPicker.getValue().atTime(LocalDateTime.now().toLocalTime())
                            : LocalDateTime.now();

                    Visita datos = Visita.builder()
                            .personaId(p.getId())
                            .personaVisitadaId(finalAnfitrion != null ? finalAnfitrion.getId() : null)
                            .empresaPropietariaId(finalAnfitrion != null ? finalAnfitrion.getEmpresaId() : null)
                            .motivo(finalMotivo)
                            .fechaHoraVisita(fHora)
                            .build();

                    return acceso.crearVisita(Sesion.obtener().idUsuario(), estrategia, datos);
                }, nuevaVisita -> {
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Visita Creada",
                            "Visita #" + nuevaVisita.getId() + " creada con estado: " + nuevaVisita.getEstado());
                    recargar();
                }, ex -> mostrarAlerta(Alert.AlertType.ERROR, "Error al crear visita", ex.getMessage()));
            }
        });
    }

    private void recargar() {
        String texto = buscador.getText() == null ? "" : buscador.getText().trim();
        String filtro = comboFiltro.getValue() == null ? "Todas las Visitas" : comboFiltro.getValue();

        UiUtils.enHiloFondo(() -> {
            // Actualizar cache de personas
            List<Persona> todasPersonas = personas.listarTodas();
            Map<Long, Persona> mapa = new HashMap<>();
            for (Persona p : todasPersonas) {
                mapa.put(p.getId(), p);
            }

            List<Visita> lista = texto.isBlank() ? acceso.listarTodas() : acceso.buscarPorTexto(texto);

            // Filtrar segun seleccion
            if ("⏳ Pendientes de Aprobación".equals(filtro)) {
                lista = lista.stream()
                        .filter(v -> v.getEstado() == EstadoVisita.PENDIENTE_APROBACION
                                || v.getEstado() == EstadoVisita.PENDIENTE_APROBACION_POR_OLVIDO)
                        .collect(Collectors.toList());
            } else if ("🏢 Personas Dentro Hoy".equals(filtro)) {
                lista = lista.stream()
                        .filter(v -> v.getEstado() == EstadoVisita.DENTRO || v.getEstado() == EstadoVisita.CHECK_IN)
                        .collect(Collectors.toList());
            } else if ("✓ Visitas Aprobadas".equals(filtro)) {
                lista = lista.stream()
                        .filter(v -> v.getEstado() == EstadoVisita.APROBADO)
                        .collect(Collectors.toList());
            }
            return Map.entry(mapa, lista);
        }, resultado -> {
            cachePersonas.clear();
            cachePersonas.putAll(resultado.getKey());
            datosTabla.setAll(resultado.getValue());

            if (visitaActual != null) {
                resultado.getValue().stream()
                        .filter(v -> v.getId().equals(visitaActual.getId()))
                        .findFirst()
                        .ifPresentOrElse(this::mostrarDetalle, this::mostrarDetalleVacio);
            } else if (!resultado.getValue().isEmpty()) {
                tablaVisitas.getSelectionModel().select(0);
            }
        }, ex -> {
            datosTabla.clear();
            mostrarDetalleVacio();
        });
    }

    private void mostrarDetalle(Visita v) {
        visitaActual = v;
        Persona p = cachePersonas.get(v.getPersonaId());
        String nombre = p != null ? p.getNombreCompleto() : ("Visitante #" + v.getPersonaId());
        String dni = p != null ? p.getDni() : "—";
        String empresa = p != null && p.getDepartamento() != null ? p.getDepartamento() : "Visitante Externo";

        lblNombrePersona.setText(nombre);
        lblDniPersona.setText("DNI: " + dni);
        lblEmpresaPersona.setText("Empresa / Depto: " + empresa);

        Persona anfitrion = cachePersonas.get(v.getPersonaVisitadaId());
        lblAnfitrion.setText(anfitrion != null ? (anfitrion.getNombreCompleto() + " (" + (anfitrion.getPuesto() == null ? "Funcionario" : anfitrion.getPuesto()) + ")") : "—");
        lblMotivo.setText(v.getMotivo() == null ? "—" : v.getMotivo());

        actualizarBadge(v.getEstado());

        String checkin = v.getFechaHoraCheckin() != null ? v.getFechaHoraCheckin().format(FORMATO_FECHA) : "—";
        String checkout = v.getFechaHoraCheckout() != null ? v.getFechaHoraCheckout().format(FORMATO_FECHA) : "—";
        lblHorarios.setText("Check-in: " + checkin + " | Check-out: " + checkout);

        avatarSlot.getChildren().clear();
        avatarSlot.getChildren().add(UiUtils.avatar(nombre, 22));

        habilitarBotonesPorEstado(v.getEstado());
    }

    private void habilitarBotonesPorEstado(EstadoVisita estado) {
        String rol = Sesion.obtener().nombreRol();
        boolean esGuardaOAdmin = "GUARDA".equalsIgnoreCase(rol) || "ADMINISTRADOR".equalsIgnoreCase(rol);
        boolean esFuncionarioOAdmin = "FUNCIONARIO".equalsIgnoreCase(rol) || "ADMINISTRADOR".equalsIgnoreCase(rol);

        boolean esPendiente = estado == EstadoVisita.PENDIENTE_APROBACION
                || estado == EstadoVisita.PENDIENTE_APROBACION_POR_OLVIDO;

        btnCheckin.setDisable(!(estado == EstadoVisita.APROBADO && esGuardaOAdmin));
        btnCheckout.setDisable(!(estado == EstadoVisita.DENTRO && esGuardaOAdmin));
        btnAprobar.setDisable(!(esPendiente && esFuncionarioOAdmin));
        btnRechazar.setDisable(!(esPendiente && esFuncionarioOAdmin));
    }

    private void actualizarBadge(EstadoVisita estado) {
        lblEstado.setText(formatearNombreEstado(estado.name()));
        lblEstado.getStyleClass().removeAll("badge-verde", "badge-amarillo", "badge-rojo", "badge-azul", "badge-naranja");
        asignarClaseBadge(lblEstado, estado.name());
    }

    private String formatearNombreEstado(String estado) {
        return switch (estado) {
            case "APROBADO" -> "✓ APROBADO";
            case "DENTRO" -> "🏢 DENTRO";
            case "PENDIENTE_APROBACION" -> "⏳ PENDIENTE DE APROBACIÓN";
            case "PENDIENTE_APROBACION_POR_OLVIDO" -> "🎫 PASE TEMPORAL PENDIENTE";
            case "RECHAZADO" -> "✕ RECHAZADO";
            case "CHECK_OUT" -> "SALIDA REGISTRADA";
            case "CERRADA_POR_SISTEMA" -> "⚠️ CERRADA POR SISTEMA";
            default -> estado;
        };
    }

    private void asignarClaseBadge(Label badge, String estado) {
        switch (estado) {
            case "APROBADO" -> badge.getStyleClass().add("badge-verde");
            case "DENTRO" -> badge.getStyleClass().add("badge-azul");
            case "PENDIENTE_APROBACION", "PENDIENTE_APROBACION_POR_OLVIDO" -> badge.getStyleClass().add("badge-amarillo");
            case "RECHAZADO" -> badge.getStyleClass().add("badge-rojo");
            case "CERRADA_POR_SISTEMA" -> badge.getStyleClass().add("badge-naranja");
            default -> badge.getStyleClass().add("badge-gris");
        }
    }

    private void deshabilitarAcciones() {
        btnCheckin.setDisable(true);
        btnCheckout.setDisable(true);
        btnAprobar.setDisable(true);
        btnRechazar.setDisable(true);
    }

    private void mostrarDetalleVacio() {
        visitaActual = null;
        lblNombrePersona.setText("Seleccione una visita");
        lblDniPersona.setText("DNI: —");
        lblEmpresaPersona.setText("Empresa: —");
        lblAnfitrion.setText("—");
        lblMotivo.setText("—");
        lblEstado.setText("SIN SELECCIÓN");
        lblEstado.getStyleClass().removeAll("badge-verde", "badge-amarillo", "badge-rojo", "badge-azul", "badge-naranja");
        lblHorarios.setText("Check-in: — | Check-out: —");
        avatarSlot.getChildren().clear();
        deshabilitarAcciones();
    }

    private void ejecutarAccion(java.util.concurrent.Callable<Visita> accion, String mensajeExito) {
        if (visitaActual == null) return;
        UiUtils.enHiloFondo(accion, resultado -> {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Operación Exitosa", mensajeExito);
            recargar();
        }, ex -> mostrarAlerta(Alert.AlertType.ERROR, "Error", ex.getMessage()));
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.show();
    }

    @Override
    public void close() {
        SharedContainer.eventPublisher().desuscribir(VisitaAprobadaEvent.class, onAprobada);
        SharedContainer.eventPublisher().desuscribir(VisitaRechazadaEvent.class, onRechazada);
        SharedContainer.eventPublisher().desuscribir(VisitaCreadaEvent.class, onCreada);
        SharedContainer.eventPublisher().desuscribir(CheckInRealizadoEvent.class, onCheckinEvt);
        SharedContainer.eventPublisher().desuscribir(CheckOutRealizadoEvent.class, onCheckoutEvt);
        SharedContainer.eventPublisher().desuscribir(VisitaCerradaPorSistemaEvent.class, onCerradaEvt);
    }
}