package interpreter;

import semanticAnalysis.types.DSLType;
import semanticAnalysis.types.DSLTypeMember;

@DSLType
public class ComponentWithDefaultCtor {
    @DSLTypeMember private String member1;
    @DSLTypeMember private int member2;
    @DSLTypeMember private String memberWithDefaultValue;

    public ComponentWithDefaultCtor() {
        this.memberWithDefaultValue = "DEFAULT VALUE";
    }
}
