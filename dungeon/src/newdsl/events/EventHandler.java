package newdsl.events;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class EventHandler {
    private static final Map<Class<?>, Map<String, Set<Consumer<?>>>> listeners = new HashMap<>();
    private static final String ALL_TOPICS = "*";

    public static <T> void subscribe(Class<T> eventType, String topic, Consumer<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new HashMap<>())
            .computeIfAbsent(topic, k -> new HashSet<>())
            .add(listener);
    }

    public static <T> void subscribeToAllTopics(Class<T> eventType, Consumer<T> listener) {
        subscribe(eventType, ALL_TOPICS, listener);
    }

    public static <T> void unsubscribe(Class<T> eventType, String topic, Consumer<T> listener) {
        Map<String, Set<Consumer<?>>> topicListeners = listeners.get(eventType);
        if (topicListeners != null) {
            Set<Consumer<?>> eventListeners = topicListeners.get(topic);
            if (eventListeners != null) {
                eventListeners.remove(listener);
                if (eventListeners.isEmpty()) {
                    topicListeners.remove(topic);
                    if (topicListeners.isEmpty()) {
                        listeners.remove(eventType);
                    }
                }
            }
        }
    }

    public static <T> void unsubscribeFromAllTopics(Class<T> eventType, Consumer<T> listener) {
        unsubscribe(eventType, ALL_TOPICS, listener);
    }

    public static <T> void publish(T event, String topic) {
        Map<String, Set<Consumer<?>>> topicListeners = listeners.get(event.getClass());
        if (topicListeners != null) {
            Set<Consumer<?>> eventListeners = topicListeners.get(topic);
            if (eventListeners != null) {
                for (Consumer<?> listener : eventListeners) {
                    Consumer<T> eventListener = (Consumer<T>) listener;
                    eventListener.accept(event);
                }
            }
            Set<Consumer<?>> allTopicListeners = topicListeners.get(ALL_TOPICS);
            if (allTopicListeners != null) {
                for (Consumer<?> listener : allTopicListeners) {
                    Consumer<T> eventListener = (Consumer<T>) listener;
                    eventListener.accept(event);
                }
            }
        }
    }
}
