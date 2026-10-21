package feature.components;

import com.badlogic.gdx.graphics.Color;
import engine.Component;

/**
 * Interface for components that can display an attribute bar above an entity.
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
   * Returns the fill colour of the bar.
   *
   * @return the colour; callers must not modify it
   */
  Color barColor();

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
