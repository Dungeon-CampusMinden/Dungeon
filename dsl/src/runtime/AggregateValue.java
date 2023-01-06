package runtime;

import semanticAnalysis.types.AggregateType;

// TODO: isn't this just a memory Space? or has it a memory space? is kind of nicer because of no
// multiple inheritance
//  and no need to implement either Value or MemorySpace as an interface..
public class AggregateValue extends Value {
    private MemorySpace ms;

    public MemorySpace getMemorySpace() {
        return ms;
    }
    // private HashMap<String, Value> values;

    public AggregateValue(AggregateType datatype, int symbolIdx, MemorySpace parentSpace) {
        super(datatype, null, symbolIdx);
        this.ms = new MemorySpace(parentSpace);
    }

    private AggregateType datatype() {
        return (AggregateType) this.getDataType();
    }

    /**
     * @param name should match a name of a member of the internal datatype
     * @param value
     */
    /*
    public boolean addValue(String name, Value value) {
        var memberSymbol = datatype().resolve(name, false);
        if (memberSymbol.equals(Symbol.NULL)) {
            return false;
        }
        this.ms
        return true;
    }

    public Value getValue(String name) {
        var returnValue = Value.NONE;
        if (values.containsKey(name)) {
            returnValue = values.get(name);
        }
        return returnValue;
    }

     */
}
