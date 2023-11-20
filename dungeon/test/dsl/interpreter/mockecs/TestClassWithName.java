package dsl.interpreter.mockecs;

import dsl.semanticanalysis.types.typebuilding.annotation.DSLType;
import dsl.semanticanalysis.types.typebuilding.annotation.DSLTypeNameMember;

@DSLType
public class TestClassWithName {
    @DSLTypeNameMember private String name;

    public TestClassWithName() {}
}
