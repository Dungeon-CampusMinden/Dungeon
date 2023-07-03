package interpreter.mockecs;

import semanticanalysis.types.DSLContextMember;
import semanticanalysis.types.DSLType;
import semanticanalysis.types.DSLTypeMember;

@DSLType
public class TestComponentWithExternalType extends Component {
    @DSLTypeMember private int member1;
    @DSLTypeMember private ExternalType memberExternalType;

    public ExternalType getMemberExternalType() {
        return memberExternalType;
    }

    public TestComponentWithExternalType(@DSLContextMember(name = "entity") Entity entity) {
        super(entity);
        member1 = 0;
        memberExternalType = null;
    }
}
