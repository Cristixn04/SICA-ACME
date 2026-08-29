package com.gestion_de_seguridad.ui;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.util.Duration;

/**
 * Utilidades visuales del tema ACME.
 *
 * Centraliza la construccion del logo (cohete + rayo en insignia circular),
 * el fondo tipo "plano industrial" (blueprint) para las pantallas de
 * autenticacion, campos de texto con icono integrado, avatares circulares
 * con iniciales y micro-interacciones (fade, pulso, anillos de notificacion),
 * manteniendo el estilo visual consistente entre pantallas sin depender de
 * imagenes externas (todo se dibuja con formas vectoriales de JavaFX).
 */
public final class UiUtils {

    private UiUtils() {
    }

    private static boolean fuenteCargada = false;

    /**
     * Carga la fuente Noto Emoji (incluida en resources/fonts/) para garantizar
     * que JavaFX renderice todos los emojis e íconos en cualquier sistema operativo.
     */
    public static synchronized void cargarFuentes() {
        if (!fuenteCargada) {
            try (var is = UiUtils.class.getResourceAsStream("/fonts/NotoEmoji-Variable.ttf")) {
                if (is != null) {
                    javafx.scene.text.Font.loadFont(is, 14);
                }
            } catch (Exception ignored) {
            }
            fuenteCargada = true;
        }
    }

    /**
     * Carga el CSS del tema sobre una escena asegurando la disponibilidad de fuentes.
     */
    public static void aplicarTema(Scene scene) {
        cargarFuentes();
        scene.getStylesheets().add(UiUtils.class.getResource("/css/acme.css").toExternalForm());
    }

    /**
     * Insignia circular del cohete ACME (usada en el logo grande y en el
     * encabezado compacto del sidebar).
     */
    private static StackPane insigniaCohete(double diametro) {
        Circle fondo = new Circle(diametro / 2.0);
        fondo.setFill(Color.web("#1b212b"));
        fondo.setStroke(Color.web("#f28c28"));
        fondo.setStrokeWidth(3);
        fondo.setEffect(new javafx.scene.effect.DropShadow(10, Color.web("#f28c28", 0.55)));

        double escala = diametro / 40.0;
        Polygon cohete = new Polygon(0, 22, 18, 22, 9, 0);
        cohete.setFill(Color.web("#e8b93a"));
        cohete.setStroke(Color.web("#f28c28"));
        cohete.setStrokeWidth(2);
        cohete.setScaleX(escala);
        cohete.setScaleY(escala);

        Polygon aletaIzq = new Polygon(0, 16, -8, 24, 3, 24);
        aletaIzq.setFill(Color.web("#e23c2e"));
        aletaIzq.setScaleX(escala);
        aletaIzq.setScaleY(escala);

        Polygon aletaDer = new Polygon(18, 16, 26, 24, 15, 24);
        aletaDer.setFill(Color.web("#e23c2e"));
        aletaDer.setScaleX(escala);
        aletaDer.setScaleY(escala);

        StackPane nave = new StackPane(aletaIzq, aletaDer, cohete);
        nave.setRotate(-20);

        StackPane insignia = new StackPane(fondo, nave);
        insignia.setPrefSize(diametro, diametro);
        insignia.setMaxSize(diametro, diametro);
        return insignia;
    }

    /**
     * Logo grande SICA ACME (insignia + texto "SICA" + "ACME"), usado en las
     * tarjetas de Login y Recuperar Contraseña.
     */
    public static StackPane crearLogoSica() {
        Label sica = new Label("SICA");
        sica.setTextFill(Color.web("#ffffff"));
        sica.setStyle("-fx-font-size: 24px; -fx-font-weight: 900;");

        Label acme = new Label("ACME");
        acme.setTextFill(Color.web("#f28c28"));
        acme.setStyle("-fx-font-size: 13px; -fx-font-weight: 900; -fx-letter-spacing: 3px;");

        VBox textos = new VBox(0, sica, acme);
        textos.setAlignment(Pos.CENTER);

        VBox contenedor = new VBox(6, insigniaCohete(58), textos);
        contenedor.setAlignment(Pos.CENTER);
        return new StackPane(contenedor);
    }

