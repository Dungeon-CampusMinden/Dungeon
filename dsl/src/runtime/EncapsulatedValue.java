package runtime;

import semanticanalysis.types.IType;

import java.lang.reflect.Field;

public class EncapsulatedValue extends Value {
    private final Field field;

    // TODO: rename to EncapsulatedField
    public EncapsulatedValue(IType type, Field field, Object object) {
        super(type, object, true);
        this.field = field;
    }

    @Override
    public boolean setInternalValue(Object internalValue) {
        try {
            field.set(this.object, internalValue);
            return true;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object getInternalValue() {
        try {
            return field.get(this.object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
