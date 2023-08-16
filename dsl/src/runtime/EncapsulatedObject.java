package runtime;

import semanticanalysis.PropertySymbol;
import semanticanalysis.Symbol;
import semanticanalysis.types.AggregateType;
import semanticanalysis.types.BuiltInType;
import semanticanalysis.types.IDSLTypeProperty;
import semanticanalysis.types.TypeBuilder;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class EncapsulatedObject extends Value implements IMemorySpace {
    private IMemorySpace parent;
    private AggregateType type;

    // TODO: this should be static for one aggregateType and not instanced for each new instance of
    //  the same type;
    //  this change is non-trivial, as it requires the central storage of the
    //  typeMember-to-class-field information PER DATATYPE
    private HashMap<String, Field> typeMemberToField;

    // TODO: should probably abstract all that away in a TypeFactory, which
    //  handles creation of encapsulated objects and other stuff
    private IEvironment environment;
    private HashMap<String, Value> objectCache;

    /**
     * Constructor
     *
     * @param innerObject the object to encapsulate
     * @param type {@link AggregateType} of the Value represented by the new EncapsulatedObject
     *     (used for resolving member access)
     */
    public EncapsulatedObject(Object innerObject, AggregateType type, IEvironment environment) {
        super(type, innerObject);

        this.type = type;
        this.environment = environment;
        this.typeMemberToField = new HashMap<>();
        this.objectCache = new HashMap<>();

        buildFieldMap(innerObject.getClass(), type);
    }

    private void buildFieldMap(Class<?> clazz, AggregateType type) {
        var nameMap = TypeBuilder.typeMemberNameToJavaFieldMap(clazz);

        for (var member : type.getSymbols()) {
            var fieldName = nameMap.get(member.getName());
            if (fieldName != null) {
                try {
                    Field field = clazz.getDeclaredField(fieldName);
                    typeMemberToField.put(member.getName(), field);
                } catch (NoSuchFieldException e) {
                    // TODO: handle
                }
            }
        }
    }

    @Override
    public boolean bindValue(String name, Value value) {
        return false;
    }

    @Override
    public Value resolve(String name) {
        Value returnValue = Value.NONE;
        if (objectCache.containsKey(name)) {
            return objectCache.get(name);
        }

        // lookup name
        Field correspondingField = this.typeMemberToField.getOrDefault(name, null);
        if (correspondingField != null) {
            // read field value
            correspondingField.setAccessible(true);
            try {
                var fieldValue = correspondingField.get(this.getInternalValue());
                // convert the read field value to a DSL 'Value'
                // this may require recursive creation of encapsulated objects,
                // if the field is a component for example
                var type = this.environment.getDSLTypeForClass(fieldValue.getClass());
                if (type != BuiltInType.noType) {
                    switch (type.getTypeKind()) {
                        case Basic:
                            // create encapsulated value (because the field is a POD-field, or
                            // "basic type") -> linking the value to the field is only required
                            // for setting the internal value
                            // NOTE: this behaviour differs from the default translation of the
                            // RuntimeObjectTranslator, because we know in this case, that the
                            // resolved name is a member of the underlying object
                            returnValue =
                                    new EncapsulatedField(type, correspondingField, this.object);
                            break;
                        case PODAdapted:
                        case AggregateAdapted:
                        case Aggregate:
                            returnValue =
                                    environment
                                            .getRuntimeObjectTranslator()
                                            .translateRuntimeObject(
                                                    fieldValue, this, this.environment);
                            break;
                        case FunctionType:
                            break;
                    }
                    // cache it
                    this.objectCache.put(name, returnValue);
                }
            } catch (IllegalAccessException e) {
                // TODO: handle
            }
        } else {
            // it may be a property
            Symbol symbol = type.resolve(name);
            if (symbol instanceof PropertySymbol propertySymbol) {
                returnValue =
                    new PropertyValue(
                        symbol.getDataType(),
                        (IDSLTypeProperty<Object, Object>) propertySymbol.getProperty(),
                        this.object);
            }
        }
        return returnValue;
    }

    @Override
    public Value resolve(String name, boolean resolveInParent) {
        return resolve(name);
    }

    // TODO: define the semantics for this based on, if the value is a POD type or
    //  a complex type -> what happens, if we want to set a component of an
    //  entity or a complex datatype of a component (e.g. Point)?!
    //  (will be done in https://github.com/Programmiermethoden/Dungeon/issues/156)
    @Override
    public boolean setValue(String name, Value value) {
        Field correspondingField = this.typeMemberToField.getOrDefault(name, null);
        if (correspondingField == null) {
            return false;
        } else {
            // TODO: this should only be possible for PODs
            // read field value
            correspondingField.setAccessible(true);
            try {
                correspondingField.set(this.getInternalValue(), value.getInternalValue());
            } catch (IllegalAccessException e) {
                // TODO: handle
                return false;
            }
        }
        return false;
    }

    @Override
    public Set<Map.Entry<String, Value>> getValueSet() {
        // TODO
        return null;
    }
}
