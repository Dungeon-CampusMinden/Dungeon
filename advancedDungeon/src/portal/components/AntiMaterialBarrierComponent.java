package portal.components;

import portal.systems.AntiMaterialBarrierSystem;

/**
 * A component that represents the state of an anti-material barrier (active or inactive).
 *
 * <p>This component stores whether the barrier is currently active and provides methods to
 * activate, deactivate, or toggle its state. It is intended to be used in combination with systems
 * such as {@link AntiMaterialBarrierSystem}, which synchronize the component's state with
 * visual and gameplay effects (e.g., blocking objects/projectiles, interactions with the player).
 *
 * @see AntiMaterialBarrierSystem
 */
public class AntiMaterialBarrierComponent extends ToggleableComponent {

  /**
   * Creates a new {@code AntiMaterialBarrierComponent} with the given initial state.
   *
   * @param active {@code true} if the barrier should start active, {@code false} otherwise
   */
  public AntiMaterialBarrierComponent(boolean active) {
    super(active);
  }
}
