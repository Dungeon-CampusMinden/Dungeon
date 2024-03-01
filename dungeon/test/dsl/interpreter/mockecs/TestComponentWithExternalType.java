package dsl.interpreter.mockecs;

import dsl.annotation.DSLContextMember;
import dsl.annotation.DSLType;
import dsl.annotation.DSLTypeMember;

@DSLType
public class TestComponentWithExternalType extends Component {
  @DSLTypeMember private int member1;
  @DSLTypeMember private ExternalType memberExternalType;

  public ExternalType getMemberExternalType() {
    return memberExternalType;
  }

  public void setMemberExternalType(ExternalType value) {
    memberExternalType = value;
  }

  public TestComponentWithExternalType(@DSLContextMember(name = "entity") Entity entity) {
    super(entity);
    member1 = 0;
    memberExternalType = null;
  }
}
