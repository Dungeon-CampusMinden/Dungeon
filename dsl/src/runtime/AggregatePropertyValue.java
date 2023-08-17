package runtime;

import semanticanalysis.types.IDSLTypeProperty;
import semanticanalysis.types.IType;

public class AggregatePropertyValue extends AggregateValue {
    private final IDSLTypeProperty<Object, Object> property;
    private IEvironment environment;
    private IMemorySpace parentMemorySpace;

    public AggregatePropertyValue(
            IType type,
            IDSLTypeProperty<Object, Object> property,
            Object instance,
            IMemorySpace parentMemorySpace,
            IEvironment environment
    ) {
        super(type, parentMemorySpace);
        this.object = instance;
        this.property = property;
        this.environment = environment;
        this.parentMemorySpace = parentMemorySpace;
    }

    @Override
    public IMemorySpace getMemorySpace() {
        Object internalValue = this.getInternalValue();
        var runtimeObject = environment.getRuntimeObjectTranslator().translateRuntimeObject(internalValue, parentMemorySpace, environment);
        assert runtimeObject instanceof AggregateValue;
        return ((AggregateValue)runtimeObject).getMemorySpace();
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
