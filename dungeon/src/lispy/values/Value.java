package lispy.values;

/**
 * Representation of values.
 *
 * <p>we could just use Integer, String, Boolean ... however, this allows us to use a nice little
 * type (instead of Object) and to use pattern matching with switch/case
 */
public sealed interface Value permits NumVal, StrVal, BoolVal, ListVal, FnVal {
  /**
   * do some pretty printing.
   *
   * @return formatted string
   */
  default String pretty() {
    return switch (this) {
      case NumVal n -> Integer.toString(n.value());
      case StrVal s -> "\"" + s.value() + "\"";
      case BoolVal b -> Boolean.toString(b.value());
      case ListVal l -> l.toString();
      case BuiltinFn f -> "<builtin " + f.name() + ">";
      case ClosureFn f -> "<fn " + f.name() + ">";
    };
  }
}
