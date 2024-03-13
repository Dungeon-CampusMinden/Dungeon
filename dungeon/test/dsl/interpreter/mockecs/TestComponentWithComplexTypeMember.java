package dsl.interpreter.mockecs;

import dsl.annotation.DSLContextMember;
import dsl.annotation.DSLType;
import dsl.annotation.DSLTypeMember;

@DSLType
public class TestComponentWithComplexTypeMember extends Component {
  @DSLTypeMember private ComplexType memberComplexType;

  public ComplexType getMemberComplexType() {
    return memberComplexType;
  }

  public void setMemberComplexTypeType(ComplexType value) {
    this.memberComplexType = value;
  }

  public TestComponentWithComplexTypeMember(@DSLContextMember(name = "entity") Entity entity) {
    super(entity);
  }
}
