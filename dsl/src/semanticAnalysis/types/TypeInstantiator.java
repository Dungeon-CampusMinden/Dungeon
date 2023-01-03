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
        Constructor[] ctors = originalJavaClass.getDeclaredConstructors();
        Constructor ctor = null;
        for (int i = 0; i < ctors.length; i++) {
            ctor = ctors[i];
            if (ctor.getGenericParameterTypes().length == 0) break;
        }

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
                // TODO: generate error message about this
                if (fieldValue == null || fieldValue == Value.NONE) {
                    return null;
                } else {
                    var internalValue = fieldValue.getInternalValue();
                    parameters.add(internalValue);
                }
            }
        }
        ctor.setAccessible(true);
        return ctor.newInstance(parameters.toArray());
    }

    public Object instantiateFromMemorySpace(AggregateType type, MemorySpace ms)
            throws NoSuchFieldException, InvocationTargetException, InstantiationException,
                    IllegalAccessException {
        Object instance;
        var originalJavaClass = type.getOriginalJavaClass();
        if (null == originalJavaClass) {
            return null;
        }

        if (originalJavaClass.isRecord()) {
            return instantiateRecord(originalJavaClass, ms);
        } else {
            Constructor[] ctors = originalJavaClass.getDeclaredConstructors();
            Constructor ctor = null;
            for (int i = 0; i < ctors.length; i++) {
                ctor = ctors[i];
                // if (ctor.isAnnotationPresent(DSLDefaultCtor.class))
                if (ctor.getGenericParameterTypes().length == 0) break;
            }

            try {
                ctor.setAccessible(true);
                instance = ctor.newInstance();

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

                            // on a record field, this does not work...
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
    }
}
