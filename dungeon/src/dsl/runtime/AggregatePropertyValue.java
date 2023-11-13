package dsl.runtime;

import dsl.semanticanalysis.types.DSLTypeProperty;
import dsl.semanticanalysis.types.IDSLTypeProperty;
import dsl.semanticanalysis.types.IType;

/**
 * Represents a property value, which encapsulated an {@link IDSLTypeProperty} and an object to use
 * as the instance to use with this {@link IDSLTypeProperty} and returns an aggregate value. This
 * means, that this class does not store the actual object representing the aggregate value, but the
 * 'parent-object', of which the aggregate value can be retrieved by means of the {@link
 * IDSLTypeProperty} .
 */
public class AggregatePropertyValue extends AggregateValue {
    private final IDSLTypeProperty<Object, Object> property;
    private IEvironment environment;
    private IMemorySpace parentMemorySpace;
    private final boolean isSettable;
    private final boolean isGettable;

    public AggregatePropertyValue(
            IType type,
            IDSLTypeProperty<Object, Object> property,
            Object instance,
            IMemorySpace parentMemorySpace,
            IEvironment environment) {
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
     * As this AggregatePropertyValue does not store the aggregate value directly, we need to call
     * the {@link IDSLTypeProperty#get(Object)} implementation. This will return an aggregate value,
     * which is converted in an actual {@link AggregateValue} instance, of which the {@link
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
