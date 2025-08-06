package contrib.components;

import core.Component;
import core.Entity;
import java.util.function.Consumer;

/**
 * Marks an entity as catapultable, meaning it can be launched upon collision with a catapult.
 *
 * <p>This component provides two hooks:
 *
 * <ul>
 *   <li>{@code deactivate} called when the entity is being catapulted (e.g., to disable controls or
 *       AI).
 *   <li>{@code reactivate} called when the catapulting process ends (e.g., to restore behavior).
 * </ul>
 *
 * @param deactivate a function that will be called to deactivate the entity
 * @param reactivate a function that will be called to reactivate the entity
 */
public record CatapultableComponent(Consumer<Entity> deactivate, Consumer<Entity> reactivate)
    implements Component {}
