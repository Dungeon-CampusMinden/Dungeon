package contrib.hud.utils;

import core.Game;
import core.ui.StageHandle;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.List;

/** Shared drop-target handling for inventory-like overlays. */
public final class InventoryDropHandling {

  private static final int DRAG_TARGET_INSET = 3;
  private static final int DRAG_TARGET_ARC = 8;

  private InventoryDropHandling() {}

  /**
   * Resolves the hovered drop target for the current mouse position.
   *
   * @param dragController the active drag controller
   * @param slotFinder resolves slots at pointer positions
   * @param targetFilter decides whether a target accepts the active source
   * @param <S> logical inventory side type
   * @return the accepted target slot, or {@code null}
   */
  public static <S> GridHitTest.Slot<S> hoveredDropTarget(
      InventoryDragController<S> dragController,
      InventoryDragController.SlotFinder<S> slotFinder,
      InventoryDragController.DropTargetFilter<S> targetFilter) {
    StageHandle stage = Game.stage().orElse(null);
    if (stage == null) {
      return null;
    }

    return dragController.dropTargetAt(stage.mouseX(), stage.mouseY(), slotFinder, targetFilter);
  }

  /**
   * Draws the default inventory drop highlight for a slot in one of the supplied grids.
   *
   * @param g the graphics context
   * @param targetSlot the target slot to highlight
   * @param grids the grids to search
   * @param <S> logical inventory side type
   */
  @SafeVarargs
  public static <S> void drawGridDropHighlight(
      Graphics2D g, GridHitTest.Slot<S> targetSlot, GridHitTest.Grid<S>... grids) {
    drawGridDropHighlight(g, targetSlot, Arrays.asList(grids));
  }

  /**
   * Draws the default inventory drop highlight for a slot in one of the supplied grids.
   *
   * @param g the graphics context
   * @param targetSlot the target slot to highlight
   * @param grids the grids to search
   * @param <S> logical inventory side type
   */
  public static <S> void drawGridDropHighlight(
      Graphics2D g, GridHitTest.Slot<S> targetSlot, List<GridHitTest.Grid<S>> grids) {
    Rectangle targetBounds = GridHitTest.boundsOf(targetSlot, grids);
    if (targetBounds == null) {
      return;
    }

    drawDropHighlight(
        g,
        targetBounds,
        InventoryDragController.DEFAULT_DROP_FILL,
        InventoryDragController.DEFAULT_DROP_OUTLINE);
  }

  /**
   * Draws a rounded drop-target highlight inside the given bounds.
   *
   * @param g the graphics context
   * @param bounds the bounds to highlight
   * @param fill the highlight fill color
   * @param outline the highlight outline color
   */
  public static void drawDropHighlight(Graphics2D g, Rectangle bounds, Color fill, Color outline) {
    if (g == null || bounds == null) {
      return;
    }

    int insetX = bounds.x + DRAG_TARGET_INSET;
    int insetY = bounds.y + DRAG_TARGET_INSET;
    int insetWidth = bounds.width - 2 * DRAG_TARGET_INSET;
    int insetHeight = bounds.height - 2 * DRAG_TARGET_INSET;

    g.setColor(fill);
    g.fillRoundRect(insetX, insetY, insetWidth, insetHeight, DRAG_TARGET_ARC, DRAG_TARGET_ARC);

    g.setColor(outline);
    g.drawRoundRect(insetX, insetY, insetWidth, insetHeight, DRAG_TARGET_ARC, DRAG_TARGET_ARC);
  }
}
