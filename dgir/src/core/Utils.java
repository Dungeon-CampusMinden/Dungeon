package core;

import core.ir.Block;
import core.ir.Operand;
import core.ir.Operation;
import core.ir.Value;
import com.mxgraph.layout.*;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.util.mxCellRenderer;
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

    public static final StackWalker STACK_WALKER = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);

    private Caller() {
    }

    /**
     * Return the {@link Class} that directly called the method which invoked this utility.
     *
     * @return the calling class.
     * @throws IllegalStateException if the caller cannot be determined.
     */
    public static Class<?> getCallingClass() {
      Optional<Class<?>> caller = STACK_WALKER.walk(stream -> stream
        .skip(2)   // skip getCallingClass() itself
        .findFirst().map(StackFrame::getDeclaringClass));
      return caller.orElseThrow(() -> new IllegalStateException("Unable to determine calling class"));
    }
  }

  // =========================================================================
  // Inner: Reflection
  // =========================================================================

  /**
   * Reflection helpers for inspecting class metadata.
   */
  public static class Reflection {

    private Reflection() {
    }

    /**
     * Return {@code true} if {@code clazz} directly implements {@code interfaceClass}.
     */
    public static boolean hasInterface(Class<?> clazz, Class<?> interfaceClass) {
      return Arrays.asList(clazz.getInterfaces()).contains(interfaceClass);
    }
  }

  // =========================================================================
  // Inner: Graphing
  // =========================================================================

  /**
   * Helpers for building and rendering use-def graphs of IR operations.
   */
  public static class Graphing {

    private Graphing() {
    }

    /**
     * Build a directed use-def graph for {@code op}'s first region.
     * Operations producing a value are vertices; an edge {@code a → b} indicates that
     * {@code b} consumes a value defined by {@code a}.
     *
     * @param op The root operation whose region is traversed.
     * @return The constructed use-def graph.
     */
    public static Graph<Object, DefaultEdge> getUseGraph(Operation op) {
      Graph<Object, DefaultEdge> useGraph = GraphTypeBuilder.directed().edgeClass(DefaultEdge.class).buildGraph();
      for (Block block : op.getRegions().getFirst().getBlocks()) {
        for (Operation o : block.getOperations()) {
          Optional<Value> output = o.getOutputValue();
          if (output.isPresent()) {
            useGraph.addVertex(o);
            for (Operand<Value, ?> operand : output.get().getUses()) {
              Operation userOp = operand.getOwner();
              useGraph.addVertex(userOp);
              useGraph.addEdge(o, userOp);
            }
          }

          if (!o.getRegions().isEmpty()) {
            var subGraph = getUseGraph(o);
            for (var vertex : subGraph.vertexSet())
              useGraph.addVertex(vertex);
            for (var edge : subGraph.edgeSet())
              useGraph.addEdge(subGraph.getEdgeSource(edge), subGraph.getEdgeTarget(edge));
          }
        }
      }
      return useGraph;
    }

    /**
     * Build the use-def graph for {@code op} and render it to a PNG file.
     *
     * @param op                 The root operation.
     * @param filePath           Destination file path (should end in {@code .png}).
     * @param hierarchicalLayout Use hierarchical layout if {@code true}, compact tree otherwise.
     * @return The written {@link File}.
     */
    public static File drawUseGraph(Operation op, String filePath, boolean hierarchicalLayout) throws IOException {
      return drawGraph(getUseGraph(op), filePath, hierarchicalLayout);
    }

    /**
     * Render an arbitrary JGraphT graph to a PNG file.
     *
     * @param graph              The graph to render.
     * @param filePath           Destination file path.
     * @param hierarchicalLayout Use hierarchical layout if {@code true}, compact tree otherwise.
     * @return The written {@link File}.
     */
    public static <V, E> File drawGraph(Graph<V, E> graph, String filePath, boolean hierarchicalLayout) throws IOException {
      JGraphXAdapter<V, E> graphAdapter = new JGraphXAdapter<>(graph);
      if (hierarchicalLayout)
        new mxHierarchicalLayout(graphAdapter).execute(graphAdapter.getDefaultParent());
      else
        new mxCompactTreeLayout(graphAdapter, false).execute(graphAdapter.getDefaultParent());
      new mxParallelEdgeLayout(graphAdapter).execute(graphAdapter.getDefaultParent());
      BufferedImage image = mxCellRenderer.createBufferedImage(graphAdapter, null, 2, new Color(1f, 1f, 1f, 0f), true, null);
      File imgFile = new File(filePath);
      ImageIO.write(image, "PNG", imgFile);
      return imgFile;
    }
  }
}
