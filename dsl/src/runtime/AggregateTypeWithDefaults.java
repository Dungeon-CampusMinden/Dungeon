package runtime;

import java.util.HashMap;
import semanticAnalysis.Scope;
import semanticAnalysis.Symbol;
import semanticAnalysis.types.AggregateType;
import semanticAnalysis.types.IType;

// TODO: rename this to "Prototype"?
public class AggregateTypeWithDefaults extends Value implements IType {
    public static AggregateTypeWithDefaults NONE =
            new AggregateTypeWithDefaults(new AggregateType("NO_TYPE_NAME", Scope.NULL), -1);
    private final HashMap<String, Value> defaultValues;

    private AggregateType internalType() {
        return (AggregateType) this.dataType;
    }

    public AggregateTypeWithDefaults(AggregateType internalType, int symbolIdx) {
        super(internalType, Value.NONE, symbolIdx);
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

    @Override
    public String getName() {
        return dataType.getName();
    }

    @Override
    public Kind getTypeKind() {
        return dataType.getTypeKind();
    }
}
