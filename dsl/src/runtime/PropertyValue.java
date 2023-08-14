package runtime;

import semanticanalysis.types.IDSLTypeProperty;
import semanticanalysis.types.IType;

public class PropertyValue extends Value {
    // TODO: this will be a problem, i guess
    private final IDSLTypeProperty<Object, Object> property;

    // the PropertyValue will (currenlty) be created in bindFromSymbol, in
    // which no concrete Object is constructed..
    // come to think of it, the PropertyValue only makes sense in the case,
    // where the enclosing AggregateValue encapsulated a concrete runtime-Object
    public PropertyValue(IType type, IDSLTypeProperty<Object, Object> property, AggregateValue parentValue) {
        super(type, parentValue);
        this.property = property;
    }

    @Override
    public Object getInternalValue() {
        if (!property.isGettable()) {
            return null;
        } else {
            AggregateValue parentValue = (AggregateValue) this.object;
            return property.get(parentValue.getInternalValue());
        }
    }

    @Override
    public boolean setInternalValue(Object internalValue) {
        if (!property.isSettable()) {
            return false;
        } else {
            try {
                AggregateValue parentValue = (AggregateValue) this.object;
                property.set(parentValue.getInternalValue(), internalValue);
                return true;
            } catch (ClassCastException ex) {
                return false;
            }
        }
    }
}
