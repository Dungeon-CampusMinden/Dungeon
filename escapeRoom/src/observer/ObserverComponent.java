package observer;

import core.Entity;
import java.util.function.Consumer;

/**
 * Represents a component that observes {@link ObservableComponent}s in an ECS system.
 *
 * <p>The {@link #onNotify} callback is executed whenever the observed component triggers a
 * notification in the {@link ObserverSystem}.
 *
 * @param onNotify a consumer that receives the entity being observed when a notification occurs
 */
public record ObserverComponent(Consumer<Entity> onNotify) {}
