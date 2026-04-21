package contrib.hud.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import contrib.item.Item;
import org.junit.jupiter.api.Test;

/** Tests for {@link InventoryDragController}. */
public class InventoryDragControllerTest {

  /** Drop target resolution returns the hovered slot when the active drag is accepted. */
  @Test
  public void dropTargetAtAcceptedHoveredSlot() {
    InventoryDragController<Side> controller = draggingController();
    GridHitTest.Slot<Side> target = new GridHitTest.Slot<>(Side.RIGHT, 1);

    GridHitTest.Slot<Side> result =
        controller.dropTargetAt(10, 20, (mouseX, mouseY) -> target, this::differentSide);

    assertEquals(target, result);
  }

  /** Drop target resolution ignores hovered slots rejected by the target filter. */
  @Test
  public void dropTargetAtRejectedHoveredSlot() {
    InventoryDragController<Side> controller = draggingController();
    GridHitTest.Slot<Side> target = new GridHitTest.Slot<>(Side.LEFT, 1);

    GridHitTest.Slot<Side> result =
        controller.dropTargetAt(10, 20, (mouseX, mouseY) -> target, this::differentSide);

    assertNull(result);
  }

  /** Drop target resolution returns null when no drag is active. */
  @Test
  public void dropTargetAtWithoutDrag() {
    InventoryDragController<Side> controller = InventoryDragController.withDistanceThreshold(4);
    GridHitTest.Slot<Side> target = new GridHitTest.Slot<>(Side.RIGHT, 1);

    GridHitTest.Slot<Side> result =
        controller.dropTargetAt(10, 20, (mouseX, mouseY) -> target, this::differentSide);

    assertNull(result);
  }

  private InventoryDragController<Side> draggingController() {
    InventoryDragController<Side> controller = InventoryDragController.withDistanceThreshold(4);
    GridHitTest.Slot<Side> source = new GridHitTest.Slot<>(Side.LEFT, 0);
    Item item = mock(Item.class);

    controller.update(true, 0, 0, (mouseX, mouseY) -> source, slot -> item);
    controller.update(true, 5, 0, (mouseX, mouseY) -> source, slot -> item);

    return controller;
  }

  private boolean differentSide(GridHitTest.Slot<Side> source, GridHitTest.Slot<Side> target) {
    return source.side() != target.side();
  }

  private enum Side {
    LEFT,
    RIGHT
  }
}
