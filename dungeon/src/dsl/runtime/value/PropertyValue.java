package dsl.runtime.value;

import dsl.semanticanalysis.types.typebuilding.annotation.DSLTypeProperty;
import dsl.semanticanalysis.types.extension.IDSLTypeProperty;
import dsl.semanticanalysis.types.IType;

/**
 * A {@link Value}, which encapsulates an {@link IDSLTypeProperty} and an object, to use as an
 * instance for this property.
 */
public class PropertyValue extends Value {
    private final IDSLTypeProperty<Object, Object> property;
    private final boolean isSettable;
    private final boolean isGettable;

    public PropertyValue(IType type, IDSLTypeProperty<Object, Object> property, Object instance) {
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
