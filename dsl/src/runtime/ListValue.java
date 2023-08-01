package runtime;

import semanticanalysis.types.ListType;

import java.util.ArrayList;
import java.util.List;

/** Implements a list value. */
public class ListValue extends Value {
    /**
     * Constructor
     *
     * @param dataType The type of the list
     */
    public ListValue(ListType dataType) {
        super(dataType, new ArrayList<Value>());
    }

    public ListType getDataType() {
        return (ListType) this.dataType;
    }

    /**
     * Add a Value to the list
     *
     * @param value the value to add
     */
    public void addValue(Value value) {
        ((ArrayList<Value>) this.object).add(value);
    }

    /**
     * Get a value by index
     *
     * @param index the index
     * @return the Value at specified index
     */
    public Value getValue(int index) {
        return ((ArrayList<Value>) this.object).get(index);
    }

    /**
     * Return all stored Values
     *
     * @return the stored Values
     */
    public List<Value> getValues() {
        return (List<Value>) this.object;
    }

    public void clearList() {
        ((List<Value>) this.object).clear();
        ;
    }
}
