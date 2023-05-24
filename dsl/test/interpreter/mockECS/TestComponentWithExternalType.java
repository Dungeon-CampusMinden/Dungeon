package interpreter.mockECS;

import semanticanalysis.types.DSLType;
import semanticanalysis.types.DSLTypeMember;

@DSLType
public class TestComponentWithExternalType {
    @DSLTypeMember private int member1;
    @DSLTypeMember private ExternalType memberExternalType;

    public ExternalType getMemberExternalType() {
        return memberExternalType;
    }

    public TestComponentWithExternalType() {
        member1 = 0;
        memberExternalType = null;
    }
}
