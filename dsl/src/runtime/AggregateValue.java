package runtime;

import semanticAnalysis.types.IType;

public class AggregateValue extends Value {
    protected IMemorySpace ms;

    public IMemorySpace getMemorySpace() {
        return ms;
    }

    public AggregateValue(IType datatype /*, int symbolIdx*/, IMemorySpace parentSpace) {
        super(datatype, null /*, symbolIdx*/);
        this.ms = new MemorySpace(parentSpace);
    }

    public AggregateValue(
            IType datatype /*, int symbolIdx*/ /*,*/,
            IMemorySpace parentSpace,
            Object internalValue) {
        super(datatype, internalValue /*, symbolIdx*/);
        this.ms = new MemorySpace(parentSpace);
    }

    // TODO: this should probably be another class alltogether
    // public AggregateValue(
    //        IType datatype/*, int symbolIdx*/, Object internalValue/*, IMemorySpace
    // ownMemorySpace*/) {
    //    super(datatype, internalValue/*, symbolIdx*/);
    //    this.ms = ownMemorySpace;
    // }

    public void setMemorySpace(IMemorySpace ms) {
        this.ms = ms;
    }
}
