package dsl.interpreter.mockecs;

import dsl.annotation.DSLCallback;
import dsl.annotation.DSLContextMember;
import dsl.annotation.DSLType;
import dsl.annotation.DSLTypeMember;
import java.util.function.Consumer;

/** WTF? . */
@DSLType
public class TestComponentStringMemberAndCallback extends Component {
  private Entity entity;

  /**
   * WTF? .
   *
   * @return foo
   */
  public Entity getEntity() {
    return entity;
  }

  @DSLTypeMember private String member1;

  @DSLCallback Consumer<TestComponent2> consumer;

  /**
   * WTF? .
   *
   * @param entity foo
   */
  public TestComponentStringMemberAndCallback(@DSLContextMember(name = "entity") Entity entity) {
    super(entity);
    this.entity = entity;
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public String getMember1() {
    return member1;
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public Consumer<TestComponent2> getConsumer() {
    return consumer;
  }
}
