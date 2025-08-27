package lispy.values;

import java.util.List;

/** Functions in Lispy. */
public sealed interface FnVal extends Value permits BuiltinFn, ClosureFn {
  /**
   * Apply function to arguments.
   *
   * @param args list of arguments
   * @return function result
   */
  Value apply(List<Value> args);
}
