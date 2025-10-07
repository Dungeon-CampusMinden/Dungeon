package contrib.components;

import contrib.utils.EntityUtils;
import core.Component;
import core.Entity;
import core.systems.InputSystem;
import core.utils.Point;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Allows interaction with the associated entity.
 *
 * <p>An interaction can be triggered using {@link #triggerInteraction(Entity, Entity)}. This
 * happens in the {@link InputSystem} if the player presses the corresponding button on the keyboard
 * and is in the interaction range of this component.
 *
 * <p>What happens during an interaction is defined by the {@link BiConsumer} {@link
 * #onInteraction}.
 *
 * <p>An interaction can be repeatable, in which case it can be triggered multiple times. If an
 * interaction is not repeatable, the {@link InteractionComponent} is removed from the associated
 * entity after the interaction.
 *
 * <p>The interaction radius can be queried with {@link #radius()}.
 */
public final class InteractionComponent implements Component {
  /** The default interaction radius. */
  public static final int DEFAULT_INTERACTION_RADIUS = 5;

  /** If it is repeatable by default. */
  public static final boolean DEFAULT_REPEATABLE = true;

  private static final BiConsumer<Entity, Entity> DEFAULT_INTERACTION = (entity, who) -> {};
  private final float radius;
  private final boolean repeatable;
  private final BiConsumer<Entity, Entity> onInteraction;
  private final Function<Entity, Point> centerFunction;

  /**
   * Creates a new {@link InteractionComponent} using the visual center of the entity as the
   * reference point for range calculations.
   *
   * <p>The center is provided by {@link EntityUtils#getEntityCenter(Entity)}.
   *
   * @param radius The radius in which an interaction can happen.
   * @param repeatable True if the interaction is repeatable, otherwise false.
   * @param onInteraction The behavior that should happen on an interaction.
   */
  public InteractionComponent(
      float radius, boolean repeatable, final BiConsumer<Entity, Entity> onInteraction) {
    this.radius = radius;
    this.repeatable = repeatable;
    this.onInteraction = onInteraction;
    this.centerFunction = EntityUtils::getEntityCenter;
  }

  /**
   * Creates a new {@link InteractionComponent} with a custom function to determine the reference
   * point used for interaction range calculations.
   *
   * <p>This allows defining custom anchor points.
   *
   * @param radius The radius in which an interaction can happen.
   * @param repeatable True if the interaction is repeatable, otherwise false.
   * @param onInteraction The behavior that should happen on an interaction.
   * @param centerFunction A function that returns the custom reference point of the entity.
   */
  public InteractionComponent(
      float radius,
      boolean repeatable,
      final BiConsumer<Entity, Entity> onInteraction,
      final Function<Entity, Point> centerFunction) {
    this.radius = radius;
    this.repeatable = repeatable;
    this.onInteraction = onInteraction;
    this.centerFunction = centerFunction;
  }

  /**
   * Create a new {@link InteractionComponent} with default configuration.
   *
   * <p>The interaction radius is {@link #DEFAULT_INTERACTION_RADIUS}.
   *
   * <p>The interaction callback is empty.
   */
  public InteractionComponent() {
    this(DEFAULT_INTERACTION_RADIUS, DEFAULT_REPEATABLE, DEFAULT_INTERACTION);
  }

  /**
   * Triggers the interaction callback.
   *
   * <p>If the interaction is not repeatable, this component will be removed from the entity
   * afterwards.
   *
   * @param entity associated entity of this component.
   * @param who The entity that triggered the interaction.
   */
  public void triggerInteraction(final Entity entity, final Entity who) {
    onInteraction.accept(entity, who);
    if (!repeatable) entity.remove(InteractionComponent.class);
  }

  /**
   * Gets the interaction radius.
   *
   * @return The radius in which an interaction can happen.
   */
  public float radius() {
    return radius;
  }

  /**
   * Determines whether a given entity is within the interaction range of this component.
   *
   * <p>This method uses the Euclidean distance to calculate the distance between the center point
   * of the entity that owns this {@link InteractionComponent} and the center point of another
   * entity attempting to interact with it. These points are provided by the configured {@code
   * centerFunction}, which defines how the anchor position of each entity is determined.
   *
   * @param self The entity that owns this {@link InteractionComponent}.
   * @param who The entity attempting to interact.
   * @return {@code true} if the other entity is within the interaction radius; {@code false}
   *     otherwise.
   */
  public boolean isEntityInRange(Entity self, Entity who) {
    Point cSelf = centerFunction.apply(self);
    Point cWho = centerFunction.apply(who);
    float dx = cWho.x() - cSelf.x();
    float dy = cWho.y() - cSelf.y();
    float squaredDistance = dx * dx + dy * dy;
    float squaredRadius = radius * radius;
    return squaredDistance <= squaredRadius;
  }
}
