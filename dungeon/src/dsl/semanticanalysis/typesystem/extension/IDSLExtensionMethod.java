package dsl.semanticanalysis.typesystem.extension;

import dsl.annotation.DSLType;
import dsl.interpreter.DSLInterpreter;
import dsl.runtime.callable.IInstanceCallable;
import java.lang.reflect.Type;
import java.util.List;

/**
 * A {@link IDSLExtensionMethod} is used to implement a method (instance-function) of a DSL data
 * type created from a java class annotated by {@link DSLType}. It differs from {@link
 * IInstanceCallable} in the aspect, that the parameters are passed to the method as objects, which
 * do not need to be interpreted by a {@link DSLInterpreter}. The parameters are passed as a {@link
 * List}.
 *
 * @param <T> type of the instance in which to access the method.
 * @param <R> type of the return value of the method
 */
public interface IDSLExtensionMethod<T, R> {

  /**
   * Implementation of the method's logic.
   *
   * @param instance the object, which provides the context for the method execution
   * @param params the List of parameters for the method call.
   * @return The return value of the method call.
   */
  R call(T instance, List<Object> params);

  /**
   * Should return an in-order list of the classes, which will be used for the parameters.
   *
   * @return foo
   */
  List<Type> getParameterTypes();
}
