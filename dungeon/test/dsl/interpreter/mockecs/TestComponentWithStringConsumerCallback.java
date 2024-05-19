package dsl.interpreter.mockecs;

import dsl.annotation.DSLCallback;
import dsl.annotation.DSLContextMember;
import dsl.annotation.DSLType;
import java.util.function.Consumer;

/** WTF? . */
@DSLType
public class TestComponentWithStringConsumerCallback extends Component {
  private Entity entity;

  /**
   * WTF? .
   *
   * @return foo
   */
  public Entity getEntity() {
    return entity;
  }

  @DSLCallback private Consumer<String> onInteraction;

  /**
   * WTF? .
   *
   * @param entity foo
   */
  public TestComponentWithStringConsumerCallback(@DSLContextMember(name = "entity") Entity entity) {
    super(entity);
    this.entity = entity;
  }

  /**
   * WTF? .
   *
   * @param text foo
   */
  public void executeCallbackWithText(String text) {
    onInteraction.accept(text);
  }
}
