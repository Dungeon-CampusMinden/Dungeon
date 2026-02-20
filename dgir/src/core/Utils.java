package core;

import core.ir.Block;
import core.ir.Operand;
import core.ir.Operation;
import core.ir.Value;
import com.mxgraph.layout.*;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.util.mxCellRenderer;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graph;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.util.Arrays;
import java.util.Optional;

/**
 * Miscellaneous utility helpers used throughout the DGIR.
 * All inner classes have private constructors — they are purely namespace containers.
 */
public class Utils {

  // =========================================================================
  // Inner: Caller
  // =========================================================================

  /**
   * Stack-walking helpers for identifying the direct caller of a method.
   * Used to enforce "only callable from X" invariants at runtime.
   */
  public static final class Caller {

    public static final @NotNull StackWalker STACK_WALKER = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);

    private Caller() {
    }

    /**
     * Return the {@link Class} that directly called the method which invoked this utility.
     *
     * @return the calling class.
     * @throws IllegalStateException if the caller cannot be determined.
     */
    public static @NotNull Class<?> getCallingClass() {
      Optional<Class<?>> caller = STACK_WALKER.walk(stream -> stream
        .skip(2)   // skip getCallingClass() itself
        .findFirst().map(StackFrame::getDeclaringClass));
      return caller.orElseThrow(() -> new IllegalStateException("Unable to determine calling class"));
    }
  }
}
