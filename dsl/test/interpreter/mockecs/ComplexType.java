package interpreter.mockecs;

import semanticanalysis.types.DSLType;
import semanticanalysis.types.DSLTypeMember;

@DSLType
public class ComplexType {
    @DSLTypeMember int member1;
    @DSLTypeMember float member2;
    @DSLTypeMember String member3;
}
