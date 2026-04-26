package contrib.hud.inventory;

import contrib.hud.InventoryDialogProvider;
import contrib.hud.itemgrid.BaseItemGridOverlay;
import contrib.hud.itemgrid.ItemGridHitTest;
import contrib.hud.itemgrid.ItemGridDragController;
import contrib.hud.itemgrid.InventoryDropHandling;
import contrib.hud.itemgrid.InventoryGridRenderer;
import contrib.hud.itemgrid.InventoryPanelRenderer;
import contrib.hud.itemgrid.InventoryTooltip;
import contrib.item.Item;
import java.awt.Color;
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

  protected final ItemGridDragController<S> dragController =
      ItemGridDragController.withDistanceThreshold(DRAG_THRESHOLD_PX);

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
    InventoryDialogLayoutState<S> layoutState =
        InventoryDialogLayoutState.create(x, contentY, g.getFontMetrics(), measurement);

    if (layoutState.showPanelTitles()) {
      g.setColor(Color.WHITE);
      for (InventoryDialogLayoutState.PanelLayout<S> panel : layoutState.panels()) {
        g.drawString(panel.title(), panel.grid().startX(), layoutState.titleBaseline());
      }
    }

    for (InventoryDialogLayoutState.PanelLayout<S> panel : layoutState.panels()) {
      InventoryPanelRenderer.drawPanelBackground(g, panel.panelBounds());
      InventoryGridRenderer.drawGrid(
          g,
          panel.grid().slots(),
          panel.grid().startX(),
          panel.grid().startY(),
          panel.grid().columns());
    }

    return layoutState;
  }

  protected final Item[] visibleSlots(Item[] slots, S side) {
    return dragController.visibleSlots(slots, side);
  }

  protected final void handlePrimaryInput(
      List<ItemGridHitTest.Grid<S>> grids,
      ItemGridDragController.DragReleaseHandler<S> dragReleaseHandler,
      ItemGridDragController.ClickReleaseHandler<S> clickReleaseHandler) {
    ItemGridDragController.handlePrimaryInput(
        dragController,
        (mouseX, mouseY) -> findSlotSelection(grids, mouseX, mouseY),
        this::itemOf,
        dragReleaseHandler,
        clickReleaseHandler);
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
    List<ItemGridHitTest.Grid<S>> grids = content.grids();
    if (dragController.isDragging()) {
      ItemGridHitTest.Slot<S> hoveredTarget =
          InventoryDropHandling.hoveredDropTarget(
              dragController,
              (mouseX, mouseY) -> findSlotSelection(grids, mouseX, mouseY),
              dropTargetFilter());
      if (hoveredTarget != null) {
        InventoryDropHandling.drawGridDropHighlight(g, hoveredTarget, grids);
      }
      dragController.drawDragPreview(g);
      return;
    }

    InventoryTooltip.drawHoveredSlotTooltip(
        g, bounds(), (mouseX, mouseY) -> findSlotSelection(grids, mouseX, mouseY), this::itemOf);
  }

  protected final ItemGridHitTest.Slot<S> findSlotSelection(
    List<ItemGridHitTest.Grid<S>> grids, int mouseX, int mouseY) {
    return ItemGridHitTest.findGridSlotAt(mouseX, mouseY, grids);
  }

  protected abstract InventoryDialogLayoutState.Measurement<S> measure();

  protected abstract String dialogTitle();

  protected abstract Item itemOf(ItemGridHitTest.Slot<S> slot);

  protected abstract void handleInput(List<ItemGridHitTest.Grid<S>> grids);

  protected abstract ItemGridDragController.DropTargetFilter<S> dropTargetFilter();
}
