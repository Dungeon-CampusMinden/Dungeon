package runtime;

import semanticAnalysis.types.IType;

public class AggregateValue extends Value {
    protected IMemorySpace ms;

    public IMemorySpace getMemorySpace() {
        return ms;
    }

    public AggregateValue(IType datatype, IMemorySpace parentSpace) {
        super(datatype, null);
        this.ms = new MemorySpace(parentSpace);
    }

    public AggregateValue(IType datatype, IMemorySpace parentSpace, Object internalValue) {
        super(datatype, internalValue);
        this.ms = new MemorySpace(parentSpace);
    }

    public void setMemorySpace(IMemorySpace ms) {
        this.ms = ms;
    }
}
