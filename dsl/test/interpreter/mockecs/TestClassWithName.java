package interpreter.mockecs;

import semanticanalysis.types.DSLType;
import semanticanalysis.types.DSLTypeNameMember;

@DSLType
public class TestClassWithName {
    @DSLTypeNameMember private String name;

    public TestClassWithName() {}
}
