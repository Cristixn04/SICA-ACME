package com.gestion_de_seguridad.ui;

import com.gestion_de_seguridad.usuarios.domain.model.Usuario;
import com.gestion_de_seguridad.usuarios.domain.port.in.ConsultarUsuarioUseCase;
import com.gestion_de_seguridad.usuarios.domain.port.in.VerificarPermisoUseCase;
import com.gestion_de_seguridad.usuarios.infrastructure.config.UsuariosContainer;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Pantalla Usuarios y Permisos (RBAC).
 *
 * Muestra la matriz de permisos granulares almacenada en la base de datos,
 * lista los usuarios del sistema y permite registrar nuevos operadores
 * verificando los permisos correspondientes.
 */
public final class VistaUsuarios {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static final Map<String, List<String>> MODULOS = new LinkedHashMap<>();
    static {
        MODULOS.put("ADMINISTRADOR", List.of(
                "crear_usuario", "modificar_permisos", "bloquear_usuario",
                "registrar_persona", "editar_persona", "eliminar_persona", "bloquear_persona",
                "crear_visita", "aprobar_visita", "rechazar_visita",
                "registrar_checkin", "registrar_checkout",
                "reportar_incidente", "gestionar_incidente",
                "generar_reporte_mensual", "exportar_datos"));
        MODULOS.put("GUARDA", List.of(
                "registrar_persona", "bloquear_persona", "crear_visita",
                "registrar_checkin", "registrar_checkout", "reportar_incidente"));
        MODULOS.put("FUNCIONARIO", List.of(
                "crear_visita", "aprobar_visita", "rechazar_visita", "reportar_incidente"));
    }

    private final VerificarPermisoUseCase verificar = UsuariosContainer.verificarPermiso();
    private final ConsultarUsuarioUseCase consultarUsuarios = UsuariosContainer.consultar();
    private final Long idUsuario = Sesion.obtener().idUsuario();

    @FXML
    private TableView<Usuario> tablaUsuarios;
    @FXML
    private TableColumn<Usuario, String> colUsrId;
    @FXML
    private TableColumn<Usuario, String> colUsrNombre;
    @FXML
    private TableColumn<Usuario, String> colUsrRol;
    @FXML
    private TableColumn<Usuario, String> colUsrActivo;
    @FXML
    private TableColumn<Usuario, String> colUsrPersona;

    @FXML
    private GridPane gridMatriz;
    @FXML
    private VBox mis;

