package runtime;

import semanticanalysis.types.ListType;

import java.util.ArrayList;

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
}
