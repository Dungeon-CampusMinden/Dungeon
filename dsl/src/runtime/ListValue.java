package runtime;

import semanticanalysis.types.ListType;

import java.util.ArrayList;
import java.util.List;

public class ListValue extends Value {
    public ListValue(ListType dataType) {
        super(dataType, new ArrayList<Value>());
    }

    public void addValue(Value value) {
        ((ArrayList<Value>) this.object).add(value);
    }

    public Value getValue(int index) {
        return ((ArrayList<Value>) this.object).get(index);
    }

    public List<Value> getValues() {
        return (List<Value>) this.object;
    }
}