    public Node crear() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/usuarios.fxml"));
            loader.setController(this);
            return loader.load();
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo cargar usuarios.fxml", ex);
        }
    }

    @FXML
    private void initialize() {
        configurarTabla();
        rellenarMatriz();
        recargar();
    }

    private void configurarTabla() {
        tablaUsuarios.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        colUsrId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        colUsrNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombreUsuario()));
        colUsrRol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRol().getNombre()));
        colUsrPersona.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getPersonaId() != null ? ("Persona #" + c.getValue().getPersonaId()) : "Sistema / Directo"));

        colUsrActivo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isActivo() ? "ACTIVO" : "INACTIVO"));
        colUsrActivo.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(item);
                badge.getStyleClass().add("badge");
                if ("ACTIVO".equals(item)) {
                    badge.getStyleClass().add("badge-verde");
                } else {
                    badge.getStyleClass().add("badge-rojo");
                }
                setGraphic(badge);
            }
        });
    }

    @FXML
    private void onRefrescar() {
        recargar();
    }

    @FXML
    private void onCrearUsuario() {
        TextField txtUsuario = new TextField();
        txtUsuario.setPromptText("Nombre de usuario (ej. guarda2)");
        PasswordField txtPass = new PasswordField();
        txtPass.setPromptText("Contraseña (mínimo 4 caracteres)");
        ComboBox<String> comboRol = new ComboBox<>();
        comboRol.getItems().setAll("ADMINISTRADOR", "GUARDA", "FUNCIONARIO");
        comboRol.setValue("GUARDA");

        VBox content = new VBox(8,
                new Label("Nombre de Usuario:"), txtUsuario,
                new Label("Contraseña:"), txtPass,
                new Label("Rol Asignado:"), comboRol);
        content.setPadding(new Insets(10));

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Crear Nuevo Usuario");
        alert.setHeaderText("Alta de operador en el sistema SICA");
        alert.getDialogPane().setContent(content);
        alert.showAndWait().ifPresent(btn -> {
            if (btn.getButtonData().isDefaultButton()) {
                UiUtils.enHiloFondo(() -> {
                    return consultarUsuarios.registrarUsuario(
                            Sesion.obtener().idUsuario(),
                            txtUsuario.getText(),
                            txtPass.getText(),
                            comboRol.getValue());
                }, u -> {
                    new Alert(Alert.AlertType.INFORMATION, "Usuario '" + u.getNombreUsuario() + "' creado con éxito.").show();
                    recargar();
                }, ex -> {
                    new Alert(Alert.AlertType.ERROR, "Error al crear usuario: " + ex.getMessage()).show();
                });
            }
        });
    }

    private void recargar() {
        UiUtils.enHiloFondo(this::cargarDatos, this::pintarDatos, ex -> {
            // ante error se deja vacio
        });
    }

    private record DatosUsuarios(List<Usuario> usuarios, List<String> permisos) {}

    private DatosUsuarios cargarDatos() {
        List<Usuario> lista = consultarUsuarios.listarTodos();
        List<String> misP = misPermisos();
        return new DatosUsuarios(lista, misP);
    }

    private void pintarDatos(DatosUsuarios d) {
        tablaUsuarios.setItems(FXCollections.observableArrayList(d.usuarios()));
        pintarMisPermisos(d.permisos());
    }

    private void rellenarMatriz() {
        gridMatriz.getChildren().clear();
        int fila = 0;
        gridMatriz.add(estiloCabecera("Rol"), 0, fila);
        gridMatriz.add(estiloCabecera("Módulo / Permisos"), 1, fila);
        gridMatriz.add(estiloCabecera("Nº permisos"), 2, fila);
        fila++;

        for (Map.Entry<String, List<String>> e : MODULOS.entrySet()) {
            String rol = e.getKey();
            List<String> codigos = e.getValue();
            VBox lista = new VBox(2);
            lista.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            for (String codigo : codigos) {
                Label permiso = new Label(codigo);
                permiso.getStyleClass().add("info-valor");
                lista.getChildren().add(permiso);
            }
            Label cantidad = new Label(String.valueOf(codigos.size()));
            cantidad.getStyleClass().add("badge");
            cantidad.getStyleClass().add("badge-azul");
            HBox filaCantidad = new HBox(cantidad);
            filaCantidad.setAlignment(javafx.geometry.Pos.CENTER);
            gridMatriz.add(new Label(rol), 0, fila);
            gridMatriz.add(lista, 1, fila);
            gridMatriz.add(filaCantidad, 2, fila);
            fila++;
        }
    }

    private Label estiloCabecera(String texto) {
        Label l = new Label(texto);
        l.getStyleClass().add("metrica-titulo");
        return l;
    }

    private List<String> misPermisos() {
        List<String> lineas = new ArrayList<>();
        for (String codigo : List.of("crear_usuario", "modificar_permisos", "bloquear_usuario", "generar_reporte_mensual", "exportar_datos")) {
            lineas.add((verificar.tienePermiso(idUsuario, codigo) ? "✓ " : "✕ ") + codigo);
        }
        return lineas;
    }

    private void pintarMisPermisos(List<String> lineas) {
        mis.getChildren().clear();
        for (String linea : lineas) {
            Label l = new Label(linea);
            l.getStyleClass().add("info-valor");
            mis.getChildren().add(l);
        }
    }
}