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
    private static final @NotNull Map<Class<? extends core.Dialect>, @Unmodifiable List<Op>> dialectOps =
        new HashMap<>();

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
