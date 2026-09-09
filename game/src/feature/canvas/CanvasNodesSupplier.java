package feature.canvas;

import java.util.List;

/**
 * Supplies the nodes currently available on a canvas.
 *
 * <p>For a {@linkplain CanvasContext#prototypeRun() prototype run}, return the union of all nodes
 * that can carry callbacks or filters. The server snapshot still decides which of those nodes are
 * actually displayed.
 */
@FunctionalInterface
public interface CanvasNodesSupplier {

  /**
   * Creates the current canvas nodes.
   *
   * @param context information about the canvas opening
   * @return fresh node instances, or {@code null} for no nodes
   */
  List<CanvasNode> get(CanvasContext context);
}
