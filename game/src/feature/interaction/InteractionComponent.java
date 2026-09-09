package feature.interaction;

import engine.Component;
import engine.Entity;
import engine.systems.input.InputSystem;
import feature.hud.DialogUtils;
import java.util.function.BiConsumer;

/**
 * A component that enables an entity to perform interactions.
 *
 * <p>An interaction can be triggered using {@link #triggerInteraction(Entity, Entity)}, typically
 * through the {@link InputSystem} when the player presses the corresponding key and is within the
 * maximum interaction distance.
 *
 * <p>The behavior of an interaction is defined by the {@link Interaction} provided to this
 * component. Each interaction internally uses a {@link BiConsumer} callback for its logic.
 *
 * <p>Depending on the interaction configuration, an interaction may be repeatable or single-use. If
 * it is not repeatable, the corresponding {@link Interaction} will deactivate itself after
 * execution.
 */
public final class InteractionComponent implements Component {

  private static final Interaction DEFAULT_INTERACTION =
      new Interaction(
          (entity, who) ->
              DialogUtils.showTextPopup(
                  "Ich drücke, ziehe und tippe... aber es passiert absolut gar nichts.",
                  "Interagieren",
                  who.id()));

  private final Interaction interaction;

  /**
   * Creates a new {@link InteractionComponent} with a custom interaction.
   *
   * @param interaction the interaction behavior to execute when an interaction is triggered
   */
  public InteractionComponent(Interaction interaction) {
    this.interaction = interaction;
  }

  /**
   * Creates a new {@link InteractionComponent} with a custom interaction callback.
   *
   * @param onInteract the action to execute when an interaction is triggered
   */
  public InteractionComponent(BiConsumer<Entity, Entity> onInteract) {
    this(new Interaction(onInteract));
  }

  /**
   * Creates a new {@link InteractionComponent} using the default interaction configuration.
   *
   * <p>The default interaction shows a generic feedback popup.
   */
  public InteractionComponent() {
    this(DEFAULT_INTERACTION);
  }

  /**
   * Triggers the interaction associated with this component.
   *
   * @param entity the entity that owns this component
   * @param who the entity performing the interaction
   */
  public void triggerInteraction(final Entity entity, final Entity who) {
    interaction.interact(entity, who);
  }

  /**
   * Returns the {@link Interaction} assigned to this component.
   *
   * @return the interaction for this component
   */
  public Interaction interaction() {
    return this.interaction;
  }
}
