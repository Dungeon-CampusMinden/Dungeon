package lispy.values;

import java.util.List;
import java.util.function.Function;

/**
 * Builtin functions.
 *
 * @param name fn name
 * @param impl reference to implementation (lambda)
 */
public record BuiltinFn(String name, Function<List<Value>, Value> impl) implements FnVal {
  @Override
  public Value apply(List<Value> args) {
    return impl.apply(args);
  }
}
