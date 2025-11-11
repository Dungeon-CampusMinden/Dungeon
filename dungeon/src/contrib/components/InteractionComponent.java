package contrib.components;

import contrib.hud.dialogs.YesNoDialog;
import contrib.utils.components.interaction.Interaction;
import core.Component;
import core.Entity;
import core.systems.InputSystem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

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

  private final float radius;
  private final boolean repeatable;
  private final List<Interaction> interactions;

  /**
   * Create a new {@link InteractionComponent}.
   *
   * @param radius The radius in which an interaction can happen.
   * @param repeatable True if the interaction is repeatable, otherwise false.
   * @param onInteraction The behavior that should happen on an interaction.
   */
  public InteractionComponent(float radius, boolean repeatable, Interaction... onInteraction) {
    this.radius = radius;
    this.repeatable = repeatable;
    if (onInteraction != null) this.interactions = Arrays.stream(onInteraction).toList();
    else this.interactions = new ArrayList<>();
  }

  /**
   * Create a new {@link InteractionComponent} with default configuration.
   *
   * <p>The interaction radius is {@link #DEFAULT_INTERACTION_RADIUS}.
   *
   * <p>The interaction callback is empty.
   */
  public InteractionComponent() {
    this(DEFAULT_INTERACTION_RADIUS, DEFAULT_REPEATABLE);
  }

  public InteractionComponent(Interaction... interactions) {
    this(DEFAULT_INTERACTION_RADIUS, DEFAULT_REPEATABLE, interactions);
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
    // THIS SHOULD BE REPLACED WITH AN INTERACTIVE HUD

    if (this.interactions.isEmpty()) return;
    if (this.interactions.size() == 1) {
      interactions.get(0).accept(entity, who);
    } else {
      // Use an iterator so we can stop the loop
      Iterator<Interaction> iterator = interactions.iterator();
      while (iterator.hasNext()){
        Interaction interaction = iterator.next();

        YesNoDialog.showYesNoDialog(
            interaction.name(),
            "Interaktion",
            () -> {
              interaction.accept(entity, who);
              if (!repeatable) {
                entity.remove(InteractionComponent.class);
              }
            },
            () -> {});
      }
    }
  }

  /**
   * Gets the interaction radius.
   *
   * @return The radius in which an interaction can happen.
   */
  public float radius() {
    return radius;
  }
}
