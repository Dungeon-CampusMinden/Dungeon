package interpreter.mockecs;

import interpreter.DSLInterpreter;

import semanticanalysis.types.*;

import java.util.Arrays;
import java.util.List;

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
    }

    @DSLExtensionMethod(name = "my_method", extendedType = TestComponent2.class)
    public static class MyMethod implements IDSLExtensionMethod<TestComponent2> {
        public static MyMethod instance = new MyMethod();

        @Override
        public Object call(DSLInterpreter interpreter, TestComponent2 instance, List<Object> params) {
            //TestComponent1 param1 = (TestComponent1) params[0];
            String param1 = (String) params.get(0);
            Integer param2 = (Integer) params.get(1);
            String param3 = (String) params.get(2);

            instance.member1 = param1;
            instance.member2 = param2;
            instance.member3 = param3;

            return this;
        }

        @Override
        public List<Class<?>> getParameterTypes() {
            //var arr = new Class<?>[] {TestComponent1.class, Integer.class, String.class};
            var arr = new Class<?>[] {String.class, Integer.class, String.class};
            return Arrays.stream(arr).toList();
        }

        @Override
        public Class<?> getReturnType() {
            return TestComponent2.class;
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

    public void setMember2(int value) {
        member2 = value;
    }

    public String getMember3() {
        return member3;
    }
}
