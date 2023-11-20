package dsl.interpreter.mockecs;

import dsl.semanticanalysis.typesystem.typebuilding.annotation.DSLType;
import dsl.semanticanalysis.typesystem.typebuilding.annotation.DSLTypeNameMember;

@DSLType
public class TestClassWithName {
    @DSLTypeNameMember private String name;

    public TestClassWithName() {}
}
