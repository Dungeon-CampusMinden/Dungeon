package core;

import com.mxgraph.layout.*;
import java.awt.*;
import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.util.Optional;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

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
}
