package contrib.hud.inventory;

import contrib.hud.dialogs.DialogInventoryProvider;
import contrib.hud.itemgrid.GridHitTest;
import contrib.hud.itemgrid.InventoryDragController;
import contrib.hud.itemgrid.InventoryDropHandling;
import contrib.hud.renderers.DialogFrameRenderer;
import contrib.item.Item;
import core.Game;
import core.ui.overlay.BaseUiOverlay;
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
abstract class BaseInventoryOverlay<S> extends BaseUiOverlay
    implements DialogInventoryProvider {

  private static final int DRAG_THRESHOLD_PX = 8;

  protected final InventoryDragController<S> dragController =
      InventoryDragController.withDistanceThreshold(DRAG_THRESHOLD_PX);

  protected BaseInventoryOverlay(int defaultWidth, int defaultHeight) {
    super(defaultWidth, defaultHeight);
  }

  @Override
  public final void render(Graphics2D g) {
    if (!visible) {
      return;
    }

    InventoryDialogLayoutState.Measurement<S> measurement = measure();
    width = measurement.dialogWidth();
    height = measurement.dialogHeight();

    centerInIfUnpositioned(Game.windowWidth(), Game.windowHeight());

    InventoryDialogLayoutState<S> layoutState;
    DialogFrameRenderer.RenderState state = DialogFrameRenderer.beginDialog(g);

    try {
      int contentY = DialogFrameRenderer.drawFrameAndTitle(g, x, y, width, height, dialogTitle());
      layoutState = InventoryDialogRenderer.draw(g, x, contentY, measurement);
      drawPointerFeedback(g, layoutState.grids());
    } finally {
      DialogFrameRenderer.finishDialog(g, state);
    }

    handleInput(layoutState.grids());
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

  private void drawPointerFeedback(Graphics2D g, List<GridHitTest.Grid<S>> grids) {
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
