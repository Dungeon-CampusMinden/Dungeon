package contrib.modules.interaction;

import core.Component;
import core.Entity;
import core.systems.InputSystem;
import java.util.Optional;
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
 */
public final class InteractionComponent implements Component {

  private static final IInteractable DEFAULT_INTERACTION = new IInteractable() {};
  private final IInteractable onInteraction;

  /**
   * Create a new {@link InteractionComponent}.
   *
   * @param onInteraction The behavior that should happen on an interaction.
   */
  public InteractionComponent(IInteractable onInteraction) {
    this.onInteraction = onInteraction;
  }

  /**
   * Create a new {@link InteractionComponent} with default configuration.
   *
   * <p>The interaction callback is empty.
   */
  public InteractionComponent() {
    this(DEFAULT_INTERACTION);
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
    Optional<Interaction> interaction = RingMenue.showInteractionMenue(onInteraction);
    interaction.ifPresent(i -> i.interact(entity, who));
  }
}
