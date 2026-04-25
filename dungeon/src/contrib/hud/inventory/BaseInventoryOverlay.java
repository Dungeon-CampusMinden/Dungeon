package contrib.hud.inventory;

import contrib.hud.itemgrid.InventoryDialogProvider;
import contrib.hud.itemgrid.BaseItemGridOverlay;
import contrib.hud.itemgrid.GridHitTest;
import contrib.hud.itemgrid.InventoryDragController;
import contrib.hud.itemgrid.InventoryDropHandling;
import contrib.item.Item;
import java.awt.Graphics2D;
import java.util.List;

/**
 * An abstract base class for inventory dialogs rendered as overlays.
 *
 * <p>This class provides core functionality for displaying, rendering, and interacting with
 * inventory dialog interfaces within a game environment. It also incorporates drag-and-drop
 * mechanics and input-handling related to inventory grids.
 *
 * @param <S> the type representing the inventory side or context
 */
abstract class BaseInventoryOverlay<S>
    extends BaseItemGridOverlay<
        InventoryDialogLayoutState.Measurement<S>, InventoryDialogLayoutState<S>>
    implements InventoryDialogProvider {

  private static final int DRAG_THRESHOLD_PX = 8;

  protected final InventoryDragController<S> dragController =
      InventoryDragController.withDistanceThreshold(DRAG_THRESHOLD_PX);

  protected BaseInventoryOverlay(int defaultWidth, int defaultHeight) {
    super(defaultWidth, defaultHeight);
  }

  @Override
  protected final InventoryDialogLayoutState.Measurement<S> measureDialog() {
    return measure();
  }

  @Override
  protected final int dialogWidth(InventoryDialogLayoutState.Measurement<S> measurement) {
    return measurement.dialogWidth();
  }

  @Override
  protected final int dialogHeight(InventoryDialogLayoutState.Measurement<S> measurement) {
    return measurement.dialogHeight();
  }

  @Override
  protected final InventoryDialogLayoutState<S> renderContent(
      Graphics2D g, int contentY, InventoryDialogLayoutState.Measurement<S> measurement) {
    return InventoryDialogRenderer.draw(g, x, contentY, measurement);
  }

  protected final Item[] visibleSlots(Item[] slots, S side) {
    return dragController.visibleSlots(slots, side);
  }

  protected final void handlePrimaryInput(
      List<GridHitTest.Grid<S>> grids,
      InventoryDialogInput.DragReleaseHandler<S> dragReleaseHandler,
      InventoryDialogInput.ClickReleaseHandler<S> clickReleaseHandler) {
    InventoryDialogInput.handlePrimaryInput(
        dragController, grids, this::itemOf, dragReleaseHandler, clickReleaseHandler);
  }

  protected final void resetDragState() {
    dragController.reset();
  }

  @Override
  protected final void handleInput(InventoryDialogLayoutState<S> content) {
    handleInput(content.grids());
  }

  @Override
  protected final void drawPointerFeedback(Graphics2D g, InventoryDialogLayoutState<S> content) {
    List<GridHitTest.Grid<S>> grids = content.grids();
    if (dragController.isDragging()) {
      GridHitTest.Slot<S> hoveredTarget =
          InventoryDialogInput.hoveredDropTarget(dragController, grids, dropTargetFilter());
      if (hoveredTarget != null) {
        InventoryDropHandling.drawGridDropHighlight(g, hoveredTarget, grids);
      }
      dragController.drawDragPreview(g);
      return;
    }

    InventoryDialogInput.drawHoverTooltip(g, bounds(), grids, this::itemOf);
  }

  protected abstract InventoryDialogLayoutState.Measurement<S> measure();

  protected abstract String dialogTitle();

  protected abstract Item itemOf(GridHitTest.Slot<S> slot);

  protected abstract void handleInput(List<GridHitTest.Grid<S>> grids);

  protected abstract InventoryDragController.DropTargetFilter<S> dropTargetFilter();
}
