package dsl.interpreter.mockecs;

import dsl.semanticanalysis.typesystem.typebuilding.annotation.DSLContextMember;
import dsl.semanticanalysis.typesystem.typebuilding.annotation.DSLType;
import dsl.semanticanalysis.typesystem.typebuilding.annotation.DSLTypeMember;

@DSLType
public class TestComponent1 extends Component {
    private Entity entity;

    public Entity getEntity() {
        return entity;
    }

    @DSLTypeMember private int member1;
    @DSLTypeMember private float member2;
    @DSLTypeMember private String member3;

    public TestComponent1(@DSLContextMember(name = "entity") Entity entity) {
        super(entity);
        this.entity = entity;
        member3 = "DEFAULT VALUE";
    }

    public int getMember1() {
        return member1;
    }

    public float getMember2() {
        return member2;
    }

    public String getMember3() {
        return member3;
    }
}
