package dgir.core;

import dgir.core.ir.Attribute;
import dgir.core.ir.Op;
import dgir.core.ir.Type;
import dgir.dialect.builtin.BuiltinOps;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Miscellaneous utility helpers used throughout the DGIR. All inner classes have private
 * constructors — they are purely namespace containers.
 */
public class DgirCoreUtils {

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
   * @param text the string to split; must not be {@code null}.
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
   * <p>The method strips the outermost {@code <…>} wrapper and then splits the inner text by {@code
   * ','} at nesting depth 0 via {@link #splitAtDepthZero(String, String)}. Both angle-bracket pairs
   * ({@code < >}) and parenthesis pairs ({@code ( )}) increment/decrement the depth counter, so
   * nested generic types and parenthesised signatures are never split mid-way. Each resulting
   * segment is trimmed of surrounding whitespace and empty segments are dropped.
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
   *     {@code <…>} wrapper (e.g. {@code "foo<a, b<c>, d>"}).
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

  public static final @NotNull StackWalker STACK_WALKER =
      StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);

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
    return caller.orElseThrow(() -> new IllegalStateException("Unable to determine calling class"));
  }

  @Contract(pure = true)
  public static @NotNull String getCallingMethodName() {
    Optional<String> caller =
        STACK_WALKER.walk(
            stream ->
                stream
                    .skip(3) // skip getCallingMethodName() itself
                    .findFirst()
                    .map(StackFrame::getMethodName));
    return caller.orElseThrow(
        () -> new IllegalStateException("Unable to determine calling method"));
  }

  @Contract(pure = true)
  public static @NotNull String getCallingMethodName(int depth) {
    Optional<String> caller =
        STACK_WALKER.walk(
            stream ->
                stream
                    .skip(depth + 1) // skip getCallingMethodName() itself
                    .findFirst()
                    .map(StackFrame::getMethodName));
    return caller.orElseThrow(
        () -> new IllegalStateException("Unable to determine calling method"));
  }

  /**
   * Adapt an {@link Optional} to an {@link Iterable} with zero or one element.
   *
   * <p>Useful in enhanced for-loops where a method returns an {@code Optional} and you want to
   * iterate over the value if present, or skip the loop body if empty.
   *
   * @param optional the optional value to iterate over.
   * @param <T> the element type.
   * @return an iterable yielding the value if present, or an empty iterable.
   */
  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  @Contract(pure = true)
  @NotNull
  public static <T> Iterable<T> iterate(Optional<T> optional) {
    return () -> optional.stream().iterator();
  }

  /**
   * Reflection-based helpers for collecting operation prototypes from a dialect's sealed marker
   * interface.
   */
  public static class Dialect {
    /**
     * Cache of already-computed operation prototype lists, keyed by dialect class. Populated lazily
     * by {@link #allOps(Class, Class)} on the first call for each dialect.
     */
    private static final @NotNull Map<Class<? extends dgir.core.Dialect>, @Unmodifiable List<Op>>
        dialectOps = new HashMap<>();

    /**
     * Cache of already-computed attribute prototype lists, keyed by dialect class. Populated lazily
     * by {@link #allAttributes(Class, Class)} on the first call for each dialect.
     */
    private static final @NotNull Map<
            Class<? extends dgir.core.Dialect>, @Unmodifiable List<Attribute>>
        dialectAttributes = new HashMap<>();

    /**
     * Cache of already-computed type prototype lists, keyed by dialect class. Populated lazily by
     * {@link #allTypes(Class, Class)} on the first call for each dialect.
     */
    private static final @NotNull Map<Class<? extends dgir.core.Dialect>, @Unmodifiable List<Type>>
        dialectTypes = new HashMap<>();

    /**
     * Collect all operation prototypes contributed by a dialect by reflectively instantiating every
     * permitted subclass of {@code diOps} via its no-arg constructor.
     *
     * <p>Results are cached so that repeated calls for the same dialect are cheap. The {@code
     * diOps} argument must be a {@code sealed} interface whose every {@code permits} entry is a
     * concrete op class with a declared no-arg constructor.
     *
     * @param dialect the dialect class requesting the ops (used as the cache key).
     * @param diOps the sealed marker interface whose permitted subclasses enumerate the dialect's
     *     ops (e.g. {@link BuiltinOps}).
     * @return an unmodifiable list of op prototypes, one per permitted subclass.
     * @throws AssertionError if {@code diOps} is not a sealed interface.
     * @throws RuntimeException if any permitted subclass lacks a no-arg constructor or its
     *     constructor throws.
     */
    @NotNull
    @Unmodifiable
    public static List<Op> allOps(Class<? extends dgir.core.Dialect> dialect, Class<?> diOps) {
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

    /**
     * Collect all attribute prototypes contributed by a dialect by reflectively instantiating every
     * permitted subclass of {@code diAttrs} via its no-arg constructor.
     *
     * <p>Results are cached so that repeated calls for the same dialect are cheap. The {@code
     * diAttrs} argument must be a {@code sealed} interface whose every {@code permits} entry is a
     * concrete attribute class with a declared no-arg constructor.
     *
     * @param dialect the dialect class requesting the attributes (used as the cache key).
     * @param diAttrs the sealed marker interface whose permitted subclasses enumerate the dialect's
     *     attributes.
     * @return an unmodifiable list of attribute prototypes, one per permitted subclass.
     * @throws AssertionError if {@code diAttrs} is not a sealed interface.
     * @throws RuntimeException if any permitted subclass lacks a no-arg constructor or its
     *     constructor throws.
     */
    @NotNull
    @Unmodifiable
    public static List<Attribute> allAttributes(
        Class<? extends dgir.core.Dialect> dialect, Class<?> diAttrs) {
      assert diAttrs.isSealed() : "IDialectAttributes interface must be sealed";

      if (dialectAttributes.containsKey(dialect)) {
        return dialectAttributes.get(dialect);
      }

      List<Attribute> attrs = new ArrayList<>();
      Class<?>[] permittedSubclasses = diAttrs.getPermittedSubclasses();
      for (Class<?> subclass : permittedSubclasses) {
        try {
          Constructor<?> defaultConstructor = subclass.getDeclaredConstructor();
          boolean isAccessible = defaultConstructor.canAccess(null);
          if (!isAccessible) defaultConstructor.setAccessible(true);
          try {
            Attribute newAttr = (Attribute) defaultConstructor.newInstance();
            attrs.add(newAttr);
          } catch (InstantiationException e) {
            throw new RuntimeException(
                "Executing default constructor failed for attribute: " + subclass.getName(), e);
          } catch (IllegalArgumentException
              | InvocationTargetException
              | IllegalAccessException e) {
            throw new RuntimeException(e);
          }
          if (!isAccessible) defaultConstructor.setAccessible(false);
        } catch (NoSuchMethodException e) {
          throw new RuntimeException(
              "Attribute class must have a default constructor: " + subclass.getName(), e);
        }
      }
      dialectAttributes.put(dialect, attrs);
      return attrs;
    }

    /**
     * Collect all type prototypes contributed by a dialect by reflectively instantiating every
     * permitted subclass of {@code diTypes} via its no-arg constructor.
     *
     * <p>Results are cached so that repeated calls for the same dialect are cheap. The {@code
     * diTypes} argument must be a {@code sealed} interface whose every {@code permits} entry is a
     * concrete type class with a declared no-arg constructor.
     *
     * @param dialect the dialect class requesting the types (used as the cache key).
     * @param diTypes the sealed marker interface whose permitted subclasses enumerate the dialect's
     *     types.
     * @return an unmodifiable list of type prototypes, one per permitted subclass.
     * @throws AssertionError if {@code diTypes} is not a sealed interface.
     * @throws RuntimeException if any permitted subclass lacks a no-arg constructor or its
     *     constructor throws.
     */
    @NotNull
    @Unmodifiable
    public static List<Type> allTypes(
        Class<? extends dgir.core.Dialect> dialect, Class<?> diTypes) {
      assert diTypes.isSealed() : "IDialectTypes interface must be sealed";

      if (dialectTypes.containsKey(dialect)) {
        return dialectTypes.get(dialect);
      }

      List<Type> types = new ArrayList<>();
      Class<?>[] permittedSubclasses = diTypes.getPermittedSubclasses();
      for (Class<?> subclass : permittedSubclasses) {
        try {
          Constructor<?> defaultConstructor = subclass.getDeclaredConstructor();
          boolean isAccessible = defaultConstructor.canAccess(null);
          if (!isAccessible) defaultConstructor.setAccessible(true);
          try {
            Type newType = (Type) defaultConstructor.newInstance();
            types.addAll(newType.getDefaultTypeInstances());
          } catch (InstantiationException e) {
            throw new RuntimeException(
                "Executing default constructor failed for type: " + subclass.getName(), e);
          } catch (IllegalArgumentException
              | InvocationTargetException
              | IllegalAccessException e) {
            throw new RuntimeException(e);
          }
          if (!isAccessible) defaultConstructor.setAccessible(false);
        } catch (NoSuchMethodException e) {
          throw new RuntimeException(
              "Type class must have a default constructor: " + subclass.getName(), e);
        }
      }
      dialectTypes.put(dialect, types);
      return types;
    }
  }
}
