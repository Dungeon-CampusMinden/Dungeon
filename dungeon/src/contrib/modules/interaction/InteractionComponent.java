package contrib.modules.interaction;

import core.Component;
import core.Entity;
import core.systems.InputSystem;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * A component that enables an entity to perform interactions.
 *
 * <p>An interaction can be triggered using {@link #triggerInteraction(Entity, Entity)}, typically
 * through the {@link InputSystem} when the player presses the corresponding key and is within the
 * interaction range.
 *
 * <p>The behavior of an interaction is defined by the {@link IInteractable} implementation provided
 * to this component. Each interaction is represented by an {@link Interaction}, which internally
 * uses a {@link BiConsumer} callback for its logic.
 *
 * <p>Depending on the interaction configuration, an interaction may be repeatable or single-use. If
 * it is not repeatable, the corresponding {@link Interaction} will deactivate itself after
 * execution. For simple interaction types, {@link ISimpleIInteractable} can be used to define a
 * single default interaction.
 */
public final class InteractionComponent implements Component {

  private static final IInteractable DEFAULT_INTERACTION = new IInteractable() {};
  private final IInteractable interactions;

  /**
   * Creates a new {@link InteractionComponent} with a custom interaction provider.
   *
   * @param interactions the interaction behavior to execute when an interaction is triggered
   */
  public InteractionComponent(IInteractable interactions) {
    this.interactions = interactions;
  }

  /**
   * Creates a new {@link InteractionComponent} using the default interaction configuration.
   *
   * <p>The default {@link IInteractable} provides no custom behavior beyond the predefined
   * interaction types.
   */
  public InteractionComponent() {
    this(DEFAULT_INTERACTION);
  }

  /**
   * Triggers the interaction associated with this component.
   *
   * <p>If the assigned {@link IInteractable} is an {@link ISimpleIInteractable}, the interaction is
   * executed directly. Otherwise, a selection menu (implemented by {@code RingMenue}) is shown to
   * allow the player to choose a specific interaction. Once chosen, the corresponding {@link
   * Interaction} is executed.
   *
   * @param entity the entity that owns this component
   * @param who the entity performing the interaction
   */
  public void triggerInteraction(final Entity entity, final Entity who) {
    if (interactions instanceof ISimpleIInteractable) interactions.interact().interact(entity, who);
    else {
      Optional<Interaction> interaction = RingMenue.showInteractionMenue(interactions);
      interaction.ifPresent(i -> i.interact(entity, who));
    }
  }

  /**
   * Returns the {@link IInteractable} assigned to this component.
   *
   * @return the interaction provider for this component
   */
  public IInteractable interactions() {
    return this.interactions;
  }
}
