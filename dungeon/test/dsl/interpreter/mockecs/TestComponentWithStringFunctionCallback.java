package dsl.interpreter.mockecs;

import dsl.annotation.DSLCallback;
import dsl.annotation.DSLContextMember;
import dsl.annotation.DSLType;
import java.util.function.Function;

/** WTF? . */
@DSLType
public class TestComponentWithStringFunctionCallback extends Component {
  private Entity entity;

  /**
   * WTF? .
   *
   * @return foo
   */
  public Entity getEntity() {
    return entity;
  }

  @DSLCallback private Function<String, String> onInteraction;

  /**
   * WTF? .
   *
   * @param entity foo
   */
  public TestComponentWithStringFunctionCallback(@DSLContextMember(name = "entity") Entity entity) {
    super(entity);
    this.entity = entity;
  }

  /**
   * WTF? .
   *
   * @param text foo
   * @return foo
   */
  public String executeCallbackWithText(String text) {
    return onInteraction.apply(text);
  }
}
