package semanticAnalysis.types;

import static semanticAnalysis.types.TypeBuilder.convertToDSLName;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import runtime.MemorySpace;
import runtime.Value;

public class TypeInstantiator {
    private Object instantiateRecord(Class<?> originalJavaClass, MemorySpace ms)
            throws InvocationTargetException, InstantiationException, IllegalAccessException,
                    NoSuchFieldException {

        Constructor<?> ctor = GetConstructor(originalJavaClass);
        if (null == ctor) {
            throw new RuntimeException(
                    "Could not find a suitable constructor to instantiate record "
                            + originalJavaClass.getName());
        }

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
                    parameters.add(internalValue);
                }
            }
        }
        ctor.setAccessible(true);
        return ctor.newInstance(parameters.toArray());
    }

    private Object instantiateClass(Class<?> originalJavaClass, MemorySpace ms) {
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
            instance = ctor.newInstance();

            // TODO: check, if the field was set in the DSL program.. if it was not set, it should
            //  be left at the default defined by the java default ctor (and not the default-Value
            //  created by the DSL)

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
                    if (fieldValue != null) {
                        var internalValue = fieldValue.getInternalValue();

                        field.setAccessible(true);
                        field.set(instance, internalValue);
                    }
                }
            }
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
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

    public Object instantiateFromMemorySpace(AggregateType type, MemorySpace ms)
            throws NoSuchFieldException, InvocationTargetException, InstantiationException,
                    IllegalAccessException {
        var originalJavaClass = type.getOriginalJavaClass();
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
