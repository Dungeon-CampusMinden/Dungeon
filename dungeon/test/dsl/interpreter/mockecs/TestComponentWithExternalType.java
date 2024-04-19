package dsl.interpreter.mockecs;

import dsl.annotation.DSLContextMember;
import dsl.annotation.DSLType;
import dsl.annotation.DSLTypeMember;

/** WTF? . */
@DSLType
public class TestComponentWithExternalType extends Component {
  @DSLTypeMember private int member1;
  @DSLTypeMember private ExternalType memberExternalType;

  /**
   * WTF? .
   *
   * @return foo
   */
  public ExternalType getMemberExternalType() {
    return memberExternalType;
  }

  /**
   * WTF? .
   *
   * @param value foo
   */
  public void setMemberExternalType(ExternalType value) {
    memberExternalType = value;
  }

  /**
   * WTF? .
   *
   * @param entity foo
   */
  public TestComponentWithExternalType(@DSLContextMember(name = "entity") Entity entity) {
    super(entity);
    member1 = 0;
    memberExternalType = null;
  }
}
