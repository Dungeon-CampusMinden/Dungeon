package dsl.interpreter.mockecs;

import dsl.annotation.DSLCallback;
import dsl.annotation.DSLContextMember;
import dsl.annotation.DSLType;
import dsl.annotation.DSLTypeMember;
import java.util.function.Consumer;

@DSLType
public class TestComponentStringMemberAndCallback extends Component {
  private Entity entity;

  public Entity getEntity() {
    return entity;
  }

  @DSLTypeMember private String member1;

  @DSLCallback Consumer<TestComponent2> consumer;

  public TestComponentStringMemberAndCallback(@DSLContextMember(name = "entity") Entity entity) {
    super(entity);
    this.entity = entity;
  }

  public String getMember1() {
    return member1;
  }

  public Consumer<TestComponent2> getConsumer() {
    return consumer;
  }
}
