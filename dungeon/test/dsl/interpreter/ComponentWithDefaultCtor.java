package dsl.interpreter;

import dsl.annotation.DSLType;
import dsl.annotation.DSLTypeMember;

/** WTF? . */
@DSLType
public class ComponentWithDefaultCtor {
  @DSLTypeMember private final String memberWithDefaultValue;
  @DSLTypeMember private String member1;
  @DSLTypeMember private int member2;

  /** WTF? . */
  public ComponentWithDefaultCtor() {
    this.memberWithDefaultValue = "DEFAULT VALUE";
  }
}
