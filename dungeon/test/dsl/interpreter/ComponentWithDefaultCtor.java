package dsl.interpreter;

import dsl.annotation.DSLType;
import dsl.annotation.DSLTypeMember;

@DSLType
public class ComponentWithDefaultCtor {
  @DSLTypeMember private String member1;
  @DSLTypeMember private int member2;
  @DSLTypeMember private String memberWithDefaultValue;

  public ComponentWithDefaultCtor() {
    this.memberWithDefaultValue = "DEFAULT VALUE";
  }
}
