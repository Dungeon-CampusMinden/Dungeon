package contrib.hud.itemgrid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import contrib.item.Item;
import org.junit.jupiter.api.Test;

/** Tests for {@link ItemGridDragController}. */
public class ItemGridDragControllerTest {

  /** Drop target resolution returns the hovered slot when the active drag is accepted. */
  @Test
  public void dropTargetAtAcceptedHoveredSlot() {
    ItemGridDragController<Side> controller = draggingController();
    ItemGridHitTest.Slot<Side> target = new ItemGridHitTest.Slot<>(Side.RIGHT, 1);

    ItemGridHitTest.Slot<Side> result =
        controller.dropTargetAt(10, 20, (mouseX, mouseY) -> target, this::differentSide);

    assertEquals(target, result);
  }

  /** Drop target resolution ignores hovered slots rejected by the target filter. */
  @Test
  public void dropTargetAtRejectedHoveredSlot() {
    ItemGridDragController<Side> controller = draggingController();
    ItemGridHitTest.Slot<Side> target = new ItemGridHitTest.Slot<>(Side.LEFT, 1);

    ItemGridHitTest.Slot<Side> result =
        controller.dropTargetAt(10, 20, (mouseX, mouseY) -> target, this::differentSide);

    assertNull(result);
  }

  /** Drop target resolution returns null when no drag is active. */
  @Test
  public void dropTargetAtWithoutDrag() {
    ItemGridDragController<Side> controller = ItemGridDragController.withDistanceThreshold(4);
    ItemGridHitTest.Slot<Side> target = new ItemGridHitTest.Slot<>(Side.RIGHT, 1);

    ItemGridHitTest.Slot<Side> result =
        controller.dropTargetAt(10, 20, (mouseX, mouseY) -> target, this::differentSide);

    assertNull(result);
  }

  private ItemGridDragController<Side> draggingController() {
    ItemGridDragController<Side> controller = ItemGridDragController.withDistanceThreshold(4);
    ItemGridHitTest.Slot<Side> source = new ItemGridHitTest.Slot<>(Side.LEFT, 0);
    Item item = mock(Item.class);

    controller.update(true, 0, 0, (mouseX, mouseY) -> source, slot -> item);
    controller.update(true, 5, 0, (mouseX, mouseY) -> source, slot -> item);

    return controller;
  }

  private boolean differentSide(ItemGridHitTest.Slot<Side> source, ItemGridHitTest.Slot<Side> target) {
    return source.side() != target.side();
  }

  private enum Side {
    LEFT,
    RIGHT
  }
}
