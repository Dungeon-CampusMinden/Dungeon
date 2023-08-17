package runtime;

import semanticanalysis.types.IDSLTypeProperty;
import semanticanalysis.types.IType;

public class PropertyValue extends Value {
    private final IDSLTypeProperty<Object, Object> property;

    public PropertyValue(IType type, IDSLTypeProperty<Object, Object> property, Object instance) {
        super(type, instance);
        this.property = property;
    }

    @Override
    public Object getInternalValue() {
        if (!property.isGettable()) {
            return null;
        } else {
            return property.get(this.object);
        }
    }

    @Override
    public boolean setInternalValue(Object internalValue) {
        if (!property.isSettable()) {
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
