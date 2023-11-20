package dsl.interpreter.mockecs;

import dsl.semanticanalysis.types.typebuilding.annotation.DSLType;
import dsl.semanticanalysis.types.typebuilding.annotation.DSLTypeMember;

@DSLType
public class ComplexType {
    @DSLTypeMember int member1;
    @DSLTypeMember float member2;
    @DSLTypeMember String member3;
    @DSLTypeMember Integer member4;
}
