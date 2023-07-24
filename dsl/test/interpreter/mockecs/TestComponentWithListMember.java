package interpreter.mockecs;

import semanticanalysis.types.DSLContextMember;
import semanticanalysis.types.DSLType;
import semanticanalysis.types.DSLTypeMember;

import java.util.List;

@DSLType
public class TestComponentWithListMember extends Component {
    private Entity entity;

    public Entity getEntity() {
        return entity;
    }

    @DSLTypeMember List<Integer> intList;
    @DSLTypeMember List<Float> floatList;

    public TestComponentWithListMember(@DSLContextMember(name = "entity") Entity entity) {
        super(entity);
        this.entity = entity;
    }
}
