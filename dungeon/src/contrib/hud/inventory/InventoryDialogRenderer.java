package contrib.hud.inventory;

import contrib.hud.itemgrid.InventoryGridRenderer;
import contrib.hud.itemgrid.InventoryPanelRenderer;
import java.awt.Color;
import java.awt.Graphics2D;

/** Shared rendering pipeline for inventory dialog panels. */
final class InventoryDialogRenderer {

  private InventoryDialogRenderer() {}

  static <S> InventoryDialogLayoutState<S> draw(
      Graphics2D g,
      int dialogX,
      int contentY,
      InventoryDialogLayoutState.Measurement<S> measurement) {
    InventoryDialogLayoutState<S> layoutState =
        InventoryDialogLayoutState.create(dialogX, contentY, g.getFontMetrics(), measurement);

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
}
