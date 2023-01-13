package semanticAnalysis.types;

import graph.Graph;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import semanticAnalysis.*;

public class TypeBuilder {
    private final HashMap<Class<?>, AggregateType> javaTypeToAggregateType;
    private final HashSet<Class<?>> currentLookedUpClasses;

    public TypeBuilder() {
        this.javaTypeToAggregateType = new HashMap<>();
        this.currentLookedUpClasses = new HashSet<>();
    }

    public static String convertToDSLName(String name) {
        Pattern pattern = Pattern.compile("([a-z0-9_])([A-Z])");
        Matcher matcher = pattern.matcher(name);
        var underscored = matcher.replaceAll(mr -> mr.group(1) + '_' + mr.group(2).toLowerCase());
        return underscored.toLowerCase();
    }

    public static IType getDSLTypeForMember(Class<?> type) {
        if (int.class.equals(type)
                || short.class.equals(type)
                || long.class.equals(type)
                || Integer.class.isAssignableFrom(type)) {
            return BuiltInType.intType;
        } else if (String.class.equals(type) || String.class.isAssignableFrom(type)) {
            return BuiltInType.stringType;
        } else if (Graph.class.equals(type) || Graph.class.isAssignableFrom(type)) {
            return BuiltInType.graphType;
        } else {

        }

        return null;
    }

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

    public static String getDSLName(Class<?> clazz) {
        var classAnnotation = clazz.getAnnotation(DSLType.class);
        return classAnnotation.name().equals("")
                ? convertToDSLName(clazz.getName())
                : classAnnotation.name();
    }

    public static String getDSLName(Field field) {
        var fieldAnnotation = field.getAnnotation(DSLTypeMember.class);
        return fieldAnnotation.name().equals("")
                ? convertToDSLName(field.getName())
                : fieldAnnotation.name();
    }

    public AggregateType createTypeFromClass(IScope parentScope, Class<?> clazz) {
        if (!clazz.isAnnotationPresent(DSLType.class)) {
            return null;
        }

        // catch recursion
        if (this.currentLookedUpClasses.contains(clazz)) {
            throw new RuntimeException("RECURSIVE TYPE DEF");
        }

        if (this.javaTypeToAggregateType.containsKey(clazz)) {
            return this.javaTypeToAggregateType.get(clazz);
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
                var memberDSLType = getDSLTypeForMember(field.getType());
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
        this.javaTypeToAggregateType.put(clazz, type);
        return type;
    }
}
