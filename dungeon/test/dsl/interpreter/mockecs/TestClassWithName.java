package dsl.interpreter.mockecs;

import dsl.annotation.DSLType;
import dsl.annotation.DSLTypeNameMember;

@DSLType
public class TestClassWithName {
  @DSLTypeNameMember private String name;

  public TestClassWithName() {}
}
