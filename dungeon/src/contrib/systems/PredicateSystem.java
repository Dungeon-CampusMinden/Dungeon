package contrib.systems;

import contrib.components.PredicateComponent;
import core.Entity;
import core.System;
import core.utils.components.MissingComponentException;

public class PredicateSystem extends System {
  public PredicateSystem() {
    super(PredicateComponent.class);
  }

  @Override
  public void execute() {
    filteredEntityStream(PredicateComponent.class).forEach(this::execute);
  }

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
