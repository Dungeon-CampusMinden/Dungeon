package semanticanalysis.types;

import static semanticanalysis.types.TypeBuilder.convertToDSLName;

import core.utils.TriConsumer;
import runtime.AggregateValue;
import runtime.IMemorySpace;
import runtime.Value;
import semanticanalysis.FunctionSymbol;
import semanticanalysis.types.CallbackAdapter.ICallbackAdapter;
import semanticanalysis.types.CallbackAdapter.ICallbackAdapterBuilder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class TypeInstantiator {
    private HashMap<String, Object> context = new HashMap<>();
    private HashMap<Class<?>, ICallbackAdapterBuilder> callbackAdapterBuilders = new HashMap<>();

    public TypeInstantiator() {}

    public void addCallbackAdapterBuilder(Class<?> clazz, ICallbackAdapterBuilder builder) {
        callbackAdapterBuilders.put(clazz, builder);
    }

    public void setFieldToFunctionalInterface(Field field, Object objectWithField, ICallbackAdapter adapter) {
        var clazz = field.getType();
        field.setAccessible(true);
        try{
            if (Consumer.class.isAssignableFrom(clazz)) {
                field.set(objectWithField, (Consumer)adapter::call);
            } else if (TriConsumer.class.isAssignableFrom(clazz)) {
                field.set(objectWithField, (TriConsumer)adapter::call);
            } else if (Function.class.isAssignableFrom(clazz)) {
                field.set(objectWithField, (Function)adapter::call);
            }
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
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
                if (!field.isAnnotationPresent(DSLTypeMember.class)) {
                    throw new RuntimeException(
                            "Instantiating a record using the TypeInstantiator requires that all "
                                    + "record members must be marked with @DSLTypeMember. Otherwise, no constructor invocation is possible");
                } else {
                    var fieldAnnotation = field.getAnnotation(DSLTypeMember.class);
                    String fieldName =
                            fieldAnnotation.name().equals("")
                                    ? convertToDSLName(field.getName())
                                    : fieldAnnotation.name();

                    var fieldValue = ms.resolve(fieldName);

                    // if a certain value is not found in the memory space,
                    // the record cannot be instantiated -> early return
                    if (fieldValue == null || fieldValue == Value.NONE) {
                        throw new RuntimeException(
                                "The name of field "
                                        + field.getName()
                                        + " cannot be resolved in the supplied memory space");
                    } else {
                        var internalValue = fieldValue.getInternalValue();

                        if (fieldValue.getDataType().getTypeKind().equals(IType.Kind.PODAdapted)) {
                            // call builder -> the type instantiator needs a reference to the
                            // builder or to the
                            // builder methods
                            var adaptedType = (AdaptedType) fieldValue.getDataType();
                            var method = adaptedType.getBuilderMethod();

                            internalValue = method.invoke(null, internalValue);
                        }
                        parameters.add(internalValue);
                    }
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
                // TODO: handle function callback
                // TODO: this whole thing could potentially be done after the instantiation itself
                String fieldName = TypeBuilder.getDSLFieldName(field);
                var fieldValue = ms.resolve(fieldName);
                if (field.isAnnotationPresent(DSLTypeMember.class)) {
                    // we only should set the field value explicitly,
                    // if it was set in the program (indicated by the dirty-flag)
                    if (fieldValue != Value.NONE && fieldValue.isDirty()) {
                        var internalValue = fieldValue.getInternalValue();

                        var fieldsDataType = fieldValue.getDataType();
                        if (fieldsDataType.getTypeKind().equals(IType.Kind.PODAdapted)) {
                            // call builder -> the type instantiator needs a reference to the
                            // builder or to the
                            // builder methods
                            var adaptedType = (AdaptedType) fieldValue.getDataType();
                            var method = adaptedType.getBuilderMethod();

                            internalValue = method.invoke(null, internalValue);
                        } else if (fieldsDataType
                                .getTypeKind()
                                .equals(IType.Kind.AggregateAdapted)) {
                            // call builder -> store values from memory space in order of parameters
                            // of builder-method
                            var adaptedType = (AggregateTypeAdapter) fieldsDataType;
                            var method = adaptedType.getBuilderMethod();
                            var aggregateFieldValue = (AggregateValue) fieldValue;

                            var parameters = new ArrayList<>(method.getParameterCount());
                            for (var parameter : method.getParameters()) {
                                var memberName = TypeBuilder.getDSLParameterName(parameter);
                                var memberValue =
                                        aggregateFieldValue.getMemorySpace().resolve(memberName);
                                var internalObject = memberValue.getInternalValue();
                                parameters.add(internalObject);
                            }

                            internalValue = method.invoke(null, parameters.toArray());
                        }

                        field.setAccessible(true);
                        field.set(instance, internalValue);
                    }
                }
                if (field.isAnnotationPresent(DSLCallback.class)) {
                    // Doing this here requires to much information to be passed to the typeInstantiator... rather set
                    // the internal value already correctly and just set the value here
                    // TODO: get IFunctionTypeBuilder for specific interface
                    var fieldsClass = field.getType();
                    var builder = this.callbackAdapterBuilders.get(fieldsClass);
                    if (builder == null) {
                        throw new RuntimeException("Can't find callback builder for functional interface "+fieldsClass);
                    }

                    assert fieldValue.getDataType().getTypeKind() == IType.Kind.FunctionType;
                    assert fieldValue.getInternalValue() instanceof FunctionSymbol;

                    ICallbackAdapter adapter = builder.buildAdapter((FunctionSymbol) fieldValue.getInternalValue());
                    setFieldToFunctionalInterface(field, instance, adapter);
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
