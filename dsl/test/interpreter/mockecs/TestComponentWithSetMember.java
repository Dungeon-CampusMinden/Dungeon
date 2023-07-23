package interpreter.mockecs;

import semanticanalysis.types.DSLContextMember;
import semanticanalysis.types.DSLType;
import semanticanalysis.types.DSLTypeMember;

import java.util.List;
import java.util.Set;

@DSLType
public class TestComponentWithSetMember extends Component {
    private Entity entity;

    public Entity getEntity() {
        return entity;
    }

    @DSLTypeMember
    Set<Integer> intSet;
    @DSLTypeMember
    Set<Float> floatSet;

    public TestComponentWithSetMember(@DSLContextMember(name = "entity") Entity entity) {
        super(entity);
        this.entity = entity;
    }
}

