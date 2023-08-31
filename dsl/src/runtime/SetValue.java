package runtime;

import interpreter.DSLInterpreter;
import parser.ast.Node;
import semanticanalysis.IInstanceCallable;
import semanticanalysis.types.SetType;

import java.util.HashSet;
import java.util.List;
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

    public SetType getDataType() {
        return (SetType) this.dataType;
    }

    protected HashSet<Value> set() { return ((HashSet<Value>) this.object); }
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
        internalValueSet.add(internalValue);
        set().add(value);
        return true;
    }

    /**
     * @return all stored values
     */
    public Set<Value> getValues() {
        return set();
    }

    public void clearSet() {
        set().clear();
    }


    // region native_methods
    public static class AddMethod implements IInstanceCallable {

        public static SetValue.AddMethod instance = new SetValue.AddMethod();

        private AddMethod() {}

        @Override
        public Object call(DSLInterpreter interpreter, Object instance, List<Node> parameters) {
            SetValue setValue = (SetValue) instance;
            Node paramNode = parameters.get(0);
            Value paramValue = (Value) paramNode.accept(interpreter);

            return setValue.addValue(paramValue);
        }
    }

    public static class SizeMethod implements IInstanceCallable {

        public static SetValue.SizeMethod instance = new SetValue.SizeMethod();

        private SizeMethod() {}

        @Override
        public Object call(DSLInterpreter interpreter, Object instance, List<Node> parameters) {
            SetValue setValue = (SetValue) instance;

            return setValue.set().size();
        }
    }

    public static class ContainsMethod implements IInstanceCallable {

        public static SetValue.ContainsMethod instance = new SetValue.ContainsMethod();

        private ContainsMethod() {}

        @Override
        public Object call(DSLInterpreter interpreter, Object instance, List<Node> parameters) {
            SetValue setValue = (SetValue) instance;

            Node valueToCheckNode = parameters.get(0);
            Value valueToCheck = (Value) valueToCheckNode.accept(interpreter);

            return setValue.internalValueSet.contains(valueToCheck.getInternalValue());
        }
    }
    // endregion

}
