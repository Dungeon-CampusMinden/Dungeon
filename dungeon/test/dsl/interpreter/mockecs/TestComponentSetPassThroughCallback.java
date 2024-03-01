package dsl.interpreter.mockecs;

import dsl.annotation.DSLCallback;
import dsl.annotation.DSLContextMember;
import dsl.annotation.DSLType;
import java.util.Set;
import java.util.function.Function;

@DSLType
public class TestComponentSetPassThroughCallback extends Component {
  private Entity entity;

  public Entity getEntity() {
    return entity;
  }

  @DSLCallback private Function<Set<Entity>, Set<Entity>> onInteraction;

  public Function<Set<Entity>, Set<Entity>> getOnInteraction() {
    return onInteraction;
  }

  public Set<Entity> executeCallback(Set<Entity> entities) {
    return onInteraction.apply(entities);
  }

  public TestComponentSetPassThroughCallback(@DSLContextMember(name = "entity") Entity entity) {
    super(entity);
    this.entity = entity;
  }
}
