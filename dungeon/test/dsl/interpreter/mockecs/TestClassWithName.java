package dsl.interpreter.mockecs;

import dsl.semanticanalysis.types.annotation.DSLType;
import dsl.semanticanalysis.types.annotation.DSLTypeNameMember;

@DSLType
public class TestClassWithName {
    @DSLTypeNameMember private String name;

    public TestClassWithName() {}
}
