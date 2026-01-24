package blockly.vm.dgir.core;

import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.util.Optional;

public class Utils {

  public static final class Caller {
    private static final StackWalker STACK_WALKER = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);

    private Caller() {
    }

    /**
     * Returns the Class that directly called the method invoking this utility.
     *
     * @return the calling Class
     * @throws IllegalStateException if the caller cannot be determined
     */
    public static Class<?> getCallingClass() {
      Optional<Class<?>> caller = STACK_WALKER.walk(stream -> stream
        // skip getCallingClass()
        .skip(2)
        // take the immediate caller
        .findFirst().map(StackFrame::getDeclaringClass));

      return caller.orElseThrow(() -> new IllegalStateException("Unable to determine calling class"));
    }
  }

}
