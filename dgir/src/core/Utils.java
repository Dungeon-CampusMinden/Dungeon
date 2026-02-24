package core;

import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;

import core.ir.Op;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

/**
 * Miscellaneous utility helpers used throughout the DGIR. All inner classes have private
 * constructors — they are purely namespace containers.
 */
public class Utils {

  /**
   * Split {@code text} by the first occurrence of {@code delimiter} that appears at nesting depth
   * 0, where depth is tracked by counting matched pairs of {@code < >} and {@code ( )}.
   *
   * <p>This is the core primitive used by {@link #getParameterStrings(String)}. It can be reused
   * whenever a string must be split on an arbitrary delimiter sequence while respecting bracket
   * nesting — for example splitting {@code "(i32, string) -> (bool)"} on {@code "->"}.
   *
   * <p>If the delimiter does not appear at depth 0, the whole input is returned as a single-element
   * list.
   *
   * <p>Examples:
   *
   * <pre>
   *   splitAtDepthZero("(i32, string) -> (bool)", "->")
   *       → ["(i32, string) ", " (bool)"]          // raw, untrimmed
   *
   *   splitAtDepthZero("i32, string", ",")
   *       → ["i32", " string"]
   *
   *   splitAtDepthZero("func.func&lt;(string) -&gt; (bool)&gt;, i32", ",")
   *       → ["func.func&lt;(string) -&gt; (bool)&gt;", " i32"]
   * </pre>
   *
   * @param text      the string to split; must not be {@code null}.
   * @param delimiter the delimiter sequence to split on; must not be {@code null} or empty.
   * @return an unmodifiable list of the parts (in order, not trimmed); never {@code null}.
   */
  @Contract(pure = true)
  public static @NotNull @Unmodifiable List<String> splitAtDepthZero(
      @NotNull String text, @NotNull String delimiter) {
    assert !delimiter.isEmpty() : "delimiter must not be empty";

    List<String> result = new ArrayList<>();
    int depth = 0;
    int start = 0;
    int i = 0;

    while (i < text.length()) {
      char c = text.charAt(i);

      // Track nesting depth
      if (c == '<' || c == '(') {
        depth++;
        i++;
        continue;
      }
      if (c == '>' || c == ')') {
        depth--;
        i++;
        continue;
      }

      // Check for delimiter match at depth 0
      if (depth == 0 && text.startsWith(delimiter, i)) {
        result.add(text.substring(start, i));
        i += delimiter.length();
        start = i;
        continue;
      }

      i++;
    }

    // Add the final segment
    result.add(text.substring(start));

    return Collections.unmodifiableList(result);
  }

  /**
   * Extract the top-level comma-separated parameter strings from a parameterized type ident.
   *
   * <p>The method strips the outermost {@code <…>} wrapper and then splits the inner text by
   * {@code ','} at nesting depth 0 via {@link #splitAtDepthZero(String, String)}. Both
   * angle-bracket pairs ({@code < >}) and parenthesis pairs ({@code ( )}) increment/decrement the
   * depth counter, so nested generic types and parenthesised signatures are never split mid-way.
   * Each resulting segment is trimmed of surrounding whitespace and empty segments are dropped.
   *
   * <p>Examples:
   *
   * <pre>
   *   "func.func&lt;(i32, string) -&gt; (bool)&gt;"
   *       → ["(i32, string) -> (bool)"]
   *
   *   "struct.struct&lt;i32, string&gt;"
   *       → ["i32", "string"]
   *
   *   "func.func&lt;(i32, func.func&lt;(string) -&gt; (bool)&gt;) -&gt; (bool)&gt;"
   *       → ["(i32, func.func&lt;(string) -&gt; (bool)&gt;) -> (bool)"]
   * </pre>
   *
   * @param parameterizedIdent a parameterized ident string that contains exactly one outermost
   *                           {@code <…>} wrapper (e.g. {@code "foo<a, b<c>, d>"}).
   * @return an unmodifiable list of trimmed, non-empty parameter strings; never {@code null}.
   */
  @Contract(pure = true)
  public static @NotNull @Unmodifiable List<String> getParameterStrings(
      @NotNull String parameterizedIdent) {
    // Strip the outermost < … >
    String inner =
        parameterizedIdent.substring(
            parameterizedIdent.indexOf('<') + 1, parameterizedIdent.length() - 1);

    // Delegate to the general splitter, then trim and drop empty segments
    return splitAtDepthZero(inner, ",").stream()
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .toList();
  }

