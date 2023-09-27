package runtime;

import semanticanalysis.types.BuiltInType;
import semanticanalysis.types.IType;

import taskdependencygraph.TaskDependencyGraph;

// TODO: should this be able to be undefined?

/**
 * This class is used to represent a value in a {@link MemorySpace}, that is a combination of actual
 * value and a dataType (defined by {@link IType}
 */
public class Value implements IClonable {
    public static Value NONE = new Value(BuiltInType.noType, null, false);
    public static String THIS_NAME = "$THIS$";

    protected IType dataType;
    protected Object object;
    protected final boolean isMutable;
    protected boolean dirty;
    protected IMemorySpace memorySpace;

    /**
     * Indicates, if the internal value of this {@link Value} was set explicitly (e.g. in a
     * entity_type definition).
     *
     * @return true, if the internal value was set explicitly, false otherwise
     */
    public boolean isDirty() {
        return this.dirty;
    }

    /** Set the dirty flag to true */
    public void setDirty() {
        this.dirty = true;
    }

    /**
     * Getter for the internal, underlying value
     *
     * @return internal, underlying value
     */
    public Object getInternalValue() {
        return object;
    }

    /**
     * Getter for the datatype of this value
     *
     * @return the datatype of this value
     */
    public IType getDataType() {
        return dataType;
    }

    public void setDataType(IType type) {
        this.dataType = type;
    }

    /**
     * Return the {@link IMemorySpace} associated with this Value. Basic Values only store a
     * reference to themselves in this IMemorySpace. Therefore, it is only created on demand.
     *
     * @return the IMemorySpace associated with this Value
     */
    public IMemorySpace getMemorySpace() {
        if (this.memorySpace == null) {
            this.memorySpace = new MemorySpace();
            this.memorySpace.bindValue(THIS_NAME, this);
        }
        return this.memorySpace;
    }

    /**
     * Setter for the internal, underlying value
     *
     * @param internalValue The value to set this {@link Value} to.
     */
    public boolean setInternalValue(Object internalValue) {
        // TODO: should this check for datatype compatibility?
        if (isMutable) {
            this.object = internalValue;

            this.dirty = true;
            return true;
        }
        System.out.println("Tried to set internal value of immutable value");
        return false;
    }

    /**
     * Constructor
     *
     * @param dataType The datatype of this value
     * @param internalValue The actual value stored in this value //* @param symbolIdx The index of
     *     the {@link semanticanalysis.Symbol} this Value corresponds to
     */
    public Value(IType dataType, Object internalValue) {
        this.object = internalValue;
        this.dataType = dataType;
        this.isMutable = true;

        this.dirty = false;
    }

    /**
     * Constructor
     *
     * @param dataType The datatype of this value
     * @param internalValue The actual value stored in this value //* @param symbolIdx The index of
     *     the {@link semanticanalysis.Symbol} this Value corresponds to
     */
    public Value(IType dataType, Object internalValue, boolean isMutable) {
        this.object = internalValue;
        this.dataType = dataType;
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
        } else if (typeName.equals(BuiltInType.floatType.getName())) {
            return 0.0f;
        } else if (typeName.equals(BuiltInType.stringType.getName())) {
            return "";
        } else if (typeName.equals(BuiltInType.graphType.getName())) {
            return new TaskDependencyGraph(null, null);
        } else {
            return null;
        }
    }

    @Override
    public Object clone() {
        var cloned = new Value(this.dataType, this.object, this.isMutable);
        cloned.dirty = this.dirty;
        return cloned;
    }

    @Override
    public String toString() {
        var internalValue = this.getInternalValue();
        if (this == Value.NONE) {
            return "[no value]";
        }
        if (internalValue == null) {
            return "[internal value of {Value@hc:" + this.hashCode() + "} is null]";
        } else {
            return internalValue.toString();
        }
    }
}
