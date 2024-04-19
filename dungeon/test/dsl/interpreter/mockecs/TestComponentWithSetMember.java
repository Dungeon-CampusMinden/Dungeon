package dsl.interpreter.mockecs;

import dsl.annotation.DSLContextMember;
import dsl.annotation.DSLType;
import dsl.annotation.DSLTypeMember;
import java.util.Set;

/** WTF? . */
@DSLType
public class TestComponentWithSetMember extends Component {
  private Entity entity;

  /**
   * WTF? .
   *
   * @return foo
   */
  public Entity getEntity() {
    return entity;
  }

  @DSLTypeMember Set<Integer> intSet;
  @DSLTypeMember Set<Float> floatSet;

  /**
   * WTF? .
   *
   * @param entity foo
   */
  public TestComponentWithSetMember(@DSLContextMember(name = "entity") Entity entity) {
    super(entity);
    this.entity = entity;
  }
}
