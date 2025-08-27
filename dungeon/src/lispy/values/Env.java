package lispy.values;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/** memory model for interpreter. */
public class Env {
  private Env enclosing;
  private Map<String, Value> values = new HashMap<>();

  /** New blank environment (top level). */
  public Env() {
    this.enclosing = null;
  }

  /**
   * New blank environment with link to parent environment.
   *
   * @param parent enclosing scope
   */
  public Env(Env parent) {
    this.enclosing = parent;
  }

  /**
   * bind a name to a value.
   *
   * @param name name
   * @param value value
   * @return this environment (for chaining operations)
   */
  public Env define(String name, Value value) {
    values.put(Objects.requireNonNull(name), Objects.requireNonNull(value));
    return this;
  }

  /**
   * bind a name to a value.
   *
   * @param builtins map of name/value pairs
   * @return this environment (for chaining operations)
   */
  public Env define(Map<String, Function<List<Value>, Value>> builtins) {
    builtins.forEach((n, fn) -> define(n, new BuiltinFn(n, fn)));
    return this;
  }

  /**
   * retrieve a value to a name.
   *
   * @param name name
   * @return value
   */
  public Value get(String name) {
    if (values.containsKey(name)) return values.get(name);
    else if (enclosing != null) return enclosing.get(name);
    else throw new RuntimeException("unbound symbol: " + name);
  }
}