  // =========================================================================
  // Inner: Caller
  // =========================================================================

  /**
   * Stack-walking helpers for identifying the direct caller of a method. Used to enforce "only
   * callable from X" invariants at runtime.
   */
  public static final class Caller {

    public static final @NotNull StackWalker STACK_WALKER =
        StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);

    private Caller() {}

    /**
     * Return the {@link Class} that directly called the method which invoked this utility.
     *
     * @return the calling class.
     * @throws IllegalStateException if the caller cannot be determined.
     */
    @Contract(pure = true)
    public static @NotNull Class<?> getCallingClass() {
      Optional<Class<?>> caller =
          STACK_WALKER.walk(
              stream ->
                  stream
                      .skip(2) // skip getCallingClass() itself
                      .findFirst()
                      .map(StackFrame::getDeclaringClass));
      return caller.orElseThrow(
          () -> new IllegalStateException("Unable to determine calling class"));
    }
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  @Contract(pure = true)
  @NotNull
  public static <T> Iterable<T> iterate(Optional<T> optional) {
    return () -> optional.stream().iterator();
  }

  public static class Dialect {
    /**
     * A mapping from dialect classes to their contributed operations. This is used to populate the
     * global {@link DGIRContext} operation registry at startup. The operations for each dialect are
     * collected via reflection by looking at all permitted subclasses of the dialect's
     * IDialectOperations interface and invoking their default constructors to get their prototypes.
     */
    private static final @NotNull Map<Class<? extends core.Dialect>, @Unmodifiable List<Op>>
        dialectOps = new HashMap<>();

    /**
     * Returns a list of all operation prototypes contributed by this dialect. These prototypes are
     * used to populate the global {@link DGIRContext} operation registry at startup.
     *
     * @return a list of all operation prototypes contributed by this dialect.
     */
    @NotNull
    @Unmodifiable
    public static List<Op> allOps(Class<? extends core.Dialect> dialect, Class<?> diOps) {
      // Check that diOps is a sealed interface
      assert diOps.isSealed() : "IDialectOperations interface must be sealed";

      if (dialectOps.containsKey(dialect)) {
        return dialectOps.get(dialect);
      }

      // Go over all permitted subclasses of this interface and collect their prototypes. This
      // allows
      // us to avoid
      // having to manually list all operations in the dialect, and instead just have them register
      // themselves via implementing
      // their dialect specific subclass.
      List<Op> ops = new ArrayList<>();

      Class<?>[] permittedSubclasses = diOps.getPermittedSubclasses();
      for (Class<?> subclass : permittedSubclasses) {
        // Get the default constructor for this operation and invoke it to get the prototype, then
        // add
        // it to the list of ops for this dialect.
        try {
          Constructor<?> defaultConstructor = subclass.getDeclaredConstructor();
          boolean isAccessible = defaultConstructor.canAccess(null);
          if (!isAccessible) defaultConstructor.setAccessible(true);
          try {
            Op newOp = (Op) defaultConstructor.newInstance();
            ops.add(newOp);
          } catch (InstantiationException e) {
            throw new RuntimeException(
                "Executing default constructor failed for op: " + subclass.getName(), e);
          } catch (IllegalArgumentException
              | InvocationTargetException
              | IllegalAccessException e) {
            throw new RuntimeException(e);
          }
          if (!isAccessible) defaultConstructor.setAccessible(false);
        } catch (NoSuchMethodException e) {
          throw new RuntimeException(
              "Operation class must have a default constructor: " + subclass.getName(), e);
        }
      }
      dialectOps.put(dialect, ops);
      return ops;
    }
  }
}
