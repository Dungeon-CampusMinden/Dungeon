package blockly.vm.dgir.core;

import blockly.vm.dgir.core.ir.Block;
import blockly.vm.dgir.core.ir.Operand;
import blockly.vm.dgir.core.ir.Operation;
import blockly.vm.dgir.core.ir.Value;
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

  public static class Reflection {
    private Reflection() {
    }

    public static boolean hasInterface(Class<?> clazz, Class<?> interfaceClass) {
      return Arrays.asList(clazz.getInterfaces()).contains(interfaceClass);
    }
  }

  public static class Graphing {
    private Graphing() {
    }

    public static Graph<Object, DefaultEdge> getUseGraph(Operation op) {
      Graph<Object, DefaultEdge> useGraph = GraphTypeBuilder.directed().edgeClass(DefaultEdge.class).buildGraph();
      for (Block block : op.getRegions().getFirst().getBlocks()) {
        for (Operation o : block.getOperations()) {
          if (o.getOutput() != null) {
            useGraph.addVertex(o);
            for (Operand<Value, ?> operand : o.getOutputValue().getUses()) {
              Operation userOp = operand.getOwner();
              useGraph.addVertex(userOp);
              useGraph.addEdge(o, userOp);
            }
          }

          if (!o.getRegions().isEmpty()) {
            var subGraph = getUseGraph(o);
            // Add the subgraph vertices and edges to the main graph
            for (var vertex : subGraph.vertexSet()) {
              useGraph.addVertex(vertex);
            }
            for (var edge : subGraph.edgeSet()) {
              var source = subGraph.getEdgeSource(edge);
              var target = subGraph.getEdgeTarget(edge);
              useGraph.addEdge(source, target);
            }
          }
        }
      }
      return useGraph;
    }

    public static File drawUseGraph(Operation op, String filePath, boolean hierarchicalLayout) throws IOException {
      Graph<Object, DefaultEdge> useGraph = getUseGraph(op);
      return drawGraph(useGraph, filePath, hierarchicalLayout);
    }

    public static <V, E> File drawGraph(Graph<V, E> graph, String filePath, boolean hierarchicalLayout) throws IOException {
      JGraphXAdapter<V, E> graphAdapter = new JGraphXAdapter<V, E>(graph);
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


