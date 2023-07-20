package runtime;

import semanticanalysis.types.SetType;

import java.util.HashSet;

public class SetValue extends Value {
    public SetValue(SetType dataType) {
        super(dataType, new HashSet<Value>());
    }

    public void addValue(Value value) {
        ((HashSet<Value>) this.object).add(value);
    }
}
