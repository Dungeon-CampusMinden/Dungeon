package interpreter.mockecs;

import semanticanalysis.types.*;

@DSLType
public class TestComponent2 extends Component {

    @DSLTypeProperty(name = "this_is_a_float", extendedType = TestComponent2.class)
    public static class TestComponentPseudoProperty
            implements IDSLTypeProperty<TestComponent2, Float> {
        public static TestComponentPseudoProperty instance = new TestComponentPseudoProperty();

        private TestComponentPseudoProperty() {}

        @Override
        public void set(TestComponent2 instance, Float valueToSet) {
            instance.hiddenFloat = valueToSet;
        }

        @Override
        public Float get(TestComponent2 instance) {
            return instance.hiddenFloat;
        }

        @Override
        public boolean isSettable() {
            return true;
        }

        @Override
        public boolean isGettable() {
            return true;
        }
    }

    @DSLTypeProperty(name = "this_is_complex", extendedType = TestComponent2.class)
    public static class TestComponentPseudoPropertyComplexType
            implements IDSLTypeProperty<TestComponent2, ComplexType> {
        public static TestComponentPseudoPropertyComplexType instance =
                new TestComponentPseudoPropertyComplexType();

        private TestComponentPseudoPropertyComplexType() {}

        @Override
        public void set(TestComponent2 instance, ComplexType valueToSet) {
            instance.hiddenComplexMember = valueToSet;
        }

        @Override
        public ComplexType get(TestComponent2 instance) {
            return instance.hiddenComplexMember;
        }

        @Override
        public boolean isSettable() {
            return true;
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

    private float hiddenFloat;
    private ComplexType hiddenComplexMember;

    public TestComponent2(@DSLContextMember(name = "entity") Entity entity) {
        super(entity);
        this.entity = entity;
        member3 = "DEFAULT VALUE";
        this.hiddenComplexMember = new ComplexType();
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
