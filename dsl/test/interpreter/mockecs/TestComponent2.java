package interpreter.mockecs;

import semanticanalysis.types.*;

@DSLType
public class TestComponent2 extends Component {

    @DSLTypeProperty(name="this_is_a_float", extendedType = TestComponent2.class)
    public static class TestComponentPseudoProperty implements IDSLTypeProperty<TestComponent2, Float> {
        public static TestComponentPseudoProperty instance = new TestComponentPseudoProperty();
        private TestComponentPseudoProperty(){}
        @Override
        public void set(TestComponent2 instance, Float valueToSet) {
        }

        @Override
        public Float get(TestComponent2 instance) {
            return (float) instance.member2 + 3.14f;
        }

        @Override
        public boolean isSettable() {
            return false;
        }

        @Override
        public boolean isGettable() {
            return true;
        }
    }

    private Entity entity;

    public Entity getEntity() {
        return entity;
    }

    @DSLTypeMember private String member1;
    @DSLTypeMember private int member2;
    @DSLTypeMember private String member3;

    public TestComponent2(@DSLContextMember(name = "entity") Entity entity) {
        super(entity);
        this.entity = entity;
        member3 = "DEFAULT VALUE";
    }

    public String getMember1() {
        return member1;
    }

    public int getMember2() {
        return member2;
    }

    public String getMember3() {
        return member3;
    }
}

