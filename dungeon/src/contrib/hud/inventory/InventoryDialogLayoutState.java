package contrib.hud.inventory;

import contrib.hud.itemgrid.GridHitTest;
import contrib.hud.itemgrid.InventoryGridRenderer;
import contrib.hud.itemgrid.InventoryPanelRenderer;
import contrib.hud.renderers.DialogFrameRenderer;
import contrib.item.Item;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/** Shared measurement and layout state for inventory dialog overlays. */
final class InventoryDialogLayoutState<S> {

  static final int PANEL_PADDING = 8;
  static final int PANEL_HEADER_GAP = 8;

  private static final int BASE_VERTICAL_CHROME =
      96 + 2 * PANEL_PADDING + DialogFrameRenderer.PADDING;
  private static final int TITLED_VERTICAL_CHROME =
      108 + 2 * PANEL_PADDING + DialogFrameRenderer.PADDING;

  private final boolean showPanelTitles;
  private final int titleBaseline;
  private final List<PanelLayout<S>> panels;

  private InventoryDialogLayoutState(
      boolean showPanelTitles, int titleBaseline, List<PanelLayout<S>> panels) {
    this.showPanelTitles = showPanelTitles;
    this.titleBaseline = titleBaseline;
    this.panels = panels;
  }

  static <S> Measurement<S> measure(
      int defaultWidth,
      int defaultHeight,
      int panelGap,
      boolean showPanelTitles,
      List<PanelSpec<S>> panels) {
    int totalGridWidth =
        panels.stream().mapToInt(PanelSpec::gridWidth).sum()
            + Math.max(0, panels.size() - 1) * panelGap;
    int maxGridHeight = panels.stream().mapToInt(PanelSpec::gridHeight).max().orElse(0);

    int dialogWidth =
        Math.max(
            defaultWidth,
            totalGridWidth + 2 * DialogFrameRenderer.PADDING + 2 * PANEL_PADDING);
    int dialogHeight =
        Math.max(
            defaultHeight,
            (showPanelTitles ? TITLED_VERTICAL_CHROME : BASE_VERTICAL_CHROME) + maxGridHeight);

    return new Measurement<>(dialogWidth, dialogHeight, totalGridWidth, panelGap, showPanelTitles, panels);
  }

  static <S> InventoryDialogLayoutState<S> create(
      int dialogX, int contentY, FontMetrics fontMetrics, Measurement<S> measurement) {
    int titleBaseline = contentY + (measurement.showPanelTitles() ? fontMetrics.getAscent() : 0);
    int gridTop = titleBaseline + PANEL_HEADER_GAP + InventoryGridRenderer.GRID_TOP_GAP;
    int panelStartX =
        dialogX + (measurement.dialogWidth() - measurement.totalGridWidth()) / 2;

    List<PanelLayout<S>> layouts = new ArrayList<>();
    for (PanelSpec<S> panel : measurement.panels()) {
      layouts.add(
          new PanelLayout<>(
              panel.title(),
              InventoryPanelRenderer.panelBounds(
                  panelStartX, gridTop, panel.gridWidth(), panel.gridHeight(), PANEL_PADDING),
              new GridHitTest.Grid<>(
                  panel.side(), panelStartX, gridTop, panel.columns(), panel.visibleSlots())));
      panelStartX += panel.gridWidth() + measurement.panelGap();
    }

    return new InventoryDialogLayoutState<>(
        measurement.showPanelTitles(), titleBaseline, layouts);
  }

  boolean showPanelTitles() {
    return showPanelTitles;
  }

  int titleBaseline() {
    return titleBaseline;
  }

  List<PanelLayout<S>> panels() {
    return panels;
  }

  List<GridHitTest.Grid<S>> grids() {
    return panels.stream().map(PanelLayout::grid).toList();
  }

  /** Measured panel geometry independent of a concrete dialog position. */
  record PanelSpec<S>(
      S side,
      String title,
      Item[] visibleSlots,
      int columns,
      int gridWidth,
      int gridHeight) {

    static <S> PanelSpec<S> of(S side, String title, Item[] slots, Item[] visibleSlots) {
      int columns = InventoryGridRenderer.columnsFor(slots);
      int rows = InventoryGridRenderer.rowsFor(slots, columns);
      return new PanelSpec<>(
          side,
          title,
          visibleSlots,
          columns,
          InventoryGridRenderer.gridWidth(columns),
          InventoryGridRenderer.gridHeight(rows));
    }
  }

  /** Complete dialog measurement for a set of inventory panels. */
  record Measurement<S>(
      int dialogWidth,
      int dialogHeight,
      int totalGridWidth,
      int panelGap,
      boolean showPanelTitles,
      List<PanelSpec<S>> panels) {}

  /** Concrete panel layout with positioned bounds and hit-test grid. */
  record PanelLayout<S>(String title, Rectangle panelBounds, GridHitTest.Grid<S> grid) {}
}
