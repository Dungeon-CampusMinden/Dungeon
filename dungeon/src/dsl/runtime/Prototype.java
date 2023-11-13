package dsl.runtime;

import dsl.semanticanalysis.Scope;
import dsl.semanticanalysis.Symbol;
import dsl.semanticanalysis.types.AggregateType;
import dsl.semanticanalysis.types.BuiltInType;
import dsl.semanticanalysis.types.IType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** A Prototype stores default values for some AggregateValue. */
public class Prototype extends Value implements IType {
    public static BuiltInType PROTOTYPE = new BuiltInType("prototype", Scope.NULL, (v) -> false);
    public static BuiltInType ITEM_PROTOTYPE =
            new BuiltInType("item_prototype", Scope.NULL, (v) -> false);
    public static Prototype NONE =
            new Prototype(PROTOTYPE, new AggregateType("NO_TYPE_NAME", Scope.NULL));
    private final AggregateType internalType;

    private final HashMap<String, Value> defaultValues;

    /**
     * Constructor
     *
     * @param prototypeType the {@link BuiltInType} of this Prototype (either {@link
     *     Prototype#ITEM_PROTOTYPE} or {@link Prototype#PROTOTYPE}
     */
    public Prototype(BuiltInType prototypeType, AggregateType internalType) {
        // an Prototype is a value and a type at the same time
        super(prototypeType, Value.NONE);
        assert prototypeType == PROTOTYPE || prototypeType == ITEM_PROTOTYPE;
        this.internalType = internalType;
        defaultValues = new HashMap<>();
    }

    public AggregateType getInternalType() {
        return internalType;
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
}
