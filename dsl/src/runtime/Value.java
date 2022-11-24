package runtime;

import symboltable.IType;

// TODO: should this be able to be undefined?
public class Value {
    public static Value NONE = new Value(null, null);

    private IType dataType;
    private Object value;

    public Object getValue() {
        return value;
    }

    public IType getDataType() {
        return dataType;
    }

    public Value(IType dataType, Object value) {
        this.value = value;
        this.dataType = dataType;
    }
}
