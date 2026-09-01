package feature.canvas;

import java.util.List;
import java.util.Objects;

/**
 * Information delivered to a node while another node is dragged over it.
 *
 * @param canvas the canvas performing the drag
 * @param draggedNode the primary node currently being dragged
 * @param draggedNodes all nodes moving as part of the current drag
 * @param worldX the current pointer x coordinate in world space
 * @param worldY the current pointer y coordinate in world space
 * @param areaX the current pointer x coordinate in canvas-area space
 * @param areaY the current pointer y coordinate in canvas-area space
 * @param localX the pointer x coordinate relative to the receiving node
 * @param localY the pointer y coordinate relative to the receiving node
 */
public record CanvasDragContext(
    CanvasArea canvas,
    CanvasNode draggedNode,
    List<CanvasNode> draggedNodes,
    float worldX,
    float worldY,
    float areaX,
    float areaY,
    float localX,
    float localY) {

  /** Validates required context values. */
  public CanvasDragContext {
    Objects.requireNonNull(canvas, "canvas");
    Objects.requireNonNull(draggedNode, "draggedNode");
    draggedNodes = List.copyOf(draggedNodes);
  }
}
