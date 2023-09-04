package semanticanalysis.types;

import interpreter.DSLInterpreter;

import java.util.List;

/**
 * A {@link IDSLExtensionMethod}
 *
 * @param <T> type of the instance in which to access the method
 */
public interface IDSLExtensionMethod<T> {
    // TODO: generify return value
    Object call(DSLInterpreter interpreter, T instance, List<Object> params);

    List<Class<?>> getParameterTypes();

    Class<?> getReturnType();
}
