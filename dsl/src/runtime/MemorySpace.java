package runtime;

import java.util.HashMap;
import symboltable.BuiltInType;
import symboltable.IType;
import symboltable.Symbol;

// TODO: also use this for object-instantiation?
// TODO: does this need to be specialized for function memory space -> just try it
public class MemorySpace {
    public static MemorySpace NONE;
    private final HashMap<String, Value> values = new HashMap<>();
    private final MemorySpace parent;

    public MemorySpace(MemorySpace parent) {
        this.parent = parent;
    }

    public MemorySpace() {
        this.parent = NONE;
    }

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

    public Value resolve(String name) {
        return this.resolve(name, true);
    }

    public Value resolve(String name, boolean resolveInParent) {
        if (this.values.containsKey(name)) {
            return this.values.get(name);
        } else if (this.parent != NONE && resolveInParent) {
            return this.parent.resolve(name, true);
        } else {
            return Value.NONE;
        }
    }

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
