package runtime;

import semanticanalysis.types.SetType;

import java.util.HashSet;
import java.util.Set;

/** Implements a set value */
public class SetValue extends Value {

    // stores the internal values of the Value-instances in order to ensure,
    // that only Value-instances with distinct internal values are stored
    private HashSet<Object> internalValueSet = new HashSet<>();

    /**
     * Constructor
     *
     * @param dataType type of the set
     */
    public SetValue(SetType dataType) {
        super(dataType, new HashSet<Value>());
    }

    /**
     * Add a Value to the set. The Value will only be added to the set, if no other Value with the
     * same internal value of the passed Value is already stored in this set.
     *
     * @param value the Value to store in the set
     * @return true, if the Value was added, false otherwise
     */
    public boolean addValue(Value value) {
        var internalValue = value.getInternalValue();
        if (internalValueSet.contains(internalValue)) {
            return false;
        }
        internalValueSet.add(object);
        ((HashSet<Value>) this.object).add(value);
        return true;
    }

    /**
     * @return all stored values
     */
    public Set<Value> getValues() {
        return (Set<Value>) this.object;
    }
}
