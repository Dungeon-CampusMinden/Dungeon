package semanticAnalysis.types;

import static semanticAnalysis.types.TypeBuilder.convertToDSLName;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import runtime.IMemorySpace;
import runtime.Value;

public class TypeInstantiator {
    private HashMap<String, Object> context = new HashMap<>();

    public void pushContextMember(String name, Object contextMember) {
        context.put(name, contextMember);
    }

    public void removeContextMember(String name) {
        context.remove(name);
    }

    private Object instantiateRecord(Class<?> originalJavaClass, IMemorySpace ms) {

        Constructor<?> ctor = GetConstructor(originalJavaClass);
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
                        var internalValue = fieldValue.getInternalObject();
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

        Constructor<?> ctor = GetConstructor(originalJavaClass);
        if (null == ctor) {
            throw new RuntimeException(
                    "Could not find a suitable constructor to instantiate class "
                            + originalJavaClass.getName());
        }

        Object instance;
        try {
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
                if (field.isAnnotationPresent(DSLTypeMember.class)) {
                    var fieldAnnotation = field.getAnnotation(DSLTypeMember.class);
                    String fieldName =
                            fieldAnnotation.name().equals("")
                                    ? convertToDSLName(field.getName())
                                    : fieldAnnotation.name();

                    var fieldValue = ms.resolve(fieldName);
                    // we only should set the field value explicitly,
                    // if it was set in the program
                    if (fieldValue != null && fieldValue.isDirty()) {
                        var internalValue = fieldValue.getInternalObject();

                        field.setAccessible(true);
                        field.set(instance, internalValue);
                    }
                }
            }
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return instance;
    }

    private Constructor<?> GetConstructor(Class<?> originalJavaClass) {
        Constructor<?> ctor = null;
        for (Constructor<?> constructor : originalJavaClass.getDeclaredConstructors()) {
            ctor = constructor;
            if (ctor.getGenericParameterTypes().length == 0) break;
        }

        return ctor;
    }

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
}
