package contrib.modules.interaction;

import contrib.hud.DialogUtils;
import contrib.utils.EntityUtils;
import core.Entity;
import java.util.function.BiConsumer;

/**
 * Represents an interaction that can be triggered via an {@link InteractionComponent}.
 *
 * <p>An {@code Interaction} consists of a callback that is executed when two entities interact, as
 * well as metadata such as interaction range and whether the interaction can be triggered multiple
 * times.
 */
public class Interaction {
  /** The default interaction radius. */
  public static final int DEFAULT_INTERACTION_RADIUS = 2;

  /** If it is repeatable by default. */
  public static final boolean DEFAULT_REPEATABLE = true;

  private final BiConsumer<Entity, Entity> onInteract;
  private final float range;
  private final boolean repeatable;
  private boolean active = true;

  /**
   * Creates a new {@code Interaction} with a custom callback, range, and repeatability.
   *
   * @param onInteract the action to execute when the interaction is triggered; receives the target
   *     entity and the interacting entity. The first entity is the one being interacted with, and
   *     the second entity is the one performing the interaction.
   * @param range the maximum distance (in game units) at which the interaction can occur
   * @param repeatable whether this interaction can be triggered more than once
   */
  public Interaction(BiConsumer<Entity, Entity> onInteract, float range, boolean repeatable) {
    this.onInteract = onInteract;
    this.range = range;
    this.repeatable = repeatable;
  }

  /**
   * Creates a new {@code Interaction} with a custom callback and default range and repeatability.
   *
   * @param onInteract the action to execute when the interaction is triggered. The first entity is
   *     the one being interacted with, and the second entity is the one performing the interaction.
   */
  public Interaction(BiConsumer<Entity, Entity> onInteract) {
    this(onInteract, DEFAULT_INTERACTION_RADIUS, DEFAULT_REPEATABLE);
  }

  /**
   * Creates a new {@code Interaction} with a custom callback and range, using default
   * repeatability.
   *
   * @param onInteract the action to execute when the interaction is triggered. The first entity is
   *     the one being interacted with, and the second entity is the one performing the interaction.
   * @param range the maximum interaction distance
   */
  public Interaction(BiConsumer<Entity, Entity> onInteract, float range) {
    this(onInteract, range, DEFAULT_REPEATABLE);
  }

  /**
   * Creates a new {@code Interaction} with a custom callback and repeatability, using the default
   * interaction range.
   *
   * @param onInteract the action to execute when the interaction is triggered. The first entity is
   *     the one being interacted with, and the second entity is the one performing the interaction.
   * @param repeatable whether this interaction can be triggered more than once
   */
  public Interaction(BiConsumer<Entity, Entity> onInteract, boolean repeatable) {
    this(onInteract, DEFAULT_INTERACTION_RADIUS, repeatable);
  }

  /**
   * Triggers the interaction if the interacting entity is within range and the interaction is still
   * active.
   *
   * <p>If the interaction is not repeatable, it becomes inactive after the first trigger. If an
   * inactive interaction is triggered an informative popup message is shown instead. If the
   * interaction is already completed or the entities are too far apart, an informative popup
   * message is shown instead.
   *
   * @param entity the entity being interacted with
   * @param who the entity performing the interaction
   */
  public void interact(Entity entity, Entity who) {
    if (range >= EntityUtils.getDistance(entity, who)) {
      if (active) {
        onInteract.accept(entity, who);
        if (!repeatable) active = false;
      } else {
        DialogUtils.showTextPopup("Das habe ich schon erledigt", "Erledigt");
      }
    } else {
      DialogUtils.showTextPopup("Dafür bin ich zu weit weg.", "Zu weit weg.");
    }
  }

  /**
   * Returns the maximum distance at which this interaction can be triggered.
   *
   * @return the interaction range
   */
  public float range() {
    return this.range;
  }
}
