package dsl.interpreter.mockecs;

import dsl.annotation.DSLCallback;
import dsl.annotation.DSLContextMember;
import dsl.annotation.DSLType;
import java.util.Set;
import java.util.function.Function;

/** WTF? . */
@DSLType
public class TestComponentSetPassThroughCallback extends Component {
  private Entity entity;

  /**
   * WTF? .
   *
   * @return foo
   */
  public Entity getEntity() {
    return entity;
  }

  @DSLCallback private Function<Set<Entity>, Set<Entity>> onInteraction;

  /**
   * WTF? .
   *
   * @return foo
   */
  public Function<Set<Entity>, Set<Entity>> getOnInteraction() {
    return onInteraction;
  }

  /**
   * WTF? .
   *
   * @param entities foo
   * @return foo
   */
  public Set<Entity> executeCallback(Set<Entity> entities) {
    return onInteraction.apply(entities);
  }

  /**
   * WTF? .
   *
   * @param entity foo
   */
  public TestComponentSetPassThroughCallback(@DSLContextMember(name = "entity") Entity entity) {
    super(entity);
    this.entity = entity;
  }
}
