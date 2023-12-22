package dsl.interpreter.mockecs;

import dsl.semanticanalysis.typesystem.typebuilding.annotation.DSLContextMember;
import dsl.semanticanalysis.typesystem.typebuilding.annotation.DSLType;
import dsl.semanticanalysis.typesystem.typebuilding.annotation.DSLTypeMember;
import java.util.Set;

@DSLType
public class TestComponentWithSetMember extends Component {
    private Entity entity;

    public Entity getEntity() {
        return entity;
    }

    @DSLTypeMember Set<Integer> intSet;
    @DSLTypeMember Set<Float> floatSet;

    public TestComponentWithSetMember(@DSLContextMember(name = "entity") Entity entity) {
        super(entity);
        this.entity = entity;
    }
}
