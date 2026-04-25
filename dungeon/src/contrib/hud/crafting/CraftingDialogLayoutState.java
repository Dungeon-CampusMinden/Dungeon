package contrib.hud.crafting;

import contrib.hud.dialogs.shared.DialogFrameMetrics;
import contrib.hud.itemgrid.GridHitTest;
import contrib.hud.itemgrid.InventoryGridRenderer;
import contrib.hud.itemgrid.InventoryPanelRenderer;
import contrib.item.Item;
import java.awt.FontMetrics;
import java.awt.Rectangle;

/** Calculated geometry for one rendered crafting dialog frame. */
final class CraftingDialogLayoutState {

  static final int DEFAULT_WIDTH = 1180;
  static final int DEFAULT_HEIGHT = 600;

  private static final int PANEL_GAP = 26;
  private static final int PANEL_HEADER_GAP = 14;
  private static final int PANEL_PADDING = 12;
  private static final int CLASSIC_CRAFTING_PANEL_WIDTH = 420;
  private static final int CLASSIC_CRAFTING_PANEL_HEIGHT = 420;

  private final Measurement measurement;
  private final int titleBaseline;
  private final int leftTitleX;
  private final int craftingTitleX;
  private final GridHitTest.Grid<CraftingInventorySide> leftGrid;
  private final Rectangle leftPanelBounds;
  private final Rectangle rightPanelBounds;

  private CraftingDialogLayoutState(
      Measurement measurement,
      int titleBaseline,
      int leftTitleX,
      int craftingTitleX,
      GridHitTest.Grid<CraftingInventorySide> leftGrid,
      Rectangle leftPanelBounds,
      Rectangle rightPanelBounds) {
    this.measurement = measurement;
    this.titleBaseline = titleBaseline;
    this.leftTitleX = leftTitleX;
    this.craftingTitleX = craftingTitleX;
    this.leftGrid = leftGrid;
    this.leftPanelBounds = leftPanelBounds;
    this.rightPanelBounds = rightPanelBounds;
  }

  static Measurement measure(Item[] targetSlots) {
    int leftColumns = InventoryGridRenderer.columnsFor(targetSlots);
    int leftRows = InventoryGridRenderer.rowsFor(targetSlots, leftColumns);
    int leftGridWidth = InventoryGridRenderer.gridWidth(leftColumns);
    int leftGridHeight = InventoryGridRenderer.gridHeight(leftRows);

    int rightPanelWidth = CLASSIC_CRAFTING_PANEL_WIDTH;
    int rightPanelHeight =
        Math.max(CLASSIC_CRAFTING_PANEL_HEIGHT, leftGridHeight + 2 * PANEL_PADDING);

    int totalContentWidth = leftGridWidth + PANEL_GAP + rightPanelWidth;
    int dialogWidth =
        Math.max(DEFAULT_WIDTH, totalContentWidth + 2 * DialogFrameMetrics.PADDING);

    int dialogHeight =
        Math.max(
            DEFAULT_HEIGHT,
            120
                + Math.max(leftGridHeight + 2 * PANEL_PADDING, rightPanelHeight)
                + DialogFrameMetrics.PADDING);

    return new Measurement(
        dialogWidth,
        dialogHeight,
        totalContentWidth,
        leftColumns,
        leftGridWidth,
        leftGridHeight,
        rightPanelWidth,
        rightPanelHeight);
  }

  static CraftingDialogLayoutState create(
      int dialogX,
      int contentY,
      FontMetrics fontMetrics,
      Measurement measurement,
      Item[] visibleTargetSlots) {
    int titleBaseline = contentY + fontMetrics.getAscent();
    int leftStartX = dialogX + (measurement.dialogWidth() - measurement.totalContentWidth()) / 2;
    int rightPanelX = leftStartX + measurement.leftGridWidth() + PANEL_GAP;
    int gridTop = titleBaseline + PANEL_HEADER_GAP + InventoryGridRenderer.GRID_TOP_GAP;

    Rectangle leftPanelBounds =
        InventoryPanelRenderer.panelBounds(
            leftStartX,
            gridTop,
            measurement.leftGridWidth(),
            measurement.leftGridHeight(),
            PANEL_PADDING);

    Rectangle rightPanelBounds =
        new Rectangle(
            rightPanelX,
            gridTop - PANEL_PADDING,
            measurement.rightPanelWidth(),
            measurement.rightPanelHeight());

    GridHitTest.Grid<CraftingInventorySide> leftGrid =
        new GridHitTest.Grid<>(
            CraftingInventorySide.TARGET,
            leftStartX,
            gridTop,
            measurement.leftColumns(),
            visibleTargetSlots);

    return new CraftingDialogLayoutState(
        measurement,
        titleBaseline,
        leftStartX,
        rightPanelX + PANEL_PADDING,
        leftGrid,
        leftPanelBounds,
        rightPanelBounds);
  }

  int dialogWidth() {
    return measurement.dialogWidth();
  }

  int dialogHeight() {
    return measurement.dialogHeight();
  }

  int titleBaseline() {
    return titleBaseline;
  }

  int leftTitleX() {
    return leftTitleX;
  }

  int craftingTitleX() {
    return craftingTitleX;
  }

  GridHitTest.Grid<CraftingInventorySide> leftGrid() {
    return leftGrid;
  }

  Rectangle leftPanelBounds() {
    return leftPanelBounds;
  }

  Rectangle rightPanelBounds() {
    return rightPanelBounds;
  }

  record Measurement(
      int dialogWidth,
      int dialogHeight,
      int totalContentWidth,
      int leftColumns,
      int leftGridWidth,
      int leftGridHeight,
      int rightPanelWidth,
      int rightPanelHeight) {}
}
