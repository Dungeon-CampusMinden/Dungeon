package runtime;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MemorySpace implements IMemorySpace {
    public static MemorySpace NONE;
    private final HashMap<String, Value> values = new HashMap<>();
    private final IMemorySpace parent;

    /**
     * Constructor
     *
     * @param parent parent MemorySpace
     */
    public MemorySpace(IMemorySpace parent) {
        this.parent = parent;
    }

    /** Constructor, parent will be set to NONE */
    public MemorySpace() {
        this.parent = NONE;
    }

    /**
     * Returns all stored Values
     *
     * @return A Set containing all entries from the value Map
     */
    public Set<Map.Entry<String, Value>> getAllValues() {
        return values.entrySet();
    }

    @Override
    public boolean bindValue(String name, Value value) {
        if (value.equals(Value.NONE)) {
            return false;
        }
        if (values.containsKey(name)) {
            return false;
        } else {
            values.put(name, value);
            return true;
        }
    }

    /**
     * Lookup passed name and return the stored {@link Value}, if one exists. If no Value was found
     * in own stored values, resolve it in the parent {@link MemorySpace}
     *
     * @param name The name to resolve
     * @return The resolved {@link Value} or Value.NONE, if the name could not be resolved
     */
    @Override
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
    @Override
    public Value resolve(String name, boolean resolveInParent) {
        if (this.values.containsKey(name)) {
            return this.values.get(name);
        } else if (this.parent != NONE && resolveInParent) {
            return this.parent.resolve(name, true);
        } else {
            return Value.NONE;
        }
    }

    @Override
    public boolean setValue(String name, Value value) {
        var resolved = resolve(name, false);
        if (resolved.equals(Value.NONE)) {
            return false;
        }
        resolved.setInternalValue(value.getInternalObject());
        return true;
    }

    @Override
    public Set<Map.Entry<String, Value>> getValueSet() {
        return this.values.entrySet();
    }
}
