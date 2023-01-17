package runtime;

import java.util.Map;
import java.util.Set;
import semanticAnalysis.types.IType;

public class AggregateValue extends Value {
    protected IMemorySpace ms;

    /**
     * @return {@link IMemorySpace} holding the values of this AggregateValue
     */
    public IMemorySpace getMemorySpace() {
        return ms;
    }

    /**
     * Constructor
     *
     * @param datatype {@link IType} representing the datatype of the new AggregateValue
     * @param parentSpace the {@link IMemorySpace} in which the new AggregateValue was defined
     */
    public AggregateValue(IType datatype, IMemorySpace parentSpace) {
        super(datatype, null);
        this.ms = new MemorySpace(parentSpace);
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
        this.ms = new MemorySpace(parentSpace);
    }

    /**
     * @param ms the {@link IMemorySpace} to set as the memory space of this AggregateValue
     */
    public void setMemorySpace(IMemorySpace ms) {
        this.ms = ms;
    }

    /**
     * @return set of entries of values in the AggregateValues {@link IMemorySpace} (combination of
     *     value-name and {@link Value})
     */
    public Set<Map.Entry<String, Value>> getValueSet() {
        return this.getMemorySpace().getValueSet();
    }
}
