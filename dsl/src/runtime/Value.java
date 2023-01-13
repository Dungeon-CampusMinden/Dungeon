package runtime;

import semanticAnalysis.types.BuiltInType;
import semanticAnalysis.types.IType;

// TODO: should this be able to be undefined?

/**
 * This class is used to represent a value in a {@link MemorySpace}, that is a combination of actual
 * value, dataType (defined by {@link IType} and optional reference to a {@link
 * semanticAnalysis.Symbol} from a {@link semanticAnalysis.SymbolTable}, as this class is basically
 * the runtime equivalent of a {@link semanticAnalysis.Symbol}
 */
public class Value implements IClonable {
    public static Value NONE = new Value(null, null, -1, false);

    protected final IType dataType;
    protected Object value;
    protected final int symbolIdx;
    protected final boolean isMutable;
    protected boolean dirty;

    public boolean isDirty() {
        return this.dirty;
    }

    public void setDirty() {
        this.dirty = true;
    }

    /**
     * Getter for the internal, underlying value
     *
     * @return internal, underlying value
     */
    public Object getInternalObject() {
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
    // TODO: this is only used for recovery of the AST corresponding to a function definition,
    // otherwise
    //  this is not used.. should find a better solution for the function part and simplify all
    // other cases
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

            this.dirty = true;
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

        this.dirty = false;
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

        this.dirty = true;
    }

    /**
     * Get default value for different builtin data types
     *
     * @param type The datatype
     * @return Object set to the default value for passed datatype, or null, if datatype is no
     *     builtin type
     */
    public static Object getDefaultValue(IType type) {
        if (type == null) {
            return null;
        }
        var typeName = type.getName();
        if (typeName.equals(BuiltInType.intType.getName())) {
            return 0;
        } else if (typeName.equals(BuiltInType.stringType.getName())) {
            return "";
        } else if (typeName.equals(BuiltInType.graphType.getName())) {
            return new graph.Graph<String>(null, null);
        } else {
            return null;
        }
    }

    @Override
    public Object clone() {
        var cloned = new Value(this.dataType, this.value, this.symbolIdx, this.isMutable);
        cloned.dirty = this.dirty;
        return cloned;
    }
}
