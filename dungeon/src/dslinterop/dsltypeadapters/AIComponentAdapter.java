package dslinterop.dsltypeadapters;

import contrib.components.AIComponent;
import contrib.entities.AIFactory;
import core.Entity;
import core.components.DrawComponent;
import dsl.annotation.DSLContextMember;
import dsl.annotation.DSLTypeAdapter;

/** Adapter for the {@link AIComponent} to the DSL. */
public class AIComponentAdapter {
  /**
   * Buildermethod for creating a new {@link DrawComponent} from a path, pointing to animations.
   *
   * @param entity foo
   * @return the created {@link DrawComponent}
   */
  @DSLTypeAdapter(name = "ai_component")
  public static AIComponent buildAIComponent(@DSLContextMember(name = "entity") Entity entity) {
    var comp =
        new AIComponent(
            AIFactory.randomFightAI(),
            AIFactory.randomIdleAI(),
            AIFactory.randomTransition(entity));
    return comp;
  }
}
