package dsl.interpreter.mockecs;

import dsl.annotation.DSLCallback;
import dsl.annotation.DSLContextMember;
import dsl.annotation.DSLType;
import java.util.List;
import java.util.function.Function;

/** WTF? . */
@DSLType
public class TestComponentListPassThroughCallback extends Component {
  private final Entity entity;
  @DSLCallback private Function<List<Entity>, List<Entity>> onInteraction;

  /**
   * WTF? .
   *
   * @param entity foo
   */
  public TestComponentListPassThroughCallback(@DSLContextMember(name = "entity") Entity entity) {
    super(entity);
    this.entity = entity;
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public Entity getEntity() {
    return entity;
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public Function<List<Entity>, List<Entity>> getOnInteraction() {
    return onInteraction;
  }

  /**
   * WTF? .
   *
   * @param entities foo
   * @return foo
   */
  public List<Entity> executeCallback(List<Entity> entities) {
    return onInteraction.apply(entities);
  }
}
