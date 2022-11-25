package runtime;

import symboltable.IType;

// TODO: should this be able to be undefined?
public class Value {
    public static Value NONE = new Value(null, null, -1);

    private final IType dataType;
    private final Object value;
    private final int symbolIdx;

    public Object getValue() {
        return value;
    }

    public IType getDataType() {
        return dataType;
    }
    public int getSymbolIdx() {
        return symbolIdx;
    }

    public Value(IType dataType, Object value, int symbolIdx) {
        this.value = value;
        this.dataType = dataType;
        this.symbolIdx = symbolIdx;
    }
}
