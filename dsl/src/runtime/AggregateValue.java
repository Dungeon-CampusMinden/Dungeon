package runtime;

import semanticanalysis.types.IType;

import java.util.Map;
import java.util.Set;

public class AggregateValue extends Value {

    /**
     * @return {@link IMemorySpace} holding the values of this AggregateValue
     */
    @Override
    public IMemorySpace getMemorySpace() {
        return memorySpace;
    }

    /**
     * Constructor
     *
     * @param datatype {@link IType} representing the datatype of the new AggregateValue
     * @param parentSpace the {@link IMemorySpace} in which the new AggregateValue was defined
     */
    public AggregateValue(IType datatype, IMemorySpace parentSpace) {
        super(datatype, null);
        initializeMemorySpace(parentSpace);
    }

    /**
     * Constructor
     *
     * @param datatype {@link IType} representing the datatype of the new AggregateValue
     * @param parentSpace the {@link IMemorySpace} in which the new AggregateValue was defined
     * @param internalValue an Object representing an internal value of the new AggregateValue
     */
    public AggregateValue(IType datatype, IMemorySpace parentSpace, Object internalValue) {
        super(datatype, internalValue);
        initializeMemorySpace(parentSpace);
    }

    private void initializeMemorySpace(IMemorySpace parentSpace) {
        this.memorySpace = new MemorySpace(parentSpace);
        this.memorySpace.bindValue(THIS_NAME, this);
    }

    /**
     * @param ms the {@link IMemorySpace} to set as the memory space of this AggregateValue
     */
    public void setMemorySpace(IMemorySpace ms) {
        ms.delete(THIS_NAME);
        ms.bindValue(THIS_NAME, this);
        this.memorySpace = ms;
    }

    /**
     * @return set of entries of values in the AggregateValues {@link IMemorySpace} (combination of
     *     value-name and {@link Value})
     */
    public Set<Map.Entry<String, Value>> getValueSet() {
        return this.getMemorySpace().getValueSet();
    }

    @Override
    public Object clone() {
        return this;
    }

    /**
     * Is this {@link AggregateValue} empty, e.g. is the internal value null and has it no other
     * member-Values than the {@link Value#THIS_NAME}-Value
     *
     * @return true, if this {@link AggregateValue} is empty, false otherwise
     */
    public boolean isEmpty() {
        boolean internalValueNull = this.getInternalValue() == null;

        var valueSet = this.memorySpace.getValueSet();

        // has this AggregateValue either no values or no values other than the "$THIS$"-value
        boolean noMemberValues =
                valueSet.size() == 0
                        || (valueSet.size() == 1
                                && valueSet.iterator().next().getKey().equals(Value.THIS_NAME));

        return internalValueNull && noMemberValues;
    }
}
