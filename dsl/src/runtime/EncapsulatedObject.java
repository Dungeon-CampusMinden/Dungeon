package runtime;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import semanticAnalysis.types.AggregateType;
import semanticAnalysis.types.IType;
import semanticAnalysis.types.TypeBuilder;

public class EncapsulatedObject extends Value implements IMemorySpace {
    private IMemorySpace parent;
    private AggregateType type;
    private HashMap<String, Field> typeMemberToField;

    // TODO: should probably abstract all that away in a TypeFactory, which
    //  handles creation of encapsulated objects and other stuff
    private IEvironment environment;
    private HashMap<String, EncapsulatedObject> objectCache;

    /**
     * Constructor
     *
     * @param innerObject the object to encapsulate
     * @param type {@link AggregateType} of the Value represented by the new EncapsulatedObject
     *     (used for resolving member access)
     * @param parent the parent {@link IMemorySpace}
     * @param environment the environment, which is used to resolve type names
     */
    public EncapsulatedObject(
            Object innerObject, AggregateType type, IMemorySpace parent, IEvironment environment) {
        super(type, innerObject);
        assert innerObject.getClass().equals(type.getOriginType());

        this.parent = parent;
        this.type = type;
        this.typeMemberToField = new HashMap<>();
        this.objectCache = new HashMap<>();

        buildFieldMap(innerObject.getClass(), type);
    }

    private void buildFieldMap(Class<?> clazz, AggregateType type) {
        var nameMap = TypeBuilder.typeMemberNameToJavaFieldMap(clazz);

        for (var member : type.getSymbols()) {
            var fieldName = nameMap.get(member.getName());
            try {
                Field field = clazz.getDeclaredField(fieldName);
                typeMemberToField.put(member.getName(), field);
            } catch (NoSuchFieldException e) {
                // TODO: handle
            }
        }
    }

    @Override
    public boolean bindValue(String name, Value value) {
        return false;
    }

    @Override
    public Value resolve(String name) {
        if (objectCache.containsKey(name)) {
            return objectCache.get(name);
        }

        // lookup name
        Field correspondingField = this.typeMemberToField.getOrDefault(name, null);
        if (correspondingField == null) {
            return Value.NONE;
        } else {
            // read field value
            correspondingField.setAccessible(true);
            try {
                var value = correspondingField.get(this.getInternalObject());
                // convert the read field value to a DSL 'Value'
                // this may require recursive creation of encapsulated objects,
                // if the field is a component for example
                var type = TypeBuilder.getDSLTypeForClass(value.getClass());
                if (type != null) {
                    // TODO: create encapsulated value (because the field is a POD-field, or "basic
                    // type")

                } else {
                    var dslTypeName = TypeBuilder.getDSLName(value.getClass());
                    var typeFromGlobalScope =
                            this.environment.getGlobalScope().resolve(dslTypeName);
                    if (typeFromGlobalScope instanceof IType) {
                        // TODO: test thins
                        type = (IType) typeFromGlobalScope;
                        assert type instanceof AggregateType;
                        // if we reach this point, then the field in the actual java class
                        // has a representation in the dsl type system, which means
                        // that we should be able to just construct a new EncapsulatedObject
                        // around it -> which should be cached;
                        var encapsulatedObject =
                                new EncapsulatedObject(
                                        value, (AggregateType) type, this, this.environment);
                        // cache it
                        this.objectCache.put(name, encapsulatedObject);

                        return encapsulatedObject;
                    }
                }
            } catch (IllegalAccessException e) {
                // TODO: handle
            }
        }
        return Value.NONE;
    }

    @Override
    public Value resolve(String name, boolean resolveInParent) {
        return null;
    }

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
                correspondingField.set(this.getInternalObject(), value.getInternalObject());
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
