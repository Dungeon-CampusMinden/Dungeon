package semanticanalysis.types;

import core.utils.TriConsumer;

import dslToGame.graph.Graph;

import semanticanalysis.*;
import semanticanalysis.types.CallbackAdapter.ConsumerFunctionTypeBuilder;
import semanticanalysis.types.CallbackAdapter.FunctionFunctionTypeBuilder;
import semanticanalysis.types.CallbackAdapter.IFunctionTypeBuilder;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TypeBuilder {
    private final HashMap<Class<?>, Method> typeAdapters;
    private final HashMap<Type, IType> javaTypeToDSLType;
    private final HashSet<Class<?>> currentLookedUpClasses;
    private final HashMap<Class<?>, IFunctionTypeBuilder> functionTypeBuilders;

    /** Constructor */
    public TypeBuilder() {
        this.typeAdapters = new HashMap<>();
        this.javaTypeToDSLType = new HashMap<>();
        this.currentLookedUpClasses = new HashSet<>();
        this.functionTypeBuilders = new HashMap<>();

        setupFunctionTypeBuilders();
    }

    private void setupFunctionTypeBuilders() {
        functionTypeBuilders.put(Consumer.class, ConsumerFunctionTypeBuilder.instance);
        functionTypeBuilders.put(TriConsumer.class, ConsumerFunctionTypeBuilder.instance);
        functionTypeBuilders.put(Function.class, FunctionFunctionTypeBuilder.instance);
    }

    public HashMap<Type, IType> getJavaTypeToDSLTypeMap() {
        // create copy of the hashmap
        return new HashMap<>(javaTypeToDSLType);
    }

    public IFunctionTypeBuilder getFunctionTypeBuilder(Class<?> callbackClass) {
        // create copy of the hashmap
        return this.functionTypeBuilders.get(callbackClass);
    }

    /**
     * Replaces all small letters followed by a capital letter in small letters spaced by '_'
     *
     * @param name the name to convert
     * @return converted name
     */
    public static String convertToDSLName(String name) {
        Pattern pattern = Pattern.compile("([a-z0-9_])([A-Z])");
        Matcher matcher = pattern.matcher(name);
        var underscored = matcher.replaceAll(mr -> mr.group(1) + '_' + mr.group(2).toLowerCase());
        return underscored.toLowerCase();
    }

    /**
     * @param type the class to get the corresponding {@link IType} for
     * @return the corresponding {@link IType} for the passed type, or null, if the passed type does
     *     not correspond to a basic type
     */
    public static IType getBuiltInDSLType(Class<?> type) {
        // check for basic types
        if (int.class.equals(type)
                || short.class.equals(type)
                || long.class.equals(type)
                || Integer.class.isAssignableFrom(type)) {
            return BuiltInType.intType;
        } else if (float.class.equals(type)
                || double.class.equals(type)
                || Float.class.isAssignableFrom(type)) {
            return BuiltInType.floatType;
        } else if (boolean.class.equals(type) || Boolean.class.isAssignableFrom(type)) {
            return BuiltInType.boolType;
        } else if (String.class.equals(type) || String.class.isAssignableFrom(type)) {
            return BuiltInType.stringType;
        } else if (Graph.class.equals(type) || Graph.class.isAssignableFrom(type)) {
            return BuiltInType.graphType;
        }

        return null;
    }

    /**
     * Generate a map, which maps the member names of an DSL {@link IType} to the field names in the
     * origin java class
     *
     * @param clazz the origin java class
     * @return the map, containing mapping between member names and java field names
     */
    public static HashMap<String, String> typeMemberNameToJavaFieldMap(Class<?> clazz) {
        HashMap<String, String> map = new HashMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            // bind new Symbol
            if (field.isAnnotationPresent(DSLTypeMember.class)
                    || field.isAnnotationPresent(DSLCallback.class)) {
                String fieldName = getDSLFieldName(field);
                map.put(fieldName, field.getName());
            }
        }
        return map;
    }

    protected static String getDSLNameOfBasicType(Class<?> clazz) {
        var basicType = getBuiltInDSLType(clazz);
        return basicType != null ? basicType.getName() : "";
    }

    /**
     * @param clazz the Class to get the DSL name for
     * @return converted name (conversion by {@link #convertToDSLName(String)}) or the 'name'
     *     parameter of {@link DSLType}
     */
    public static String getDSLTypeName(Class<?> clazz) {
        // check for basic type
        String dslName = getDSLNameOfBasicType(clazz);
        if (dslName.isEmpty()) {
            var classAnnotation = clazz.getAnnotation(DSLType.class);
            return classAnnotation == null || classAnnotation.name().equals("")
                    ? convertToDSLName(clazz.getSimpleName())
                    : classAnnotation.name();
        } else {
            return dslName;
        }
    }

    /**
     * @param field the field to get the DSL name for
     * @return converted name (conversion by {@link #convertToDSLName(String)}) or the 'name'
     *     parameter of {@link DSLTypeMember}
     */
    public static String getDSLFieldName(Field field) {
        var fieldAnnotation = field.getAnnotation(DSLTypeMember.class);
        return fieldAnnotation == null || fieldAnnotation.name().equals("")
                ? convertToDSLName(field.getName())
                : fieldAnnotation.name();
    }

    /**
     * @param parameter the parameter to get the DSL name for
     * @return converted name (conversion by {@link #convertToDSLName(String)}) or the 'name'
     *     parameter of {@link DSLTypeMember}
     */
    public static String getDSLParameterName(Parameter parameter) {
        var parameterAnnotation = parameter.getAnnotation(DSLTypeMember.class);
        return parameterAnnotation == null || parameterAnnotation.name().equals("")
                ? convertToDSLName(parameter.getName())
                : parameterAnnotation.name();
    }

    /**
     * Register a new type adapter (which will be used to instantiate a class, which is not
     * converted to a DSLType)
     *
     * @param adapterClass the adapter to register
     */
    public boolean registerTypeAdapter(Class<?> adapterClass, IScope parentScope) {
        for (var method : adapterClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(DSLTypeAdapter.class)
                    && Modifier.isStatic(method.getModifiers())) {
                var forType = method.getReturnType();
                if (this.typeAdapters.containsKey(forType)) {
                    return false;
                }
                this.typeAdapters.put(forType, method);

                var annotation = method.getAnnotation(DSLTypeAdapter.class);
                String dslTypeName =
                        annotation.name().equals("")
                                ? convertToDSLName(forType.getSimpleName())
                                : annotation.name();

                // create adapterType
                var adapterType = createAdapterType(forType, dslTypeName, method, parentScope);

                this.javaTypeToDSLType.put(forType, adapterType);

                return true;
            }
        }
        return true;
    }

    public IType createAdapterType(
            Class<?> forType, String dslTypeName, Method adapterMethod, IScope parentScope) {
        // get parameters, if only one: PODType, otherwise: AggregateType
        if (adapterMethod.getParameterCount() == 0) {
            // TODO: handle
            throw new RuntimeException(
                    "Builder methods with zero arguments are currently not supported");
        }

        if (adapterMethod.getParameterCount() == 1) {
            var paramType = adapterMethod.getParameterTypes()[0];
            // TODO: how to handle non-builtIn types here?
            var paramDSLType = getBuiltInDSLType(paramType);
            return new AdaptedType(
                    dslTypeName, parentScope, forType, (BuiltInType) paramDSLType, adapterMethod);
        } else {
            var typeAdapter =
                    new AggregateTypeAdapter(dslTypeName, parentScope, forType, adapterMethod);
            // bind symbol for each parameter in the adapterMethod
            for (var parameter : adapterMethod.getParameters()) {
                String parameterName = getDSLParameterName(parameter);
                // TODO: how to handle non-builtin types here?
                IType paramDSLType = getBuiltInDSLType(parameter.getType());
                if (null == paramDSLType) {
                    currentLookedUpClasses.add(forType);
                    paramDSLType = createTypeFromClass(parentScope, forType);
                    currentLookedUpClasses.remove(forType);
                }
                Symbol parameterSymbol = new Symbol(parameterName, typeAdapter, paramDSLType);
                typeAdapter.bind(parameterSymbol);
            }
            return typeAdapter;
        }
    }

    public Set<Map.Entry<Class<?>, Method>> getRegisteredTypeAdapters() {
        return this.typeAdapters.entrySet();
    }

    public Method getRegisteredTypeAdapter(Class<?> clazz) {
        return this.typeAdapters.getOrDefault(clazz, null);
    }

    // create a symbol in parentType for given field, representing a callback
    protected Symbol createCallbackMemberSymbol(Field field, AggregateType parentType) {
        String callbackName = getDSLFieldName(field);

        IType callbackType = BuiltInType.noType;
        var fieldsClass = field.getType();
        var functionTypeBuilder = functionTypeBuilders.get(fieldsClass);
        if (functionTypeBuilder != null) {
            callbackType = functionTypeBuilder.buildFunctionType(field, this);
        }

        return new Symbol(callbackName, parentType, callbackType);
    }

    protected IType createSetType(ParameterizedType setType) {
        var elementType = setType.getActualTypeArguments()[0];
        IType elementDSLType = this.createTypeFromClass(Scope.NULL, (Class<?>) elementType);

        if (javaTypeToDSLType.get(setType) == null) {
            IType dslSetType = new SetType(elementDSLType, Scope.NULL);
            javaTypeToDSLType.put(setType, dslSetType);
        }
        return javaTypeToDSLType.get(setType);
    }

    protected IType createListType(ParameterizedType listType) {
        var elementType = listType.getActualTypeArguments()[0];
        IType elementDSLType = this.createTypeFromClass(Scope.NULL, (Class<?>) elementType);

        if (javaTypeToDSLType.get(listType) == null) {
            IType dslListType = new ListType(elementDSLType, Scope.NULL);
            javaTypeToDSLType.put(listType, dslListType);
        }
        return javaTypeToDSLType.get(listType);
    }

    // create a symbol in parentType for given field, representing data in parentClass
    protected Symbol createDataMemberSymbol(
            Field field, Class<?> parentClass, AggregateType parentType) {
        String fieldName = getDSLFieldName(field);

        Class<?> fieldsType = field.getType();

        // get datatype
        var memberDSLType = getBuiltInDSLType(fieldsType);
        if (memberDSLType == null) {
            // is list or set?
            if (List.class.isAssignableFrom(fieldsType)) {
                memberDSLType = createListType((ParameterizedType) field.getGenericType());
            } else if (Set.class.isAssignableFrom(fieldsType)) {
                memberDSLType = createSetType((ParameterizedType) field.getGenericType());
            }
        }
        if (memberDSLType == null) {
            // lookup the type in already converted types
            // if it is not already in the converted types, try to convert it -> check for
            // DSLType
            // annotation
            this.currentLookedUpClasses.add(parentClass);
            memberDSLType = createTypeFromClass(parentType, fieldsType);
            this.currentLookedUpClasses.remove(parentClass);
        }

        return new Symbol(fieldName, parentType, memberDSLType);
    }

    /**
     * Creates a DSL {@link AggregateType} from a java class. This requires the class to be marked
     * with the {@link DSLType} annotation. Each field marked with the {@link DSLTypeMember}
     * annotation will be converted to a member of the created {@link AggregateType}, if the field's
     * type can be mapped to a DSL data type. This requires the field's type to be either one of the
     * types declared in {@link BuiltInType} or another class marked with {@link DSLType}.
     *
     * @param parentScope the scope in which to create the new type
     * @param clazz the class to create a type for
     * @return a new {@link AggregateType}, if the passed Class could be converted to a DSL type;
     *     null otherwise
     */
    public IType createTypeFromClass(IScope parentScope, Class<?> clazz) {
        if (this.javaTypeToDSLType.containsKey(clazz)) {
            return this.javaTypeToDSLType.get(clazz);
        }

        var builtInType = getBuiltInDSLType(clazz);
        if (builtInType != null) {
            return builtInType;
        }

        if (!clazz.isAnnotationPresent(DSLType.class)) {
            return null;
        }

        // catch recursion
        if (this.currentLookedUpClasses.contains(clazz)) {
            throw new RuntimeException("RECURSIVE TYPE DEF");
        }

        String typeName = getDSLTypeName(clazz);

        var type = new AggregateType(typeName, parentScope, clazz);
        for (Field field : clazz.getDeclaredFields()) {
            // bind new Symbol
            if (field.isAnnotationPresent(DSLTypeMember.class)) {
                var fieldSymbol = createDataMemberSymbol(field, clazz, type);
                type.bind(fieldSymbol);
            }
            if (field.isAnnotationPresent(DSLCallback.class)) {
                var callbackSymbol = createCallbackMemberSymbol(field, type);
                type.bind(callbackSymbol);
            }
        }
        this.javaTypeToDSLType.put(clazz, type);
        return type;
    }
}
