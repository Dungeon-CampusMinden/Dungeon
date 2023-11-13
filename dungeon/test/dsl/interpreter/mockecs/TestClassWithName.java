package dsl.interpreter.mockecs;

import dsl.semanticanalysis.types.DSLType;
import dsl.semanticanalysis.types.DSLTypeNameMember;

@DSLType
public class TestClassWithName {
    @DSLTypeNameMember private String name;

    public TestClassWithName() {}
}
