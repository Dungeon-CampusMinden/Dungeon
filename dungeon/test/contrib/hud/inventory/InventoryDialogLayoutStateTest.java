package contrib.hud.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import contrib.hud.dialogs.frame.DialogFrameMetrics;
import contrib.hud.itemgrid.InventoryGridRenderer;
import contrib.item.Item;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.image.BufferedImage;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Tests for {@link InventoryDialogLayoutState}. */
public class InventoryDialogLayoutStateTest {

  /** Single-panel inventories keep the legacy default size for a normal six-slot layout. */
  @Test
  public void measureSinglePanelUsesDefaultDialogSize() {
    int gridWidth = InventoryGridRenderer.gridWidth(6);
    InventoryDialogLayoutState.Measurement<TestSide> measurement =
        InventoryDialogLayoutState.measure(
            560,
            430,
            0,
            false,
            List.of(
                InventoryDialogLayoutState.PanelSpec.of(
                    TestSide.LEFT, "Inventory", new Item[6], new Item[6])));

    assertEquals(
        Math.max(
            560,
            gridWidth
                + 2 * DialogFrameMetrics.PADDING
                + 2 * InventoryDialogLayoutState.PANEL_PADDING),
        measurement.dialogWidth());
    assertEquals(430, measurement.dialogHeight());
    assertEquals(gridWidth, measurement.totalGridWidth());
  }

  /** Dual-panel layouts position both grids from the measured shared content width. */
  @Test
  public void createPositionsDualPanelsFromSharedMeasurement() {
    FontMetrics fontMetrics = fontMetrics();
    InventoryDialogLayoutState.Measurement<TestSide> measurement =
        InventoryDialogLayoutState.measure(
            1100,
            470,
            34,
            true,
            List.of(
                InventoryDialogLayoutState.PanelSpec.of(
                    TestSide.LEFT, "Left", new Item[6], new Item[6]),
                InventoryDialogLayoutState.PanelSpec.of(
                    TestSide.RIGHT, "Right", new Item[4], new Item[4])));

    InventoryDialogLayoutState<TestSide> state =
        InventoryDialogLayoutState.create(100, 80, fontMetrics, measurement);

    int expectedLeftX = 100 + (measurement.dialogWidth() - measurement.totalGridWidth()) / 2;
    int expectedTitleBaseline = 80 + fontMetrics.getAscent();
    int expectedGridTop =
        expectedTitleBaseline
            + InventoryDialogLayoutState.PANEL_HEADER_GAP
            + InventoryGridRenderer.GRID_TOP_GAP;
    int expectedRightX =
        expectedLeftX + InventoryGridRenderer.gridWidth(6) + measurement.panelGap();

    assertEquals(expectedTitleBaseline, state.titleBaseline());
    assertEquals(expectedLeftX, state.panels().getFirst().grid().startX());
    assertEquals(expectedGridTop, state.panels().getFirst().grid().startY());
    assertEquals(TestSide.LEFT, state.panels().getFirst().grid().side());
    assertEquals(expectedLeftX - InventoryDialogLayoutState.PANEL_PADDING, state.panels().getFirst().panelBounds().x);
    assertEquals(expectedRightX, state.panels().get(1).grid().startX());
    assertEquals(TestSide.RIGHT, state.panels().get(1).grid().side());
  }

  private FontMetrics fontMetrics() {
    BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    return image.createGraphics().getFontMetrics(new Font("Dialog", Font.PLAIN, 12));
  }

  private enum TestSide {
    LEFT,
    RIGHT
  }
}
