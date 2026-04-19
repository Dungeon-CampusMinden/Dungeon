package portal.util;

import core.Component;

/**
 * A generic component that represents a toggleable state (active or inactive).
 *
 * <p>This component provides common functionality for entities that can be turned on or off, such
 * as laser grids, barriers, switches, or other game mechanics. It is intended to be extended by
 * more specific components (e.g., {@code LasergridComponent}, {@code AntiMaterialComponent}) to
 * allow systems to distinguish between different toggleable entities while sharing the same
 * activation logic.
 *
 * <p>By default, the component only stores and manages its active state. Systems using this
 * component (or its subclasses) are responsible for applying gameplay and visual logic depending on
 * whether the state is active or inactive.
 */
public class ToggleableComponent implements Component {

  private boolean active;

  /**
   * Creates a new {@code ToggleableComponent} with the given initial state.
   *
   * @param active {@code true} if the entity should start in an active state, {@code false}
   *     otherwise
   */
  public ToggleableComponent(boolean active) {
    this.active = active;
  }

  /**
   * Returns whether the component is currently active.
   *
   * @return {@code true} if active, {@code false} if inactive
   */
  public boolean isActive() {
    return active;
  }

  /** Sets the component's state to active. */
  public void activate() {
    this.active = true;
  }

  /** Sets the component's state to inactive. */
  public void deactivate() {
    this.active = false;
  }

  /**
   * Toggles the component's state.
   *
   * <p>If the component is active, it becomes inactive. If it is inactive, it becomes active.
   */
  public void toggle() {
    this.active = !this.active;
  }
}
