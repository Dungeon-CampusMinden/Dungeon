package runtime;

import semanticAnalysis.types.IType;

// TODO: should this be able to be undefined?

/**
 * This class is used to represent a value in a {@link MemorySpace}, that is a combination of actual
 * value, dataType (defined by {@link IType} and optional reference to a {@link
 * semanticAnalysis.Symbol} from a {@link semanticAnalysis.SymbolTable}, as this class is basically
 * the runtime equivalent of a {@link semanticAnalysis.Symbol}
 */
public class Value {
    public static Value NONE = new Value(null, null, -1, false);

    private final IType dataType;
    private Object value;
    private final int symbolIdx;
    private final boolean isMutable;

    /**
     * Getter for the internal, underlying value
     *
     * @return internal, underlying value
     */
    public Object getInternalValue() {
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
     * Getter for index of the {@link semanticAnalysis.Symbol} linked to this Value
     *
     * @return index of the linked Symbol
     */
    public int getSymbolIdx() {
        return symbolIdx;
    }

    /**
     * Setter for the internal, underlying value
     *
     * @param internalValue The value to set this {@link Value} to.
     */
    public boolean setInternalValue(Object internalValue) {
        // TODO: should this check for datatype compatibility?
        if (isMutable) {
            this.value = internalValue;
            return true;
        }
        return false;
    }

    /**
     * Constructor
     *
     * @param dataType The datatype of this value
     * @param internalValue The actual value stored in this value
     * @param symbolIdx The index of the {@link semanticAnalysis.Symbol} this Value corresponds to
     */
    public Value(IType dataType, Object internalValue, int symbolIdx) {
        this.value = internalValue;
        this.dataType = dataType;
        this.symbolIdx = symbolIdx;
        this.isMutable = true;
    }

    /**
     * Constructor
     *
     * @param dataType The datatype of this value
     * @param internalValue The actual value stored in this value
     * @param symbolIdx The index of the {@link semanticAnalysis.Symbol} this Value corresponds to
     */
    public Value(IType dataType, Object internalValue, int symbolIdx, boolean isMutable) {
        this.value = internalValue;
        this.dataType = dataType;
        this.symbolIdx = symbolIdx;
        this.isMutable = isMutable;
    }
}
