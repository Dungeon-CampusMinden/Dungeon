package dsl.runtime.value;

import dsl.annotation.DSLTypeProperty;
import dsl.runtime.memoryspace.IMemorySpace;
import dsl.runtime.memoryspace.MemorySpace;
import dsl.semanticanalysis.environment.IEnvironment;
import dsl.semanticanalysis.typesystem.extension.IDSLExtensionProperty;
import dsl.semanticanalysis.typesystem.typebuilding.type.IType;

/**
 * WTF? (erster Satz KURZ).
 *
 * <p>Represents a property value, which encapsulated an {@link IDSLExtensionProperty} and an object
 * to use as the instance to use with this {@link IDSLExtensionProperty} and returns an aggregate
 * value. This means, that this class does not store the actual object representing the aggregate
 * value, but the 'parent-object', of which the aggregate value can be retrieved by means of the
 * {@link IDSLExtensionProperty} .
 */
public class AggregatePropertyValue extends AggregateValue {
  private final IDSLExtensionProperty<Object, Object> property;
  private IEnvironment environment;
  private IMemorySpace parentMemorySpace;
  private final boolean isSettable;
  private final boolean isGettable;

  /**
   * WTF? .
   *
   * @param type foo
   * @param property foo
   * @param instance foo
   * @param parentMemorySpace foo
   * @param environment foo
   */
  public AggregatePropertyValue(
      IType type,
      IDSLExtensionProperty<Object, Object> property,
      Object instance,
      IMemorySpace parentMemorySpace,
      IEnvironment environment) {
    super(type, parentMemorySpace);
    this.object = instance;
    this.property = property;
    this.environment = environment;
    this.parentMemorySpace = parentMemorySpace;

    var annotation = property.getClass().getAnnotation(DSLTypeProperty.class);
    this.isSettable = annotation.isSettable();
    this.isGettable = annotation.isGettable();
  }

  /**
   * WTF? (erster Satz KURZ).
   *
   * <p>As this AggregatePropertyValue does not store the aggregate value directly, we need to call
   * the {@link IDSLExtensionProperty#get(Object)} implementation. This will return an aggregate
   * value, which is converted in an actual {@link AggregateValue} instance, of which the {@link
   * IMemorySpace} will contain the members of the aggregate value.
   */
  @Override
  public IMemorySpace getMemorySpace() {
    Object internalValue = this.getInternalValue();
    if (internalValue == null) {
      return MemorySpace.NONE;
    }

    var runtimeObject =
        environment
            .getRuntimeObjectTranslator()
            .translateRuntimeObject(internalValue, parentMemorySpace, environment);
    return runtimeObject.getMemorySpace();
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
