package semanticanalysis.types;

import core.utils.TriConsumer;

import dslToGame.graph.Graph;

import runtime.nativefunctions.ExtensionMethod;

import semanticanalysis.*;
import semanticanalysis.types.callbackadapter.BiFunctionFunctionTypeBuilder;
import semanticanalysis.types.callbackadapter.ConsumerFunctionTypeBuilder;
import semanticanalysis.types.callbackadapter.FunctionFunctionTypeBuilder;
import semanticanalysis.types.callbackadapter.IFunctionTypeBuilder;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TypeBuilder {
    private final HashMap<Class<?>, List<Method>> typeAdapters;
    private final HashMap<Type, IType> javaTypeToDSLType;
    private final HashSet<Type> currentLookedUpTypes;
    private final HashMap<Class<?>, IFunctionTypeBuilder> functionTypeBuilders;

    /** Constructor */
    public TypeBuilder() {
        this.typeAdapters = new HashMap<>();
        this.javaTypeToDSLType = new HashMap<>();
        this.currentLookedUpTypes = new HashSet<>();
        this.functionTypeBuilders = new HashMap<>();

        setupFunctionTypeBuilders();
    }

    private void setupFunctionTypeBuilders() {
        functionTypeBuilders.put(Consumer.class, ConsumerFunctionTypeBuilder.instance);
        functionTypeBuilders.put(TriConsumer.class, ConsumerFunctionTypeBuilder.instance);
        functionTypeBuilders.put(Function.class, FunctionFunctionTypeBuilder.instance);
        functionTypeBuilders.put(BiFunction.class, BiFunctionFunctionTypeBuilder.instance);
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
    public static IType getBuiltInDSLType(Type type) {
        boolean canBeCastToClass = true;
        Class<?> clazz = null;
        try {
            clazz = (Class<?>) type;
        } catch (ClassCastException ex) {
            canBeCastToClass = false;
        }
        if (!canBeCastToClass) {
            return null;
        }
        // check for basic types
        if (int.class.equals(clazz)
                || short.class.equals(clazz)
                || long.class.equals(clazz)
                || Integer.class.isAssignableFrom(clazz)) {
            return BuiltInType.intType;
        } else if (float.class.equals(clazz)
                || double.class.equals(clazz)
                || Float.class.isAssignableFrom(clazz)) {
            return BuiltInType.floatType;
        } else if (boolean.class.equals(clazz) || Boolean.class.isAssignableFrom(clazz)) {
            return BuiltInType.boolType;
        } else if (String.class.equals(clazz) || String.class.isAssignableFrom(clazz)) {
            return BuiltInType.stringType;
        } else if (Graph.class.equals(clazz) || Graph.class.isAssignableFrom(clazz)) {
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

    public static boolean doParameterTypesMatch(Method m1, Method m2) {
        // check, if registered adapter matches signature of new adapter
        if (m1.getParameterCount() != m2.getParameterCount()) {
            return false;
        }
        boolean parametersMatch = true;
        for (int i = 0; parametersMatch && i < m1.getParameterCount(); i++) {
            Class<?> m1Parameter = m1.getParameterTypes()[i];
            Class<?> m2Parameter = m2.getParameterTypes()[i];
            parametersMatch = m2Parameter.equals(m1Parameter);
        }
        return parametersMatch;
    }

    /**
     * Register a new type adapter (which will be used to instantiate a class, which is not
     * converted to a DSLType)
     *
     * @param adapterClass the adapter to register
     */
    public void registerTypeAdapter(Class<?> adapterClass, IScope parentScope) {
        for (var method : adapterClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(DSLTypeAdapter.class)
                    && Modifier.isStatic(method.getModifiers())) {
                DSLTypeAdapter annotation = method.getAnnotation(DSLTypeAdapter.class);

                var forType = method.getReturnType();
                if (!this.typeAdapters.containsKey(forType)) {
                    this.typeAdapters.put(forType, new ArrayList<>());
                }

                List<Method> typeAdaptersForType = this.typeAdapters.get(forType);
                for (Method adapter : typeAdaptersForType) {
                    if (doParameterTypesMatch(adapter, method)) {
                        throw new UnsupportedOperationException(
                                "An adapter for class "
                                        + forType.getName()
                                        + " with the same signature was already registered");
                    }
                }

                this.typeAdapters.get(forType).add(method);

                String dslTypeName =
                        annotation.name().equals("")
                                ? convertToDSLName(forType.getSimpleName())
                                : annotation.name();

                // create adapterType
                var adapterType = createAdapterType(forType, dslTypeName, method, parentScope);

                this.javaTypeToDSLType.put(forType, adapterType);
                parentScope.bind((Symbol) adapterType);
                return;
            }
        }
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
            IType paramDSLType = createDSLTypeForJavaTypeInScope(parentScope, paramType);
            return new AdaptedType(
                    dslTypeName, parentScope, forType, (BuiltInType) paramDSLType, adapterMethod);
        } else {
            var typeAdapter =
                    new AggregateTypeAdapter(dslTypeName, parentScope, forType, adapterMethod);
            // bind symbol for each parameter in the adapterMethod
            for (var parameter : adapterMethod.getParameters()) {
                String parameterName = getDSLParameterName(parameter);
                Type parametersType = parameter.getType();

                IType paramDSLType = createDSLTypeForJavaTypeInScope(parentScope, parametersType);
                if (paramDSLType == null) {
                    // TODO: refactor this to be included in createDSLTypeForJavaTypeInScope, see:
                    //  https://github.com/Programmiermethoden/Dungeon/issues/917

                    var parametersAnnotatedType = parameter.getAnnotatedType();
                    // if the cast fails, the type may be a parameterized type (e.g. list or set)
                    if (List.class.isAssignableFrom((Class<?>) parametersType)) {
                        paramDSLType =
                                createListType(
                                        (ParameterizedType) parametersAnnotatedType.getType(),
                                        parentScope);
                    } else if (Set.class.isAssignableFrom((Class<?>) parametersType)) {
                        paramDSLType =
                                createSetType(
                                        (ParameterizedType) parametersAnnotatedType.getType(),
                                        parentScope);
                    }
                }

                Symbol parameterSymbol = new Symbol(parameterName, typeAdapter, paramDSLType);
                typeAdapter.bind(parameterSymbol);
            }
            return typeAdapter;
        }
    }

    public Set<Map.Entry<Class<?>, List<Method>>> getRegisteredTypeAdapters() {
        return this.typeAdapters.entrySet();
    }

    public List<Method> getRegisteredTypeAdaptersForType(Class<?> clazz) {
        return this.typeAdapters.getOrDefault(clazz, new ArrayList<>());
    }

    protected IType bindOrResolveTypeInScope(IType type, IScope scope) {
        IType returnedType = type;
        Symbol resolvedType = scope.resolve(type.getName());
        if (resolvedType != Symbol.NULL) {
            if (resolvedType instanceof IType) {
                returnedType = (IType) resolvedType;
            } else {
                // symbol with the name of the function type is already bound in
                // global scope but not a type
                throw new RuntimeException(
                        "A symbol with the name "
                                + type.getName()
                                + " is already bound in the global scope but is not a type");
            }
        } else {
            // bind newly created function type in the global scope
            scope.bind((Symbol) type);
        }
        return returnedType;
    }

    // create a symbol in parentType for given field, representing a callback
    protected Symbol createCallbackMemberSymbol(
            Field field, AggregateType parentType, IScope globalScope) {
        String callbackName = getDSLFieldName(field);

        IType callbackType = BuiltInType.noType;
        var fieldsClass = field.getType();
        var functionTypeBuilder = functionTypeBuilders.get(fieldsClass);

        if (functionTypeBuilder != null) {
            callbackType = functionTypeBuilder.buildFunctionType(field, this, globalScope);
            callbackType = bindOrResolveTypeInScope(callbackType, globalScope);
        }

        return new Symbol(callbackName, parentType, callbackType);
    }

    /**
     * Create a new {@link SetType} from the passed {@link ParameterizedType}.
     *
     * @param setType the {@link ParameterizedType} to convert into a {@link SetType}
     * @return the created type
     */
    public IType createSetType(ParameterizedType setType, IScope globalScope) {
        var elementType = setType.getActualTypeArguments()[0];
        IType elementDSLType = this.createDSLTypeForJavaTypeInScope(globalScope, elementType);

        if (javaTypeToDSLType.get(setType) == null) {
            IType dslSetType = new SetType(elementDSLType, globalScope);
            dslSetType = bindOrResolveTypeInScope(dslSetType, globalScope);
            javaTypeToDSLType.put(setType, dslSetType);
        }
        return javaTypeToDSLType.get(setType);
    }

    /**
     * Create a new {@link ListType} from the passed {@link ParameterizedType}.
     *
     * @param listType the {@link ParameterizedType} to convert into a {@link ListType}
     * @return the created type
     */
    public IType createListType(ParameterizedType listType, IScope globalScope) {
        var elementType = listType.getActualTypeArguments()[0];
        IType elementDSLType = this.createDSLTypeForJavaTypeInScope(globalScope, elementType);

        if (javaTypeToDSLType.get(listType) == null) {
            IType dslListType = new ListType(elementDSLType, globalScope);
            dslListType = bindOrResolveTypeInScope(dslListType, globalScope);
            javaTypeToDSLType.put(listType, dslListType);
        }
        return javaTypeToDSLType.get(listType);
    }

    // create a symbol in parentType for given field, representing data in parentClass
    protected Symbol createDataMemberSymbol(
            Field field, Class<?> parentClass, AggregateType parentType, IScope globalScope) {
        String fieldName = getDSLFieldName(field);

        Class<?> fieldsType = field.getType();

        // get datatype
        var memberDSLType = getBuiltInDSLType(fieldsType);
        if (memberDSLType == null) {
            // is list or set?
            if (List.class.isAssignableFrom(fieldsType)) {
                memberDSLType =
                        createListType((ParameterizedType) field.getGenericType(), globalScope);
            } else if (Set.class.isAssignableFrom(fieldsType)) {
                memberDSLType =
                        createSetType((ParameterizedType) field.getGenericType(), globalScope);
            }
        }
        if (memberDSLType == null) {
            // lookup the type in already converted types
            // if it is not already in the converted types, try to convert it -> check for
            // DSLType
            // annotation
            memberDSLType = createDSLTypeForJavaTypeInScope(globalScope, field.getType());
        }

        return new Symbol(fieldName, parentType, memberDSLType);
    }

    /**
     * Creates a DSL {@link IType} from a java {@link Type}. Based on the kind of passed {@link
     * Type}, different kinds of {@link IType} will be created. The most common scenario is the
     * creation of an {@link AggregateType} from a class or a record. This requires the class to be
     * marked * with the {@link DSLType} annotation. Each field marked with the {@link
     * DSLTypeMember} annotation will be converted to a member of the created {@link AggregateType},
     * if the field's type can be mapped to a DSL data type. This requires the field's type to be
     * either one of the types declared in {@link BuiltInType} or another class marked with {@link
     * DSLType}. If the passed {@link Type} implements {@link ParameterizedType}, it will either be
     * converted into a {@link ListType} or {@link SetType}, if it assignable to {@link List} or
     * {@link Set} respectively. If the name of the newly created type can be resolved in the passed
     * {@link IScope}, the resolved {@link IType} will be returned.
     *
     * @param globalScope the global scope to use for resolving any DSL datatype
     * @param type the java {@link Type} to create a DSL {@link IType} from
     */
    public IType createDSLTypeForJavaTypeInScope(IScope globalScope, Type type) {
        // catch recursion
        if (this.currentLookedUpTypes.contains(type)) {
            throw new RuntimeException("RECURSIVE TYPE DEF");
        }

        if (this.javaTypeToDSLType.containsKey(type)) {
            return this.javaTypeToDSLType.get(type);
        }

        var builtInType = getBuiltInDSLType(type);
        if (builtInType != null) {
            return builtInType;
        }

        // Try to cast the passed Type to Class<?> (needed for further operations).
        // The passed Type will be either a Class<?> or a ParameterizedType (used to
        // create List- and Set-Types).
        Class<?> clazz = null;
        try {
            clazz = (Class<?>) type;
        } catch (ClassCastException ex) {
            if (type instanceof ParameterizedType parameterizedType) {
                var rawType = parameterizedType.getRawType();
                try {
                    clazz = (Class<?>) rawType;
                } catch (ClassCastException exc) {
                    throw new UnsupportedOperationException(
                            "The TypeBuilder does not support conversion of type " + type);
                }

                // if the cast fails, the type may be a parameterized type (e.g. list or set)
                if (List.class.isAssignableFrom(clazz)) {
                    return createListType((ParameterizedType) type, globalScope);
                } else if (Set.class.isAssignableFrom(clazz)) {
                    return createSetType((ParameterizedType) type, globalScope);
                }
            }
        }

        // trye to resolve the typename in global scope
        String typeName = getDSLTypeName(clazz);
        Symbol resolved = globalScope.resolve(typeName);
        if (resolved != Symbol.NULL) {
            if (resolved instanceof IType) {
                return (IType) resolved;
            } else {
                // symbol with the typename is already bound in the global scope
                // but is not a type
                throw new RuntimeException(
                        "Symbol with name "
                                + typeName
                                + " is already bound in global scope, "
                                + "but not a type");
            }
        }

        if (!clazz.isAnnotationPresent(DSLType.class)) {
            return null;
        }

        // create new AggregateType for clazz
        var aggregateType = new AggregateType(typeName, globalScope, clazz);

        this.currentLookedUpTypes.add(clazz);
        for (Field field : clazz.getDeclaredFields()) {
            // bind new Symbol
            if (field.isAnnotationPresent(DSLTypeMember.class)) {
                var fieldSymbol = createDataMemberSymbol(field, clazz, aggregateType, globalScope);
                aggregateType.bind(fieldSymbol);
            }
            if (field.isAnnotationPresent(DSLCallback.class)) {
                var callbackSymbol = createCallbackMemberSymbol(field, aggregateType, globalScope);
                aggregateType.bind(callbackSymbol);
            }
        }
        this.currentLookedUpTypes.remove(clazz);

        this.javaTypeToDSLType.put(clazz, aggregateType);
        globalScope.bind(aggregateType);
        return aggregateType;
    }

    public void registerProperty(IScope globalScope, IDSLTypeProperty<?, ?> property) {
        // get extended type
        Class<?> propertyClass = property.getClass();
        if (propertyClass.isAnnotationPresent(DSLTypeProperty.class)) {
            var annotation = propertyClass.getAnnotation(DSLTypeProperty.class);
            var extendedClass = annotation.extendedType();
            String extendedClassName = getDSLTypeName(extendedClass);
            Symbol extendedTypeSymbol = globalScope.resolve(extendedClassName);
            if (extendedTypeSymbol.equals(Symbol.NULL)) {
                throw new RuntimeException(
                        "Name of extended type '"
                                + extendedClassName
                                + "' could not be resolved in scope");
            }

            IType extendedType = (IType) extendedTypeSymbol;
            if (extendedType instanceof AggregateType aggregateExtendedType) {
                var genericInterfaces = propertyClass.getGenericInterfaces();
                var type = genericInterfaces[0];
                ParameterizedType parameterizedType = (ParameterizedType) type;

                var instanceType = parameterizedType.getActualTypeArguments()[0];
                IType instanceDSLType = createDSLTypeForJavaTypeInScope(globalScope, instanceType);

                var valueType = parameterizedType.getActualTypeArguments()[1];
                IType valueDSLType = createDSLTypeForJavaTypeInScope(globalScope, valueType);

                // create new symbol for property -> likely requires new Symbol kind
                PropertySymbol propertySymbol =
                        new PropertySymbol(
                                annotation.name(), aggregateExtendedType, valueDSLType, property);
                aggregateExtendedType.bind(propertySymbol);
            }
        }
    }

    public void registerMethod(IScope globalScope, IDSLExtensionMethod<?> method) {
        // get extended type
        Class<?> methodClass = method.getClass();
        if (methodClass.isAnnotationPresent(DSLExtensionMethod.class)) {
            var annotation = methodClass.getAnnotation(DSLExtensionMethod.class);
            var extendedClass = annotation.extendedType();
            String extendedClassName = getDSLTypeName(extendedClass);
            Symbol extendedTypeSymbol = globalScope.resolve(extendedClassName);
            if (extendedTypeSymbol.equals(Symbol.NULL)) {
                throw new RuntimeException(
                        "Name of extended type '"
                                + extendedClassName
                                + "' could not be resolved in scope");
            }

            IType extendedType = (IType) extendedTypeSymbol;
            if (extendedType instanceof AggregateType aggregateExtendedType) {
                var genericInterfaces = methodClass.getGenericInterfaces();
                var type = genericInterfaces[0];
                ParameterizedType parameterizedType = (ParameterizedType) type;

                var instanceType = parameterizedType.getActualTypeArguments()[0];
                IType instanceDSLType = createDSLTypeForJavaTypeInScope(globalScope, instanceType);

                // create FunctionType
                Class<?> returnType = method.getReturnType();
                IType returnDSLType = createDSLTypeForJavaTypeInScope(globalScope, returnType);

                var parameterTypes = method.getParameterTypes();
                List<IType> parameterDSLTypes =
                        parameterTypes.stream()
                                .map(t -> createDSLTypeForJavaTypeInScope(globalScope, t))
                                .toList();

                FunctionType functionType = new FunctionType(returnDSLType, parameterDSLTypes);

                ExtensionMethod nativeMethodSymbol =
                        new ExtensionMethod(
                                annotation.name(),
                                aggregateExtendedType,
                                functionType,
                                (IDSLExtensionMethod<Object>) method);
                aggregateExtendedType.bind(nativeMethodSymbol);
            }
        }
    }
}
