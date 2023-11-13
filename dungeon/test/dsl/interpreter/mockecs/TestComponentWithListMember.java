package dsl.interpreter.mockecs;

import dsl.semanticanalysis.types.DSLContextMember;
import dsl.semanticanalysis.types.DSLType;
import dsl.semanticanalysis.types.DSLTypeMember;

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
