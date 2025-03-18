package contrib.utils;

import java.util.function.Supplier;

/**
 * Utility class for creating boolean predicates using {@link Supplier}s.
 *
 * <p>This class provides static methods to combine and manipulate boolean predicates represented as
 * {@link Supplier}s. These predicates can be composed using logical operations such as AND, OR,
 * XOR, and NOT.
 *
 * <p>Each method returns a new {@link Supplier<Boolean>} that evaluates the desired logical
 * operation when {@link Supplier#get()} is called.
 *
 * <p>Example usage:
 *
 * <pre>
 * Supplier<Boolean> a = () -> true;
 * Supplier<Boolean> b = () -> true;
 * Supplier<Boolean> c = () -> false;
 * Supplier<Boolean> result = PredicateFactory.and(a, PredicateFactory.xor(b,c)); // result will be false
 * </pre>
 */
public class PredicateFactory {

  /**
   * Creates a {@link Supplier} that represents the logical AND of two boolean predicates.
   *
   * @param a the first boolean predicate
   * @param b the second boolean predicate
   * @return a {@link Supplier<Boolean>} that returns the result of a logical AND operation
   */
  public static Supplier<Boolean> and(Supplier<Boolean> a, Supplier<Boolean> b) {
    return () -> a.get() && b.get();
  }

  /**
   * Creates a {@link Supplier} that represents the logical OR of two boolean predicates.
   *
   * @param a the first boolean predicate
   * @param b the second boolean predicate
   * @return a {@link Supplier<Boolean>} that returns the result of a logical OR operation
   */
  public static Supplier<Boolean> or(Supplier<Boolean> a, Supplier<Boolean> b) {
    return () -> a.get() || b.get();
  }

  /**
   * Creates a {@link Supplier} that represents the logical XOR (exclusive OR) of two boolean
   * predicates.
   *
   * @param a the first boolean predicate
   * @param b the second boolean predicate
   * @return a {@link Supplier<Boolean>} that returns the result of a logical XOR operation
   */
  public static Supplier<Boolean> xor(Supplier<Boolean> a, Supplier<Boolean> b) {
    return () -> a.get() ^ b.get();
  }

  /**
   * Creates a {@link Supplier} that represents the boolean predicate itself.
   *
   * @param a the boolean predicate
   * @return a {@link Supplier<Boolean>} that returns the result of the boolean predicate
   */
  public static Supplier<Boolean> is(Supplier<Boolean> a) {
    return () -> a.get();
  }

  /**
   * Creates a {@link Supplier} that represents the logical NOT of a boolean predicate.
   *
   * @param a the boolean predicate
   * @return a {@link Supplier<Boolean>} that returns the result of a logical NOT operation
   */
  public static Supplier<Boolean> not(Supplier<Boolean> a) {
    return () -> !a.get();
  }
}