    /**
     * Encabezado compacto del sidebar: insignia pequeña + "ACME" al lado y
     * "SICA" debajo, para el tope del panel de navegacion.
     */
    public static VBox crearLogoCompacto() {
        Label acme = new Label("ACME");
        acme.setStyle("-fx-font-size: 15px; -fx-font-weight: 900; -fx-text-fill: #f28c28;");

        HBox fila = new HBox(8, insigniaCohete(30), acme);
        fila.setAlignment(Pos.CENTER_LEFT);

        Label sica = new Label("SICA");
        sica.setStyle("-fx-font-size: 12px; -fx-font-weight: 900; -fx-text-fill: #9aa5b1; -fx-letter-spacing: 2px;");

        VBox contenedor = new VBox(2, fila, sica);
        contenedor.getStyleClass().add("sidebar-header");
        return contenedor;
    }

    /**
     * Fondo tipo "plano industrial" (blueprint): rejilla de lineas claras
     * sobre azul muy oscuro, para las pantallas de Login y Recuperar
     * Contraseña. Se dibuja con formas vectoriales, sin depender de una
     * imagen externa.
     */
    public static Pane fondoBlueprint(double ancho, double alto) {
        Pane fondo = new Pane();
        fondo.setPrefSize(ancho, alto);
        fondo.setStyle("-fx-background-color: linear-gradient(to bottom right, #101826, #0a0e15);");

        double paso = 42;
        for (double x = 0; x <= ancho; x += paso) {
            Line l = new Line(x, 0, x, alto);
            l.setStroke(Color.web("#3a6fa8", 0.18));
            l.setStrokeWidth(1);
            fondo.getChildren().add(l);
        }
        for (double y = 0; y <= alto; y += paso) {
            Line l = new Line(0, y, ancho, y);
            l.setStroke(Color.web("#3a6fa8", 0.18));
            l.setStrokeWidth(1);
            fondo.getChildren().add(l);
        }
        // Un par de "engranajes" decorativos, como en la referencia.
        fondo.getChildren().add(engranaje(70, 90, 34, 0.12));
        fondo.getChildren().add(engranaje(ancho - 90, alto - 110, 46, 0.10));
        return fondo;
    }

    private static Circle engranaje(double x, double y, double radio, double opacidad) {
        Circle c = new Circle(x, y, radio);
        c.setFill(Color.TRANSPARENT);
        c.setStroke(Color.web("#f28c28", opacidad));
        c.setStrokeWidth(4);
        return c;
    }

    /**
     * Envuelve un campo de texto (TextField/PasswordField) con un icono a la
     * izquierda dentro de una caja clara con borde naranja, replicando el
     * estilo de los campos de Login/Recuperar de la referencia visual. El
     * campo pasado debe tener la clase "campo-plano" en su hoja de estilos.
     */
    public static HBox campoConIcono(String icono, Control campo) {
        Label lbl = new Label(icono);
        lbl.getStyleClass().add("campo-icono");

        HBox caja = new HBox(8, lbl, campo);
        caja.getStyleClass().add("campo-caja");
        caja.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(campo, javafx.scene.layout.Priority.ALWAYS);

        if (campo instanceof javafx.scene.control.TextInputControl input) {
            input.focusedProperty().addListener((obs, antes, ahora) -> {
                if (ahora) {
                    caja.getStyleClass().add("campo-caja-focus");
                } else {
                    caja.getStyleClass().remove("campo-caja-focus");
                }
            });
        }
        return caja;
    }

