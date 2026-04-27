package contrib.hud.crafting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import contrib.components.InventoryComponent;
import contrib.hud.crafting.input.CraftingDragDropController;
import contrib.hud.crafting.render.CraftingTooltipRenderer;
import contrib.item.Item;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Tests for {@link CraftingTooltipRenderer}. */
public class CraftingTooltipRendererTest {

  /** Result hit-testing returns the hovered result item. */
  @Test
  public void resultItemAtReturnsHoveredResultItem() {
    Item first = mock(Item.class);
    Item second = mock(Item.class);
    CraftingTooltipRenderer controller = tooltipController();

    Item result =
        controller.resultItemAt(
            45,
            15,
            new Item[] {first, second},
            List.of(
                new CraftingDialogLayout.ItemBounds(10, 10, 20),
                new CraftingDialogLayout.ItemBounds(40, 10, 20)));

    assertSame(second, result);
  }

  /** Result hit-testing ignores positions outside all result item bounds. */
  @Test
  public void resultItemAtReturnsNullOutsideResultBounds() {
    Item item = mock(Item.class);
    CraftingTooltipRenderer controller = tooltipController();

    Item result =
        controller.resultItemAt(
            80, 80, new Item[] {item}, List.of(new CraftingDialogLayout.ItemBounds(10, 10, 20)));

    assertNull(result);
  }

  /** Result hit-testing never reads past the available result item array. */
  @Test
  public void resultItemIndexAtIgnoresBoundsWithoutResultItem() {
    CraftingTooltipRenderer controller = tooltipController();

    int result =
        controller.resultItemIndexAt(
            45,
            15,
            new Item[] {mock(Item.class)},
            List.of(
                new CraftingDialogLayout.ItemBounds(10, 10, 20),
                new CraftingDialogLayout.ItemBounds(40, 10, 20)));

    assertEquals(-1, result);
  }

  private CraftingTooltipRenderer tooltipController() {
    CraftingDialogController dialogController =
        new CraftingDialogController(new InventoryComponent(1), new InventoryComponent(1));
    return new CraftingTooltipRenderer(new CraftingDragDropController(dialogController));
  }
}
