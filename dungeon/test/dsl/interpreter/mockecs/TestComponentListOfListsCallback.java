package dsl.interpreter.mockecs;

import dsl.annotation.DSLCallback;
import dsl.annotation.DSLContextMember;
import dsl.annotation.DSLType;
import java.util.List;
import java.util.function.Function;

/** WTF? . */
@DSLType
public class TestComponentListOfListsCallback extends Component {
  private Entity entity;

  /**
   * WTF? .
   *
   * @return foo
   */
  public Entity getEntity() {
    return entity;
  }

  @DSLCallback private Function<List<List<Entity>>, Boolean> onInteraction;

  /**
   * WTF? .
   *
   * @return foo
   */
  public Function<List<List<Entity>>, Boolean> getOnInteraction() {
    return onInteraction;
  }

  /**
   * WTF? .
   *
   * @param entity foo
   */
  public TestComponentListOfListsCallback(@DSLContextMember(name = "entity") Entity entity) {
    super(entity);
    this.entity = entity;
  }
}
