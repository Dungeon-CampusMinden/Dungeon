package dsl.interpreter.mockecs;

import dsl.annotation.DSLContextMember;
import dsl.annotation.DSLType;
import dsl.annotation.DSLTypeMember;
import java.util.List;

/** WTF? . */
@DSLType
public class TestComponentWithListMember extends Component {
  private Entity entity;

  /**
   * WTF? .
   *
   * @return foo
   */
  public Entity getEntity() {
    return entity;
  }

  @DSLTypeMember List<Integer> intList;
  @DSLTypeMember List<Float> floatList;

  /**
   * WTF? .
   *
   * @param entity foo
   */
  public TestComponentWithListMember(@DSLContextMember(name = "entity") Entity entity) {
    super(entity);
    this.entity = entity;
  }
}
