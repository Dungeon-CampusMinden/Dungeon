package runtime;

import semanticanalysis.Scope;
import semanticanalysis.Symbol;
import semanticanalysis.types.AggregateType;
import semanticanalysis.types.BuiltInType;
import semanticanalysis.types.IType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

// TODO: make this a subclass of AggregateType
// TODO: add class description
public class EntityType extends Value implements IType {
    public static EntityType NONE = new EntityType(new AggregateType("NO_TYPE_NAME", Scope.NULL));
    public static BuiltInType ENTITY_TYPE = new BuiltInType("entity_type", Scope.NULL);
    private final HashMap<String, Value> defaultValues;

    private final AggregateType internalType;

    public AggregateType getInternalType() {
        return internalType;
    }

    /*private AggregateType internalType() {
        return this.internalType;
    }*/

    /**
     * Constructor
     *
     * @param internalType the {@link AggregateType} of which this is a Prototype
     */
    public EntityType(AggregateType internalType) {
        // an EntityType is a value and a type at the same time
        super(ENTITY_TYPE, Value.NONE);
        this.internalType = internalType;
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
        if (getInternalType().resolve(name) == Symbol.NULL) {
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
        return internalType.getName();
    }

    /**
     * @return the {@link IType.Kind} of the internal datatype
     */
    @Override
    public Kind getTypeKind() {
        return internalType.getTypeKind();
    }
}
