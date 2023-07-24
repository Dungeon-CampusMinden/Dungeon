package runtime;

import semanticanalysis.types.SetType;

import java.util.HashSet;
import java.util.Set;

public class SetValue extends Value {
    private HashSet<Object> internalValueSet = new HashSet<>();
    public SetValue(SetType dataType) {
        super(dataType, new HashSet<Value>());
    }

    public boolean addValue(Value value) {
        var internalValue = value.getInternalValue();
        if (internalValueSet.contains(internalValue)) {
            return false;
        }
        internalValueSet.add(object);
        ((HashSet<Value>)this.object).add(value);
        return true;
    }

    public Set<Value> getValues() {
        return (Set<Value>)this.object;
    }
}
