package dsl.runtime.value;

import dsl.annotation.DSLTypeProperty;
import dsl.semanticanalysis.typesystem.extension.IDSLExtensionProperty;
import dsl.semanticanalysis.typesystem.typebuilding.type.IType;

/**
 * A {@link Value}, which encapsulates an {@link IDSLExtensionProperty} and an object, to use as an
 * instance for this property.
 */
public class PropertyValue extends Value {
  private final IDSLExtensionProperty<Object, Object> property;
  private final boolean isSettable;
  private final boolean isGettable;

  /**
   * WTF? .
   *
   * @param type foo
   * @param property foo
   * @param instance foo
   */
  public PropertyValue(
      IType type, IDSLExtensionProperty<Object, Object> property, Object instance) {
    super(type, instance);
    this.property = property;

    var annotation = property.getClass().getAnnotation(DSLTypeProperty.class);
    this.isSettable = annotation.isSettable();
    this.isGettable = annotation.isGettable();
  }

  @Override
  public Object getInternalValue() {
    if (!this.isGettable) {
      return null;
    } else {
      return property.get(this.object);
    }
  }

  @Override
  public boolean setInternalValue(Object internalValue) {
    if (!this.isSettable) {
      return false;
    } else {
      try {
        property.set(this.object, internalValue);
        return true;
      } catch (ClassCastException ex) {
        return false;
      }
    }
  }
}
