package runtime;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import symboltable.BuiltInType;
import symboltable.IType;
import symboltable.Symbol;

// TODO: also use this for object-instantiation?
// TODO: does this need to be specialized for function memory space -> just try it
public class MemorySpace {
    public static MemorySpace NONE;
    private final HashMap<String, Value> values = new HashMap<>();
    private final MemorySpace parent;

    /**
     * Constructor
     *
     * @param parent parent MemorySpace
     */
    public MemorySpace(MemorySpace parent) {
        this.parent = parent;
    }

    /** Constructor, parent will be set to NONE */
    public MemorySpace() {
        this.parent = NONE;
    }

    /**
     * Bind a new {@link Value} in this memory space from a {@link Symbol} and will set it's
     * underlying value to the default value for it's type. This will also store the symbols idx in
     * the {@link Value} object. If a Value with the name already exists, no new Value will be
     * created.
     *
     * @param symbol the {@link Symbol} to create and bind a new {@link Value} from
     * @return True, if creation and binding succeeded, false otherwise.
     */
    public boolean bindFromSymbol(Symbol symbol) {
        var symbolName = symbol.getName();
        if (values.containsKey(symbolName)) {
            return false;
        } else if (!(symbol instanceof IType)) {
            var defaultValue = getDefaultValue(symbol.getDataType());
            var val = new Value(symbol.getDataType(), defaultValue, symbol.getIdx());
            values.put(symbolName, val);
            return true;
        }
        return false;
    }

    /**
     * Creates and binds a {@link Value} with passed name, datatype and underlying value, if none of
     * the same name exists in the stored values
     *
     * @param name the name of the Value
     * @param value the underlying value
     * @param datatype the datatype of the value
     * @return True, if no {@link Value} is found in the stored values, false otherwise
     */
    public boolean bindWithObject(String name, Object value, IType datatype) {
        if (values.containsKey(name)) {
            return false;
        } else {
            var val = new Value(datatype, value, -1);
            values.put(name, val);
            return true;
        }
    }

    /**
     * Returns all stored Values
     *
     * @return A Set containing all entries from the value Map
     */
    public Set<Map.Entry<String, Value>> getAllValues() {
        return values.entrySet();
    }

    /**
     * Lookup passed name and return the stored {@link Value}, if one exists. If no Value was found
     * in own stored values, resolve it in the parent {@link MemorySpace}
     *
     * @param name The name to resolve
     * @return The resolved {@link Value} or Value.NONE, if the name could not be resolved
     */
    public Value resolve(String name) {
        return this.resolve(name, true);
    }

    /**
     * Lookup passed name and return the stored {@link Value}, if one exists.
     *
     * @param name The name to resolve
     * @param resolveInParent if set to true, and no {@link Value} could be found in own stored
     *     values, the name will be resolved in the parent {@link MemorySpace}
     * @return The resolved {@link Value} or Value.NONE, if the name could not be resolved
     */
    public Value resolve(String name, boolean resolveInParent) {
        if (this.values.containsKey(name)) {
            return this.values.get(name);
        } else if (this.parent != NONE && resolveInParent) {
            return this.parent.resolve(name, true);
        } else {
            return Value.NONE;
        }
    }

    /**
     * Get default value for different builtin data types
     *
     * @param type The datatype
     * @return Object set to the default value for passed datatype, or null, if datatype is no
     *     builtin type
     */
    private Object getDefaultValue(IType type) {
        if (type == null) {
            return null;
        }
        var typeName = type.getName();
        if (typeName.equals(BuiltInType.intType.getName())) {
            return 0;
        } else if (typeName.equals(BuiltInType.stringType.getName())) {
            return "";
        } else if (typeName.equals(BuiltInType.graphType.getName())) {
            return new graph.Graph<String>(null, null);
        } else {
            return null;
        }
    }
}
