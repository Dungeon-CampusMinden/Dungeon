package interpreter.mockECS;

import semanticAnalysis.types.DSLContextMember;
import semanticAnalysis.types.DSLType;
import semanticAnalysis.types.DSLTypeMember;

@DSLType
public class TestComponent1 {
    private Entity entity;

    public Entity getEntity() {
        return entity;
    }

    @DSLTypeMember private int member1;
    @DSLTypeMember private int member2;
    @DSLTypeMember private String member3;

    public TestComponent1(@DSLContextMember(name = "entity") Entity entity) {
        this.entity = entity;
        member3 = "DEFAULT VALUE";
    }
    /*public TestComponent1() {
        // this.entity = entity;
        member3 = "DEFAULT VALUE";
    }*/
}
