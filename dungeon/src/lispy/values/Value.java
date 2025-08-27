package lispy.values;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Representation of values.
 *
 * <p>we could just use Integer, String, Boolean ... however, this allows us to use a nice little
 * type (instead of Object) and to use pattern matching with switch/case
 */
public sealed interface Value permits NumVal, StrVal, BoolVal, ListVal, FnVal {
  /**
   * Read value as Integer.
   *
   * @param v value
   * @return Integer
   */
  static Integer asNum(Value v) {
    return switch (v) {
      case NumVal(int value) -> value;
      default -> throw new RuntimeException("number expected, got: " + Value.pretty(v));
    };
  }

  /**
   * Read value as ListVal.
   *
   * @param v value
   * @return ListVal
   */
  static ListVal asList(Value v) {
    return switch (v) {
      case ListVal l -> l;
      default -> throw new RuntimeException("list expected, got: " + Value.pretty(v));
    };
  }

  /**
   * Read value in boolean context.
   *
   * @param v value
   * @return true, if (BoolVal and true) or other value type
   */
  static boolean isTruthy(Value v) {
    return switch (v) {
      case BoolVal(boolean value) -> value;
      default -> true;
    };
  }

  /**
   * Compare values.
   *
   * @param a lhs value
   * @param b rhs value
   * @return true if equal
   */
  static boolean valueEquals(Value a, Value b) {
    return switch (a) {
      case NumVal an ->
          switch (b) {
            case NumVal bn -> an.value() == bn.value();
            default -> false;
          };
      case StrVal as ->
          switch (b) {
            case StrVal bs -> as.value().equals(bs.value());
            default -> false;
          };
      case BoolVal ab ->
          switch (b) {
            case BoolVal bb -> ab.value() == bb.value();
            default -> false;
          };
      case ListVal al ->
          switch (b) {
            case ListVal bl -> {
              List<Value> aElems = al.elements();
              List<Value> bElems = bl.elements();
              yield aElems.size() == bElems.size()
                  && IntStream.range(0, aElems.size())
                      .allMatch(i -> valueEquals(aElems.get(i), bElems.get(i)));
            }
            default -> false;
          };

      case FnVal af -> af == b; // functions: use identity
    };
  }

  /**
   * do some pretty printing.
   *
   * @param v value
   * @return formatted string
   */
  static String pretty(Value v) {
    return switch (v) {
      case NumVal n -> Integer.toString(n.value());
      case StrVal s -> "\"" + s.value() + "\"";
      case BoolVal b -> Boolean.toString(b.value());
      case ListVal l -> l.toString();
      case BuiltinFn f -> "<builtin " + f.name() + ">";
      case ClosureFn f -> "<fn " + f.name() + ">";
    };
  }
}
