package dsl.interpreter.mockecs;

import dsl.annotation.DSLCallback;
import dsl.annotation.DSLContextMember;
import dsl.annotation.DSLType;
import java.util.function.Consumer;

/** WTF? . */
@DSLType
public class TestComponentWithCallback extends Component {
  private final Entity entity;
  @DSLCallback private Consumer<Entity> onInteraction;

  /**
   * WTF? .
   *
   * @param entity foo
   */
  public TestComponentWithCallback(@DSLContextMember(name = "entity") Entity entity) {
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
  public Consumer<Entity> getOnInteraction() {
    return onInteraction;
  }
}
