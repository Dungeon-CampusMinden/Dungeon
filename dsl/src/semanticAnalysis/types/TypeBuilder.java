package semanticAnalysis.types;

import graph.Graph;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import semanticAnalysis.*;

// TODO: should include a way to create a class instance from
//  property-values (by reflection) -> need to store original names
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

    private IType getDSLTypeForMember(Class<?> type) {
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

    public HashMap<String, String> typeMemberNameToJavaFieldMap(Class<?> clazz) {
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

    public AggregateType createTypeFromClass(IScope parentScope, Class<?> clazz) {
        if (!clazz.isAnnotationPresent(DSLType.class)) {
            return null;
        }

        // catch recursion
        if (this.currentLookedUpClasses.contains(clazz)) {
            System.out.println("RECURSIVE TYPE DEF");
            return null;
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
                var fieldAnnotation = field.getAnnotation(DSLTypeMember.class);
                String fieldName =
                        fieldAnnotation.name().equals("")
                                ? convertToDSLName(field.getName())
                                : fieldAnnotation.name();

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
