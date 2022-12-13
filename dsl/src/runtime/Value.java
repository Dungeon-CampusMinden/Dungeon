package runtime;

import symboltable.IType;

// TODO: should this be able to be undefined?

/**
 * This class is used to represent a value in a {@link MemorySpace}, that is a combination of actual
 * value, dataType (defined by {@link IType} and optional reference to a {@link symboltable.Symbol}
 * from a {@link symboltable.SymbolTable}, as this class is basically the runtime equivalent of a
 * {@link symboltable.Symbol}
 */
public class Value {
    public static Value NONE = new Value(null, null, -1);

    private final IType dataType;
    private Object value;
    private final int symbolIdx;

    /**
     * Getter for the internal, underlying value
     *
     * @return internal, underlying value
     */
    public Object getValue() {
        return value;
    }

    /**
     * Getter for the datatype of this value
     *
     * @return the datatype of this value
     */
    public IType getDataType() {
        return dataType;
    }

    /**
     * Getter for index of the {@link symboltable.Symbol} linked to this Value
     *
     * @return index of the linked Symbol
     */
    public int getSymbolIdx() {
        return symbolIdx;
    }

    // TODO: should this check for datatype compatibility?

    /**
     * Setter for the internal, underlying value
     *
     * @param internalValue The value to set this {@link Value} to.
     */
    public void setInternalValue(Object internalValue) {
        this.value = internalValue;
    }

    /**
     * Constructor
     *
     * @param dataType The datatype of this value
     * @param internalValue The actual value stored in this value
     * @param symbolIdx The index of the {@link symboltable.Symbol} this Value corresponds to
     */
    public Value(IType dataType, Object internalValue, int symbolIdx) {
        this.value = internalValue;
        this.dataType = dataType;
        this.symbolIdx = symbolIdx;
    }
}
