package runtime;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import semanticAnalysis.Scope;
import semanticAnalysis.Symbol;
import semanticAnalysis.types.AggregateType;
import semanticAnalysis.types.IType;

// TODO: make this a subclass of AggregateType
public class Prototype extends Value implements IType {
    public static Prototype NONE = new Prototype(new AggregateType("NO_TYPE_NAME", Scope.NULL));
    private final HashMap<String, Value> defaultValues;

    private AggregateType internalType() {
        return (AggregateType) this.dataType;
    }

    public Prototype(AggregateType internalType) {
        super(internalType, Value.NONE);
        defaultValues = new HashMap<>();
    }

    public boolean addDefaultValue(String name, Value value) {
        if (internalType().resolve(name) == Symbol.NULL) {
            return false;
        } else {
            // TODO: typecheck?
            defaultValues.put(name, value);
            return true;
        }
    }

    public Value getDefaultValue(String name) {
        return defaultValues.getOrDefault(name, Value.NONE);
    }

    public Set<Map.Entry<String, Value>> getDefaultValues() {
        return this.defaultValues.entrySet();
    }

    @Override
    public String getName() {
        return dataType.getName();
    }

    @Override
    public Kind getTypeKind() {
        return dataType.getTypeKind();
    }
}
