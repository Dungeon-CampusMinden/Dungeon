package components;

/**
 * A component that represents the state of a laser grid (active or inactive).
 *
 * <p>This component stores whether the laser grid is currently active and provides methods to
 * activate, deactivate, or toggle its state. It is intended to be used in combination with systems
 * such as {@link systems.LasergridSystem}, which synchronize the component's state with visual and
 * gameplay effects (e.g., damage and collisions).
 *
 * @see systems.LasergridSystem
 */
public class LasergridComponent extends ToggleableComponent {

  /**
   * Creates a new {@code LasergridComponent} with the given initial state.
   *
   * @param active {@code true} if the laser grid should start active, {@code false} otherwise
   */
  public LasergridComponent(boolean active) {
    super(active);
  }
}
