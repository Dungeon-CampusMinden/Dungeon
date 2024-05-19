package dsl.interpreter.mockecs;

import dsl.annotation.DSLCallback;
import dsl.annotation.DSLContextMember;
import dsl.annotation.DSLType;
import java.util.function.Function;

/** WTF? . */
@DSLType
public class TestComponentWithFunctionCallback extends Component {
  private Entity entity;

  /**
   * WTF? .
   *
   * @return foo
   */
  public Entity getEntity() {
    return entity;
  }

  @DSLCallback private Function<Entity, Boolean> onInteraction;
  @DSLCallback private Function<Entity, MyEnum> getEnum;
  @DSLCallback private Function<MyEnum, Boolean> functionWithEnumParam;

  /**
   * WTF? .
   *
   * @return foo
   */
  public Function<Entity, Boolean> getOnInteraction() {
    return onInteraction;
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public Function<Entity, MyEnum> getGetEnum() {
    return getEnum;
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public Function<MyEnum, Boolean> getFunctionWithEnumParam() {
    return functionWithEnumParam;
  }

  /**
   * WTF? .
   *
   * @param entity foo
   */
  public TestComponentWithFunctionCallback(@DSLContextMember(name = "entity") Entity entity) {
    super(entity);
    this.entity = entity;
  }
}
