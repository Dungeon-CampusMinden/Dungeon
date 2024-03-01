package dsl.interpreter.mockecs;

import dsl.annotation.DSLCallback;
import dsl.annotation.DSLContextMember;
import dsl.annotation.DSLType;
import java.util.function.Consumer;

@DSLType
public class TestComponentWithStringConsumerCallback extends Component {
  private Entity entity;

  public Entity getEntity() {
    return entity;
  }

  @DSLCallback private Consumer<String> onInteraction;

  public TestComponentWithStringConsumerCallback(@DSLContextMember(name = "entity") Entity entity) {
    super(entity);
    this.entity = entity;
  }

  public void executeCallbackWithText(String text) {
    onInteraction.accept(text);
  }
}
