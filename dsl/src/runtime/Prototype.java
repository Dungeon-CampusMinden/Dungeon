package runtime;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import semanticAnalysis.Scope;
import semanticAnalysis.Symbol;
import semanticAnalysis.types.AggregateType;
import semanticAnalysis.types.IType;

// TODO: make this a subclass of AggregateType
// TODO: add class description
public class Prototype extends Value implements IType {
    public static Prototype NONE = new Prototype(new AggregateType("NO_TYPE_NAME", Scope.NULL));
    private final HashMap<String, Value> defaultValues;

    private AggregateType internalType() {
        return (AggregateType) this.dataType;
    }

    /**
     * Constructor
     *
     * @param internalType the {@link AggregateType} of which this is a Prototype
     */
    public Prototype(AggregateType internalType) {
        super(internalType, Value.NONE);
        defaultValues = new HashMap<>();
    }

    /**
     * Add a new default value to the Prototype
     *
     * @param name name of the new default value
     * @param value default value
     * @return true on success, false otherwise
     */
    public boolean addDefaultValue(String name, Value value) {
        if (internalType().resolve(name) == Symbol.NULL) {
            return false;
        } else {
            // TODO: typecheck?
            defaultValues.put(name, value);
            return true;
        }
    }

    /**
     * @param name the name of the default value to get
     * @return Value with name or Value.NONE
     */
    public Value getDefaultValue(String name) {
        return defaultValues.getOrDefault(name, Value.NONE);
    }

    /**
     * @return Set of all default values (as combination of name and value)
     */
    public Set<Map.Entry<String, Value>> getDefaultValues() {
        return this.defaultValues.entrySet();
    }

    /**
     * @return the name of the internal datatype
     */
    @Override
    public String getName() {
        return dataType.getName();
    }

    /**
     * @return the {@link IType.Kind} of the internal datatype
     */
    @Override
    public Kind getTypeKind() {
        return dataType.getTypeKind();
    }
}
