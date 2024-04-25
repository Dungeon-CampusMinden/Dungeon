package dsl.interpreter.mockecs;

import dsl.annotation.*;
import dsl.semanticanalysis.typesystem.extension.IDSLExtensionMethod;
import dsl.semanticanalysis.typesystem.extension.IDSLExtensionProperty;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

/** WTF? . */
@DSLType
public class TestComponent2 extends Component {

  /** WTF? . */
  @DSLTypeProperty(name = "this_is_a_float", extendedType = TestComponent2.class)
  public static class TestComponentPseudoProperty
      implements IDSLExtensionProperty<TestComponent2, Float> {
    /** WTF? . */
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

  /** WTF? . */
  @DSLTypeProperty(name = "this_is_complex", extendedType = TestComponent2.class)
  public static class TestComponentPseudoPropertyComplexType
      implements IDSLExtensionProperty<TestComponent2, ComplexType> {
    /** WTF? . */
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

  /** WTF? . */
  @DSLExtensionMethod(name = "my_method", extendedType = TestComponent2.class)
  public static class MyMethod implements IDSLExtensionMethod<TestComponent2, TestComponent2> {
    /** WTF? . */
    public static MyMethod instance = new MyMethod();

    @Override
    public TestComponent2 call(TestComponent2 instance, List<Object> params) {
      Integer param1 = (Integer) params.get(0);
      Integer param2 = (Integer) params.get(1);

      instance.member2 = param1;
      instance.member3 = param2.toString();

      return instance;
    }

    @Override
    public List<Type> getParameterTypes() {
      var arr = new Type[] {Integer.class, Integer.class};
      return Arrays.stream(arr).toList();
    }
  }

  private Entity entity;

  /**
   * WTF? .
   *
   * @return foo
   */
  public Entity getEntity() {
    return entity;
  }

  @DSLTypeMember private String member1;
  @DSLTypeMember private int member2;
  @DSLTypeMember private String member3;

  private float hiddenFloat;
  private ComplexType hiddenComplexMember;

  /**
   * WTF? .
   *
   * @param entity foo
   */
  public TestComponent2(@DSLContextMember(name = "entity") Entity entity) {
    super(entity);
    this.entity = entity;
    member3 = "DEFAULT VALUE";
    this.hiddenComplexMember = new ComplexType();
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public String getMember1() {
    return member1;
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public int getMember2() {
    return member2;
  }

  /**
   * WTF? .
   *
   * @param value foo
   */
  public void setMember2(int value) {
    member2 = value;
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public String getMember3() {
    return member3;
  }
}
