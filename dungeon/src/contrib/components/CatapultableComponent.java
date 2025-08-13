package contrib.components;

import core.Component;
import core.Entity;
import java.util.function.Consumer;

/**
 * Marks an entity as catapultable, meaning it can be launched upon collision with a catapult.
 *
 * <p>This component provides hooks to manage the entity’s behavior during the catapult process.
 *
 * <p>The component also tracks whether the entity is currently flying.
 */
public class CatapultableComponent implements Component {

  private final Consumer<Entity> deactivate;
  private final Consumer<Entity> reactivate;
  private boolean flying = false;

  /**
   * Creates a new CatapultableComponent.
   *
   * @param deactivate a function that will be called to deactivate the entity when catapulting
   *     starts
   * @param reactivate a function that will be called to reactivate the entity when catapulting ends
   */
  public CatapultableComponent(Consumer<Entity> deactivate, Consumer<Entity> reactivate) {
    this.deactivate = deactivate;
    this.reactivate = reactivate;
  }

  /**
   * Returns the function to deactivate the entity.
   *
   * @return a Consumer called when catapulting starts
   */
  public Consumer<Entity> deactivate() {
    return deactivate;
  }

  /**
   * Returns the function to reactivate the entity.
   *
   * @return a Consumer called when catapulting ends
   */
  public Consumer<Entity> reactivate() {
    return reactivate;
  }

  /** Marks the entity as flying. */
  public void flies() {
    flying = true;
  }

  /** Marks the entity as landed. */
  public void lands() {
    flying = false;
  }

  /**
   * Returns whether the entity is currently flying.
   *
   * @return true if the entity is in the air, false otherwise
   */
  public boolean isFlying() {
    return flying;
  }
}
