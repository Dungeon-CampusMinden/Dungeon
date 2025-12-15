package contrib.components;

import core.Component;

/**
 * Interface for components that can display a progress bar above an entity.
 *
 * <p>Components implementing this interface will have their bars automatically managed by the
 * AttributeBarSystem, with stacking based on priority (lower priority = closer to entity).
 */
public interface BarDisplayable extends Component {

  /**
   * Returns the current value of the attribute.
   *
   * @return the current value
   */
  float current();

  /**
   * Returns the maximum possible value of the attribute.
   *
   * @return the maximum value
   */
  float max();

  /**
   * Returns the style name for the progress bar.
   *
   * @return the bar style name (e.g., "healthbar", "manabar")
   */
  String barStyleName();

  /**
   * Returns the priority for bar stacking order.
   *
   * <p>Lower priority values render closer to the entity (bottom), higher values further away
   * (top).
   *
   * @return the priority (0 = closest to entity)
   */
  int barPriority();
}