    /**
     * Icono de notificacion (campana) dentro de un circulo, con anillos de
     * pulso expandiendose para sugerir una alerta en tiempo real.
     */
    public static StackPane campanaConPulso() {
        Label campana = new Label("\uD83D\uDD14");
        campana.setStyle("-fx-font-size: 16px;");

        StackPane circulo = new StackPane(campana);
        circulo.getStyleClass().add("campana-circulo");

        Circle anillo = new Circle(16);
        anillo.getStyleClass().add("anillo-pulso");
        anillo.setOpacity(0);

        StackPane contenedor = new StackPane(anillo, circulo);

        Timeline pulso = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(anillo.radiusProperty(), 14),
                        new KeyValue(anillo.opacityProperty(), 0.6)),
                new KeyFrame(Duration.seconds(1.4),
                        new KeyValue(anillo.radiusProperty(), 26),
                        new KeyValue(anillo.opacityProperty(), 0))
        );
        pulso.setCycleCount(Timeline.INDEFINITE);
        pulso.play();

        return contenedor;
    }

    /**
     * Avatar circular con las iniciales de una persona.
     */
    public static StackPane avatar(String nombre, double radio) {
        Circle fondo = new Circle(radio);
        fondo.getStyleClass().add("avatar");
        String inicial = (nombre == null || nombre.isBlank())
                ? "?"
                : nombre.trim().substring(0, 1).toUpperCase();
        Label letra = new Label(inicial);
        letra.setTextFill(Color.WHITE);
        letra.setStyle("-fx-font-size: " + (radio * 0.9) + "px; -fx-font-weight: 900;");
        return new StackPane(fondo, letra);
    }

    /**
     * Aplica una animacion de entrada (fade) a un nodo.
     */
    public static void animarEntrada(Node nodo) {
        if (nodo == null) {
            return;
        }
        FadeTransition fade = new FadeTransition(Duration.millis(500), nodo);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
    }

    /**
     * Ejecuta una tarea potencialmente lenta (acceso a BD, hash de BCrypt,
     * etc.) en un hilo de fondo y devuelve el resultado al hilo JavaFX via
     * {@code Platform.runLater}. Evita que la UI se congele mientras se
     * consulta: es el mecanismo central de la Fase "no bloquear el hilo FX".
     *
     * @param tarea   trabajo a ejecutar fuera del hilo FX
     * @param onExito consumidor del resultado (se ejecuta en el hilo FX)
     * @param onError consumidor del error eventual (se ejecuta en el hilo FX)
     */
    public static <T> void enHiloFondo(
            java.util.concurrent.Callable<T> tarea,
            java.util.function.Consumer<T> onExito,
            java.util.function.Consumer<Throwable> onError) {
        Thread hilo = new Thread(() -> {
            try {
                T resultado = tarea.call();
                javafx.application.Platform.runLater(() -> onExito.accept(resultado));
            } catch (Throwable ex) {
                javafx.application.Platform.runLater(() -> onError.accept(ex));
            }
        }, "sica-bg");
        hilo.setDaemon(true);
        hilo.start();
    }

    /**
     * Aplica un pulso de escala (para badges de notificacion/urgencia).
     */
    public static void pulso(Node nodo) {
        ScaleTransition st = new ScaleTransition(Duration.millis(600), nodo);
        st.setFromX(1.0);
        st.setFromY(1.0);
        st.setToX(1.12);
        st.setToY(1.12);
        st.setAutoReverse(true);
        st.setCycleCount(ScaleTransition.INDEFINITE);
        st.play();
    }

    /**
     * Lanza una linea de "motion" (raya) temporal sobre un nodo (decorativo).
     */
    public static void motionLine(StackPane contenedor) {
        Line linea = new Line(0, 0, 30, 0);
        linea.setStroke(Color.web("#ffb347"));
        linea.setStrokeWidth(2.5);
        contenedor.getChildren().add(linea);
        FadeTransition ft = new FadeTransition(Duration.millis(900), linea);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setOnFinished(e -> contenedor.getChildren().remove(linea));
        ft.play();
    }
}
