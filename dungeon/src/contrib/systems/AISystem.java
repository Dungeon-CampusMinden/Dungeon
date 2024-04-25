package contrib.systems;

import contrib.components.AIComponent;
import core.Entity;
import core.System;
import core.utils.components.MissingComponentException;

/**
 * Controls the AI.
 *
 * <p>Entities with the {@link AIComponent} will be processed by this system.
 */
public final class AISystem extends System {

  /** Create a new AISystem. */
  public AISystem() {
    super(AIComponent.class);
  }

  @Override
  public void execute() {
    entityStream().forEach(this::executeAI);
  }

  private void executeAI(Entity entity) {
    AIComponent ai =
        entity
            .fetch(AIComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, AIComponent.class));

    if (ai.shouldFight().apply(entity)) ai.fightBehavior().accept(entity);
    else ai.idleBehavior().accept(entity);
  }
}
