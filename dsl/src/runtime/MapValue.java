package runtime;

import semanticanalysis.types.MapType;
import semanticanalysis.types.SetType;

import java.util.HashMap;
import java.util.Map;

public class MapValue extends Value {

    // stores the internal values of the Value-instances in order to ensure,
    // that only Value-instances with distinct internal values are stored
    private HashMap<Object, Object> internalValueSet = new HashMap<>();

    /**
     * Constructor
     *
     * @param dataType type of the set
     */
    public MapValue(SetType dataType) {
        super(dataType, new HashMap<Value, Value>());
    }

    public MapType getDataType() {
        return (MapType) this.dataType;
    }

    /**
     * @return the internal HashMap of this {@link SetValue}.
     */
    public HashMap<Value, Value> internalMap() {
        return ((HashMap<Value, Value>) this.object);
    }

    /**
     * Add a Value to the set. The Value will only be added to the set, if no other Value with the
     * same internal value of the passed Value is already stored in this set.
     *
     * @param value the Value to store in the set
     * @return true, if the Value was added, false otherwise
     */
    public boolean addValue(Value key, Value value) {
        // TODO
        throw new UnsupportedOperationException();
    }

    /**
     * @return all stored values
     */
    public Map<Value, Value> getValues() {
        // TODO
        throw new UnsupportedOperationException();
    }

    public void clearMap() {
        internalMap().clear();
    }


    // region native_methods
    /**
     * Native method, which implements adding a Value to the internal {@link Map} of a {@link
     * MapValue}.
     */
    // TODO:
    /*
    public static class AddMethod implements IInstanceCallable {

        public static MapValue.AddMethod instance = new MapValue.AddMethod();

        private AddMethod() {}

        @Override
        public Object call(DSLInterpreter interpreter, Object instance, List<Node> parameters) {
            MapValue setValue = (SetValue) instance;
            Node paramNode = parameters.get(0);
            Value paramValue = (Value) paramNode.accept(interpreter);

            return setValue.addValue(paramValue);
        }
    }*/

    /**
     * Native method, which implements calculating the size (i.e. the number of stored elements of a
     * {@link MapValue}.
     */
    /*
    public static class SizeMethod implements IInstanceCallable {

        public static MapValue.SizeMethod instance = new SetValue.SizeMethod();

        private SizeMethod() {}

        @Override
        public Object call(DSLInterpreter interpreter, Object instance, List<Node> parameters) {
            MapValue setValue = (SetValue) instance;

            return setValue.internalMap().size();
        }
    }

     */

    /**
     * Native method, which checks whether a given Value is present in the internal value set of a
     * {@link MapValue}. Because different instances of {@link Value} can refer to the same internal
     * value, the internal values are used for the lookup.
     */
    /*
    public static class ContainsMethod implements IInstanceCallable {

        public static MapValue.ContainsMethod instance = new SetValue.ContainsMethod();

        private ContainsMethod() {}

        @Override
        public Object call(DSLInterpreter interpreter, Object instance, List<Node> parameters) {
            MapValue setValue = (SetValue) instance;

            Node valueToCheckNode = parameters.get(0);
            Value valueToCheck = (Value) valueToCheckNode.accept(interpreter);

            return setValue.internalValueMap.contains(valueToCheck.getInternalValue());
        }
    }

     */
    // endregion

}
