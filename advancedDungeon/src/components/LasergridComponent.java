package components;

import core.Component;

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
public class LasergridComponent implements Component {

  private boolean active;

  /**
   * Creates a new {@code LasergridComponent} with the given initial state.
   *
   * @param active {@code true} if the laser grid should start active, {@code false} otherwise
   */
  public LasergridComponent(boolean active) {
    this.active = active;
  }

  /**
   * Checks whether the laser grid is currently active.
   *
   * @return {@code true} if the laser grid is active, {@code false} otherwise
   */
  public boolean isActive() {
    return active;
  }

  /** Activates the laser grid by setting its state to active. */
  public void activate() {
    this.active = true;
  }

  /** Deactivates the laser grid by setting its state to inactive. */
  public void deactivate() {
    this.active = false;
  }

  /**
   * Toggles the state of the laser grid.
   *
   * <p>If the grid is active, it becomes inactive. If it is inactive, it becomes active.
   */
  public void toggle() {
    if (this.active) {
      this.deactivate();
    } else {
      this.activate();
    }
  }
}
