package dsl.interpreter.mockecs;

import dsl.annotation.DSLType;
import dsl.annotation.DSLTypeMember;

@DSLType
public class ComplexType {
  @DSLTypeMember int member1;
  @DSLTypeMember float member2;
  @DSLTypeMember String member3;
  @DSLTypeMember Integer member4;

  public int getMember1() {
    return member1;
  }

  public float getMember2() {
    return member2;
  }

  public String getMember3() {
    return member3;
  }

  public Integer getMember4() {
    return member4;
  }
}
