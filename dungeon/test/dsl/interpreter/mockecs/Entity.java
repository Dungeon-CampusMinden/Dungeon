package dsl.interpreter.mockecs;

import dsl.annotation.DSLContextPush;
import dsl.annotation.DSLType;
import dsl.annotation.DSLTypeMember;
import dsl.annotation.DSLTypeProperty;
import dsl.semanticanalysis.typesystem.*;
import dsl.semanticanalysis.typesystem.extension.IDSLExtensionProperty;
import java.util.ArrayList;
import java.util.List;

/** WTF? . */
@DSLType(name = "entity")
@DSLContextPush(name = "entity")
public class Entity {
  /** WTF? . */
  @DSLTypeProperty(name = "test_component2", extendedType = Entity.class, isSettable = false)
  public static class TestComponent2Property
      implements IDSLExtensionProperty<Entity, TestComponent2> {
    /** WTF? . */
    public static Entity.TestComponent2Property instance = new Entity.TestComponent2Property();

    private TestComponent2Property() {}

    @Override
    public void set(Entity instance, TestComponent2 valueToSet) {}

    @Override
    public TestComponent2 get(Entity instance) {
      var list = instance.components.stream().filter(c -> c instanceof TestComponent2).toList();
      if (list.size() == 0) {
        return null;
      } else {
        return (TestComponent2) list.get(0);
      }
    }
  }

  /** WTF? . */
  @DSLTypeProperty(name = "test_component1", extendedType = Entity.class, isSettable = false)
  public static class TestComponent1Property
      implements IDSLExtensionProperty<Entity, TestComponent1> {
    /** WTF? . */
    public static Entity.TestComponent1Property instance = new Entity.TestComponent1Property();

    private TestComponent1Property() {}

    @Override
    public void set(Entity instance, TestComponent1 valueToSet) {}

    @Override
    public TestComponent1 get(Entity instance) {
      var list = instance.components.stream().filter(c -> c instanceof TestComponent1).toList();
      if (list.size() == 0) {
        return null;
      } else {
        return (TestComponent1) list.get(0);
      }
    }
  }

  /** WTF? . */
  @DSLTypeProperty(
      name = "test_component_with_external_type",
      extendedType = Entity.class,
      isSettable = false)
  public static class TestComponentWithExternalTypeProperty
      implements IDSLExtensionProperty<Entity, TestComponentWithExternalType> {
    /** WTF? . */
    public static Entity.TestComponentWithExternalTypeProperty instance =
        new Entity.TestComponentWithExternalTypeProperty();

    private TestComponentWithExternalTypeProperty() {}

    @Override
    public void set(Entity instance, TestComponentWithExternalType valueToSet) {}

    @Override
    public TestComponentWithExternalType get(Entity instance) {
      var list =
          instance.components.stream()
              .filter(c -> c instanceof TestComponentWithExternalType)
              .toList();
      if (list.size() == 0) {
        return null;
      } else {
        return (TestComponentWithExternalType) list.get(0);
      }
    }
  }

  /** WTF? . */
  @DSLTypeProperty(
      name = "component_with_external_type_member",
      extendedType = Entity.class,
      isSettable = false)
  public static class ComponentWithExternalTypeMemberProperty
      implements IDSLExtensionProperty<Entity, ComponentWithExternalTypeMember> {
    /** WTF? . */
    public static Entity.ComponentWithExternalTypeMemberProperty instance =
        new Entity.ComponentWithExternalTypeMemberProperty();

    private ComponentWithExternalTypeMemberProperty() {}

    @Override
    public void set(Entity instance, ComponentWithExternalTypeMember valueToSet) {}

    @Override
    public ComponentWithExternalTypeMember get(Entity instance) {
      var list =
          instance.components.stream()
              .filter(c -> c instanceof ComponentWithExternalTypeMember)
              .toList();
      if (list.size() == 0) {
        return null;
      } else {
        return (ComponentWithExternalTypeMember) list.get(0);
      }
    }
  }

  private static int _idx;

  /** WTF? . */
  public List<Component> components = new ArrayList<>();

  @DSLTypeMember private int idx;

  /**
   * WTF? .
   *
   * @return foo
   */
  public int getIdx() {
    return idx;
  }

  /** WTF? . */
  public Entity() {
    this.idx = _idx++;
  }
}
