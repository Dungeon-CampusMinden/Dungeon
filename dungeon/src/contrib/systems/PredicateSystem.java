package contrib.systems;

import contrib.components.PredicateComponent;
import core.Entity;
import core.System;
import core.utils.components.MissingComponentException;

/**
 * The PredicateSystem class is responsible for managing the execution of predicate-based logic in
 * the game. It checks whether the state of each {@link PredicateComponent} has changed based on its
 * logical result and executes or undoes the associated actions accordingly.
 *
 * @see PredicateComponent
 */
public class PredicateSystem extends System {

  /**
   * Constructs a new PredicateSystem.
   *
   * <p>This system listens for changes in the state of {@link PredicateComponent}s and ensures that
   * actions are executed when the state of a predicate changes.
   */
  public PredicateSystem() {
    super(PredicateComponent.class);
  }

  /**
   * Executes the logic of all filtered entities that contain a {@link PredicateComponent}.
   *
   * <p>This method checks the result of the predicate logic and updates the state accordingly,
   * executing or undoing the associated action based on the result.
   */
  @Override
  public void execute() {
    filteredEntityStream(PredicateComponent.class).forEach(this::execute);
  }

  /**
   * Executes the logic for a single entity with a {@link PredicateComponent}.
   *
   * @param entity the entity to execute the logic for
   */
  private void execute(Entity entity) {
    PredicateComponent pc =
        entity
            .fetch(PredicateComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, PredicateComponent.class));
    boolean result = pc.logicResult();
    if (result != pc.state()) {
      pc.state(result);
      if (result) pc.execute();
      else pc.undo();
    }
  }
}
