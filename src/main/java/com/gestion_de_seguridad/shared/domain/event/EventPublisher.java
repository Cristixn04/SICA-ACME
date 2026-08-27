package com.gestion_de_seguridad.shared.domain.event;

import com.gestion_de_seguridad.shared.domain.DomainEvent;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Publicador de eventos de dominio (patron Observer).
 *
 * Implementacion simple basada en un Map de tipo de evento -> lista de
 * listeners, usando lambdas para registrar los consumidores. Permite que los
 * slices se comuniquen entre si sin conocerce: un slice publica un evento y
 * cualquier listener suscrito reacciona.
 *
 * Diseñado para un unico proceso multi-ventana JavaFX, donde la actualizacion
 * en tiempo real de la UI (ej. pantalla del Guarda) se resuelve via
 * Platform.runLater() dentro de los listeners.
 */
public final class EventPublisher {

    private final Map<Class<? extends DomainEvent>, List<Consumer<? extends DomainEvent>>> listeners =
            new ConcurrentHashMap<>();

    /**
     * Registra un listener para un tipo de evento concreto.
     *
     * @param <E>        tipo del evento
     * @param tipoEvento clase del evento a escuchar
     * @param listener   consumidor lambda que reaccionara al evento
     */
    public <E extends DomainEvent> void suscribir(Class<E> tipoEvento, Consumer<E> listener) {
        listeners.computeIfAbsent(tipoEvento, k -> new CopyOnWriteArrayList<>())
                 .add(listener);
    }

    /**
     * Quita un listener registrado para un tipo de evento.
     *
     * @param <E>        tipo del evento
     * @param tipoEvento clase del evento
     * @param listener   consumidor a remover
     */
    public <E extends DomainEvent> void desuscribir(Class<E> tipoEvento, Consumer<E> listener) {
        List<Consumer<? extends DomainEvent>> suscriptores = listeners.get(tipoEvento);
        if (suscriptores != null) {
            // Se compara por igualdad de referencia del lambda.
            Iterator<Consumer<? extends DomainEvent>> it = suscriptores.iterator();
            while (it.hasNext()) {
                if (it.next() == listener) {
                    it.remove();
                }
            }
        }
    }

    /**
     * Publica un evento, notificando a todos los listeners suscritos a su tipo.
     *
     * Si algun listener lanza una excepcion, esta se propaga para que el
     * productor del evento pueda tomar decisiones (ej. revertir una operacion
     * que no pudo auditarse correctamente).
     *
     * @param <E>   tipo del evento
     * @param evento el evento de dominio a publicar
     */
    @SuppressWarnings("unchecked")
    public <E extends DomainEvent> void publicar(E evento) {
        List<Consumer<? extends DomainEvent>> suscriptores = listeners.get(evento.getClass());
        if (suscriptores == null) {
            return;
        }
        for (Consumer<? extends DomainEvent> listener : suscriptores) {
            ((Consumer<E>) listener).accept(evento);
        }
    }

    /**
     * Elimina todos los listeners registrados (util en tests o reinicios).
     */
    public void limpiar() {
        listeners.clear();
    }
}
