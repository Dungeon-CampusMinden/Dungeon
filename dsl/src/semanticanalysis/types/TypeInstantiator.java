package semanticanalysis.types;

import interpreter.DSLInterpreter;

import runtime.*;

import semanticanalysis.FunctionSymbol;
import semanticanalysis.types.callbackadapter.CallbackAdapter;
import semanticanalysis.types.callbackadapter.CallbackAdapterBuilder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TypeInstantiator {
    private final HashMap<String, Object> context = new HashMap<>();
    private final CallbackAdapterBuilder callbackAdapterBuilder;

    public TypeInstantiator(DSLInterpreter interpreter) {
        callbackAdapterBuilder = new CallbackAdapterBuilder(interpreter);
    }

    /**
     * Instantiate a new Object corresponding to an {@link AggregateType} with an {@link
     * IMemorySpace} containing all needed values. This requires the passed {@link AggregateType} to
     * have an origin java class
     *
     * @param type the type to instantiate
     * @param ms the memory space containing the values
     * @return the instantiated object
     */
    public Object instantiate(AggregateType type, IMemorySpace ms) {
        var originalJavaClass = type.getOriginType();
        if (null == originalJavaClass) {
            return null;
        }

        if (originalJavaClass.isRecord()) {
            return instantiateRecord(originalJavaClass, ms);
        } else {
            return instantiateClass(originalJavaClass, ms);
        }
    }

    /**
     * Instantiate a {@link List} instance from a {@link ListValue}. Convert every entry
     * of the {@link ListValue} into an Object.
     *
     * @param listValue The ListValue to convert
     * @return the converted List
     */
    public List<?> instantiateList(ListValue listValue) {
        ArrayList arrayListInstance = new ArrayList<>();
        for (Value entryValue : (ArrayList<Value>) listValue.getInternalValue()) {
            var convertedEntryValue = convertValueToObject(entryValue);
            arrayListInstance.add(convertedEntryValue);
        }
        return arrayListInstance;
    }

    /**
     * Instantiate a {@link Set} instance from a {@link SetValue}. Convert every entry
     * of the {@link SetValue} into an Object.
     *
     * @param setValue The SetValue to convert
     * @return the converted Set
     */
    public Set<?> instantiateSet(SetValue setValue) {
        HashSet hashSetInstance = new HashSet<>();
        for (Value entryValue : (HashSet<Value>) setValue.getInternalValue()) {
            var convertedEntryValue = convertValueToObject(entryValue);
            hashSetInstance.add(convertedEntryValue);
        }
        return hashSetInstance;
    }

    /**
     * Push an object as part of the context (so it can be looked up, if it is referenced by {@link
     * DSLContextMember} by a constructor parameter)
     *
     * @param name the name to use for the contextMember
     * @param contextMember the Object to push
     */
    public void pushContextMember(String name, Object contextMember) {
        context.put(name, contextMember);
    }

    /**
     * Remove a context member with name
     *
     * @param name the name of the context member to remove
     */
    public void removeContextMember(String name) {
        context.remove(name);
    }

    /**
     * Converts a {@link Value} to a regular Java Object. The conversion is
     * dependent on the kind of datatype of the {@link Value} instance.
     *
     * @param value the Value to convert
     * @return the converted Object
     */
    private Object convertValueToObject(Value value) {
        Object convertedObject = value.getInternalValue();
        try {
            var fieldsDataType = value.getDataType();
            if (fieldsDataType.getTypeKind().equals(IType.Kind.PODAdapted)) {
                // call builder -> the type instantiator needs a reference to the
                // builder or to the
                // builder methods
                var adaptedType = (AdaptedType) value.getDataType();
                var method = adaptedType.getBuilderMethod();

                convertedObject = method.invoke(null, convertedObject);
            } else if (fieldsDataType.getTypeKind().equals(IType.Kind.AggregateAdapted)) {
                // call builder -> store values from memory space in order of parameters
                // of builder-method
                var adaptedType = (AggregateTypeAdapter) fieldsDataType;
                var method = adaptedType.getBuilderMethod();
                var aggregateFieldValue = (AggregateValue) value;

                var parameters = new ArrayList<>(method.getParameterCount());
                for (var parameter : method.getParameters()) {
                    var memberName = TypeBuilder.getDSLParameterName(parameter);
                    var memberValue = aggregateFieldValue.getMemorySpace().resolve(memberName);
                    var internalObject = memberValue.getInternalValue();
                    parameters.add(internalObject);
                }

                convertedObject = method.invoke(null, parameters.toArray());
            } else if (value.getDataType().getTypeKind().equals(IType.Kind.ListType)) {
                convertedObject = instantiateList((ListValue) value);
            } else if (value.getDataType().getTypeKind().equals(IType.Kind.SetType)) {
                convertedObject = instantiateSet((SetValue) value);
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return convertedObject;
    }

    private Object instantiateRecord(Class<?> originalJavaClass, IMemorySpace ms) {

        Constructor<?> ctor = getConstructor(originalJavaClass);
        if (null == ctor) {
            throw new RuntimeException(
                    "Could not find a suitable constructor to instantiate record "
                            + originalJavaClass.getName());
        }

        try {
            // find the corresponding record-field to the constructor-parameter, get the according
            // value from the memory space and pass it as a parameter to the constructor
            ArrayList<Object> parameters = new ArrayList<>(ctor.getParameters().length);
            for (var param : ctor.getParameters()) {
                var field = originalJavaClass.getDeclaredField(param.getName());
                if (field.isAnnotationPresent(DSLTypeMember.class)) {
                    String fieldName = TypeBuilder.getDSLFieldName(field);
                    var fieldValue = ms.resolve(fieldName);

                    // if a certain value is not found in the memory space,
                    // the record cannot be instantiated -> early return
                    if (fieldValue == null || fieldValue == Value.NONE) {
                        throw new RuntimeException(
                                "The name of field "
                                        + field.getName()
                                        + " cannot be resolved in the supplied memory space");
                    } else {
                        Object internalValue = convertValueToObject(fieldValue);
                        parameters.add(internalValue);
                    }
                } else if (field.isAnnotationPresent(DSLCallback.class)) {
                    String fieldName = TypeBuilder.getDSLFieldName(field);
                    var fieldValue = ms.resolve(fieldName);

                    assert fieldValue.getDataType().getTypeKind() == IType.Kind.FunctionType;
                    assert fieldValue.getInternalValue() instanceof FunctionSymbol;

                    CallbackAdapter adapter =
                            callbackAdapterBuilder.buildAdapter(
                                    (FunctionSymbol) fieldValue.getInternalValue());
                    parameters.add(adapter);
                } else {
                    throw new RuntimeException(
                            "Instantiating a record using the TypeInstantiator requires that all "
                                    + "record members must be marked with @DSLTypeMember. Otherwise, no constructor invocation is possible");
                }
            }
            ctor.setAccessible(true);
            return ctor.newInstance(parameters.toArray());
        } catch (NoSuchFieldException
                | InvocationTargetException
                | InstantiationException
                | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private Object instantiateClass(Class<?> originalJavaClass, IMemorySpace ms) {
        if (originalJavaClass.isMemberClass()) {
            throw new RuntimeException("Cannot instantiate an inner class");
        }

        Constructor<?> ctor = getConstructor(originalJavaClass);
        if (null == ctor) {
            throw new RuntimeException(
                    "Could not find a suitable constructor to instantiate class "
                            + originalJavaClass.getName());
        }

        Object instance;
        try {
            // get constructor
            ctor.setAccessible(true);
            ArrayList<Object> parameterValues = new ArrayList<>(ctor.getParameterCount());
            for (var param : ctor.getParameters()) {
                if (param.isAnnotationPresent(DSLContextMember.class)) {
                    String contextMemberName = param.getAnnotation(DSLContextMember.class).name();
                    Object contextMember = context.get(contextMemberName);
                    parameterValues.add(contextMember);
                } else {
                    throw new RuntimeException(
                            "Constructor parameter with name "
                                    + param.getName()
                                    + " is not marked as context parameter, cannot "
                                    + "instantiate class "
                                    + originalJavaClass.getName());
                }
            }

            instance = ctor.newInstance(parameterValues.toArray());

            // set values of the fields marked as DSLTypeMembers to corresponding values from
            // the memory space
            for (Field field : originalJavaClass.getDeclaredFields()) {
                String fieldName = TypeBuilder.getDSLFieldName(field);
                var fieldValue = ms.resolve(fieldName);
                if (field.isAnnotationPresent(DSLTypeMember.class)) {
                    // we only should set the field value explicitly,
                    // if it was set in the program (indicated by the dirty-flag)
                    if (fieldValue != Value.NONE && fieldValue.isDirty()) {
                        Object internalValue = convertValueToObject(fieldValue);

                        field.setAccessible(true);
                        field.set(instance, internalValue);
                    }
                }
                if (field.isAnnotationPresent(DSLCallback.class)) {
                    assert fieldValue.getDataType().getTypeKind() == IType.Kind.FunctionType;
                    assert fieldValue.getInternalValue() instanceof FunctionSymbol;

                    CallbackAdapter adapter =
                            callbackAdapterBuilder.buildAdapter(
                                    (FunctionSymbol) fieldValue.getInternalValue());
                    field.setAccessible(true);
                    field.set(instance, adapter);
                }
            }
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return instance;
    }

    private Constructor<?> getConstructor(Class<?> originalJavaClass) {
        Constructor<?> ctor = null;
        for (Constructor<?> constructor : originalJavaClass.getDeclaredConstructors()) {
            ctor = constructor;
            boolean unmarkedCtorParameter = false;
            for (var parameter : ctor.getParameters()) {
                if (!parameter.isAnnotationPresent(DSLContextMember.class)) {
                    unmarkedCtorParameter = true;
                    break;
                }
            }

            if (!unmarkedCtorParameter) {
                break;
            }
        }

        return ctor;
    }
}
