package lispy.values;

import java.util.List;

/**
 * Lists.
 *
 * @param elements list of values
 */
public record ListVal(List<Value> elements) implements Value {
  /**
   * create a new list value.
   *
   * @param vs list of values
   * @return new list value
   */
  public static ListVal of(List<Value> vs) {
    return new ListVal(vs);
  }

  /**
   * is this list empty?
   *
   * @return true if empty
   */
  public boolean isEmpty() {
    return elements.isEmpty();
  }

  @Override
  public String toString() {
    return elements.stream()
        .map(Value::pretty)
        .reduce(
            new StringBuilder("("),
            (sb, s) -> sb.length() == 1 ? sb.append(s) : sb.append(' ').append(s),
            StringBuilder::append)
        .append(')')
        .toString();
  }
}
