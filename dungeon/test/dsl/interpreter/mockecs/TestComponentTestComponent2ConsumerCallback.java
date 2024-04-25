package dsl.interpreter.mockecs;

import dsl.annotation.DSLCallback;
import dsl.annotation.DSLContextMember;
import dsl.annotation.DSLType;
import java.util.function.Consumer;

/** WTF? . */
@DSLType(name = "test_component_with_callback")
public class TestComponentTestComponent2ConsumerCallback extends Component {
  private Entity entity;

  /**
   * WTF? .
   *
   * @return foo
   */
  public Entity getEntity() {
    return entity;
  }

  /** WTF? . */
  @DSLCallback public Consumer<TestComponent2> consumer;

  /**
   * WTF? .
   *
   * @param entity foo
   */
  public TestComponentTestComponent2ConsumerCallback(
      @DSLContextMember(name = "entity") Entity entity) {
    super(entity);
    this.entity = entity;
  }
}
