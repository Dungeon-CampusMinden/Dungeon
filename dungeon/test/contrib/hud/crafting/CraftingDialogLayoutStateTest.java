package contrib.hud.crafting;

import static org.junit.jupiter.api.Assertions.assertEquals;

import contrib.hud.itemgrid.render.InventoryGridRenderer;
import contrib.item.Item;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;

/** Tests for {@link CraftingDialogLayoutState}. */
public class CraftingDialogLayoutStateTest {

  /** Measuring keeps the default dialog size for a normal inventory grid. */
  @Test
  public void measureUsesDefaultDialogSizeForNormalInventory() {
    CraftingDialogLayoutState.Measurement measurement =
        CraftingDialogLayoutState.measure(new Item[6]);

    assertEquals(CraftingDialogLayoutState.DEFAULT_WIDTH, measurement.dialogWidth());
    assertEquals(CraftingDialogLayoutState.DEFAULT_HEIGHT, measurement.dialogHeight());
    assertEquals(6, measurement.leftColumns());
    assertEquals(InventoryGridRenderer.gridWidth(6), measurement.leftGridWidth());
  }

  /** Creating layout state positions panels from measured content width. */
  @Test
  public void createPositionsInventoryAndCraftingPanels() {
    Item[] visibleTargetSlots = new Item[6];
    CraftingDialogLayoutState.Measurement measurement =
        CraftingDialogLayoutState.measure(visibleTargetSlots);

    CraftingDialogLayoutState state =
        CraftingDialogLayoutState.create(100, 80, fontMetrics(), measurement, visibleTargetSlots);

    int expectedLeftX = 100 + (measurement.dialogWidth() - measurement.totalContentWidth()) / 2;
    int expectedGridTop = state.titleBaseline() + 14 + InventoryGridRenderer.GRID_TOP_GAP;

    assertEquals(expectedLeftX, state.leftTitleX());
    assertEquals(expectedLeftX, state.leftGrid().startX());
    assertEquals(expectedGridTop, state.leftGrid().startY());
    assertEquals(CraftingInventorySide.TARGET, state.leftGrid().side());
    assertEquals(expectedLeftX - 12, state.leftPanelBounds().x);
    assertEquals(expectedLeftX + measurement.leftGridWidth() + 26 + 12, state.craftingTitleX());
  }

  private FontMetrics fontMetrics() {
    BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    return image.createGraphics().getFontMetrics(new Font("Dialog", Font.PLAIN, 12));
  }
}
