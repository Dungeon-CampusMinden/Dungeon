package semanticAnalysis.types;

import dslToGame.graph.Graph;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import semanticAnalysis.*;

public class TypeBuilder {
    private final HashMap<Class<?>, Method> typeAdapters;
    private final HashMap<Class<?>, IType> javaTypeToDSLType;
    private final HashSet<Class<?>> currentLookedUpClasses;

    /** Constructor */
    public TypeBuilder() {
        this.typeAdapters = new HashMap<>();
        this.javaTypeToDSLType = new HashMap<>();
        this.currentLookedUpClasses = new HashSet<>();
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
     * @return the corresponding {@link IType} for the passed type, or null
     */
    public static IType getDSLTypeForClass(Class<?> type) {
        if (int.class.equals(type)
                || short.class.equals(type)
                || long.class.equals(type)
                || Integer.class.isAssignableFrom(type)) {
            return BuiltInType.intType;
        } else if (float.class.equals(type)
                || double.class.equals(type)
                || Float.class.isAssignableFrom(type)) {
            return BuiltInType.floatType;
        } else if (String.class.equals(type) || String.class.isAssignableFrom(type)) {
            return BuiltInType.stringType;
        } else if (Graph.class.equals(type) || Graph.class.isAssignableFrom(type)) {
            return BuiltInType.graphType;
        } else {

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
            if (field.isAnnotationPresent(DSLTypeMember.class)) {
                var fieldAnnotation = field.getAnnotation(DSLTypeMember.class);
                String fieldName =
                        fieldAnnotation.name().equals("")
                                ? convertToDSLName(field.getName())
                                : fieldAnnotation.name();

                map.put(fieldName, field.getName());
            }
        }
        return map;
    }

    /**
     * @param clazz the Class to get the DSL name for
     * @return converted name (conversion by {@link #convertToDSLName(String)}) or the 'name'
     *     parameter of {@link DSLType}
     */
    public static String getDSLName(Class<?> clazz) {
        var classAnnotation = clazz.getAnnotation(DSLType.class);
        return classAnnotation.name().equals("")
                ? convertToDSLName(clazz.getName())
                : classAnnotation.name();
    }

    /**
     * @param field the field to get the DSL name for
     * @return converted name (conversion by {@link #convertToDSLName(String)}) or the 'name'
     *     parameter of {@link DSLTypeMember}
     */
    public static String getDSLName(Field field) {
        var fieldAnnotation = field.getAnnotation(DSLTypeMember.class);
        return fieldAnnotation.name().equals("")
                ? convertToDSLName(field.getName())
                : fieldAnnotation.name();
    }

    /**
     * @param parameter the parameter to get the DSL name for
     * @return converted name (conversion by {@link #convertToDSLName(String)}) or the 'name'
     *     parameter of {@link DSLTypeMember}
     */
    public static String getDSLName(Parameter parameter) {
        var parameterAnnotation = parameter.getAnnotation(DSLTypeMember.class);
        return parameterAnnotation.name().equals("")
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
                var annotation = method.getAnnotation(DSLTypeAdapter.class);
                var forType = annotation.t();
                if (this.typeAdapters.containsKey(forType)) {
                    return false;
                }
                this.typeAdapters.put(forType, method);

                // create adapterType
                var adapterType = createAdapterType(forType, method, parentScope);
                this.javaTypeToDSLType.put(forType, adapterType);

                return true;
            }
        }
        return true;
    }

    public IType createAdapterType(Class<?> forType, Method adapterMethod, IScope parentScope) {
        String dslTypeName = convertToDSLName(forType.getSimpleName());
        // get parameters, if only one: PODType, otherwise: AggregateType
        if (adapterMethod.getParameterCount() == 0) {
            // TODO: handle
            throw new RuntimeException(
                    "Builder methods with zero arguments are currently not supported");
        }

        if (adapterMethod.getParameterCount() == 1) {
            var paramType = adapterMethod.getParameterTypes()[0];
            // TODO: how to handle non-builtIn types here?
            var paramDSLType = getDSLTypeForClass(paramType);
            return new AdaptedType(
                    dslTypeName, parentScope, forType, (BuiltInType) paramDSLType, adapterMethod);
        } else {
            var typeAdapter =
                    new AggregateTypeAdapter(dslTypeName, parentScope, forType, adapterMethod);
            // bind symbol for each parameter in the adapterMethod
            for (var parameter : adapterMethod.getParameters()) {
                String parameterName = getDSLName(parameter);
                IType paramDSLType = getDSLTypeForClass(parameter.getType());
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

        if (!clazz.isAnnotationPresent(DSLType.class)) {
            return null;
        }

        // catch recursion
        if (this.currentLookedUpClasses.contains(clazz)) {
            throw new RuntimeException("RECURSIVE TYPE DEF");
        }

        var annotation = clazz.getAnnotation(DSLType.class);
        String typeName =
                annotation.name().equals("")
                        ? convertToDSLName(clazz.getSimpleName())
                        : annotation.name();

        // TODO: refactor
        var type = new AggregateType(typeName, parentScope, clazz);
        for (Field field : clazz.getDeclaredFields()) {
            // bind new Symbol
            if (field.isAnnotationPresent(DSLTypeMember.class)) {
                String fieldName = getDSLName(field);

                // get datatype
                var memberDSLType = getDSLTypeForClass(field.getType());
                if (memberDSLType == null) {
                    // lookup the type in already converted types
                    // if it is not already in the converted types, try to convert it -> check for
                    // DSLType
                    // annotation
                    this.currentLookedUpClasses.add(clazz);
                    memberDSLType = createTypeFromClass(parentScope, field.getType());
                    this.currentLookedUpClasses.remove(clazz);
                }

                var fieldSymbol = new Symbol(fieldName, type, memberDSLType);
                type.bind(fieldSymbol);
            }
        }
        this.javaTypeToDSLType.put(clazz, type);
        return type;
    }
}
