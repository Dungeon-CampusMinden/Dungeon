package contrib.hud.inventory;

import contrib.components.InventoryComponent;
import contrib.entities.HeroController;
import contrib.hud.itemgrid.ItemGridHitTest;
import contrib.hud.itemgrid.ItemGridDragController;
import contrib.item.Item;
import core.Entity;
import core.Game;
import core.input.MouseButtons;
import core.network.messages.c2s.InputMessage;
import core.ui.StageHandle;
import core.utils.InputManager;
import core.utils.Vector2;
import java.util.List;
import java.util.stream.Stream;

/**
 * Represents an inventory dialog overlay used for managing and interacting with player or entity
 * inventories. The dialog supports rendering, input handling, and item manipulation functionality.
 *
 * <p>It provides visual feedback through hover tooltips, drag-and-drop mechanics, and item slot
 * highlights.
 *
 * <p>The class is intended to be used as a floating UI element for inventory management, fully
 * integrated with game systems.
 *
 * <p>Key features include:
 *
 * <ul>
 *   <li>Rendering the inventory UI and associated elements.
 *   <li>Drag-and-drop support for moving items between slots or dropping them.
 *   <li>Handling player input, including drag events and item slot interaction.
 *   <li>Visual feedback for hovered or targeted slots.
 * </ul>
 */
final class InventoryDialogOverlay
    extends BaseInventoryOverlay<InventoryDialogOverlay.InventorySide> {

  private static final int DEFAULT_WIDTH = 560;
  private static final int DEFAULT_HEIGHT = 430;

  private final String title;
  private final Entity owner;
  private final InventoryComponent inventory;
  private final boolean allowUseItems;

  private Integer pressedUseSlotIndex = null;
  private boolean rightButtonDownLastFrame = false;

  InventoryDialogOverlay(
      String title, Entity owner, InventoryComponent inventory, boolean allowUseItems) {
    super(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    this.title = (title == null || title.isBlank()) ? "Inventory" : title;
    this.owner = owner;
    this.inventory = inventory;
    this.allowUseItems = allowUseItems;
  }

  @Override
  protected InventoryDialogLayoutState.Measurement<InventorySide> measure() {
    Item[] slots = inventory.items();
    Item[] visibleSlots = visibleSlots(slots, InventorySide.PLAYER);

    return InventoryDialogLayoutState.measure(
        DEFAULT_WIDTH,
        DEFAULT_HEIGHT,
        0,
        false,
        List.of(
            InventoryDialogLayoutState.PanelSpec.of(
                InventorySide.PLAYER, title, slots, visibleSlots)));
  }

  @Override
  protected String dialogTitle() {
    return title;
  }

  @Override
  protected void handleInput(List<ItemGridHitTest.Grid<InventorySide>> grids) {
    if (!allowUseItems) {
      resetInteractionState();
      return;
    }

    StageHandle stage = Game.stage().orElse(null);
    if (stage == null) {
      resetInteractionState();
      return;
    }

    handleLeftDragInput(grids);
    int mouseX = stage.mouseX();
    int mouseY = stage.mouseY();
    handleRightUseInput(grids, mouseX, mouseY);
  }

  private void handleLeftDragInput(List<ItemGridHitTest.Grid<InventorySide>> grids) {
    handlePrimaryInput(grids, this::handleDraggedRelease, null);
  }

  private void handleRightUseInput(
    List<ItemGridHitTest.Grid<InventorySide>> grids, int mouseX, int mouseY) {
    boolean rightButtonDown = InputManager.isButtonPressed(MouseButtons.RIGHT);

    if (dragController.isDragging()) {
      rightButtonDownLastFrame = rightButtonDown;
      return;
    }

    if (rightButtonDown && !rightButtonDownLastFrame) {
      ItemGridHitTest.Slot<InventorySide> slot = findSlotSelection(grids, mouseX, mouseY);
      pressedUseSlotIndex = slot == null ? null : slot.slotIndex();
    }

    if (!rightButtonDown && rightButtonDownLastFrame) {
      ItemGridHitTest.Slot<InventorySide> releasedSlot = findSlotSelection(grids, mouseX, mouseY);
      int releasedSlotIndex = releasedSlot == null ? -1 : releasedSlot.slotIndex();

      Integer previouslyPressedSlot = pressedUseSlotIndex;
      pressedUseSlotIndex = null;

      if (previouslyPressedSlot != null && previouslyPressedSlot == releasedSlotIndex) {
        HeroController.useItem(owner, releasedSlotIndex);
      }
    }

    rightButtonDownLastFrame = rightButtonDown;
  }

  private void handleDraggedRelease(
      ItemGridDragController.DragState<InventorySide> completedDrag,
      ItemGridHitTest.Slot<InventorySide> releasedSlot,
      int mouseX,
      int mouseY) {
    int sourceSlot = completedDrag.source().slotIndex();
    int releasedSlotIndex = releasedSlot == null ? -1 : releasedSlot.slotIndex();

    if (releasedSlotIndex >= 0 && releasedSlotIndex != sourceSlot) {
      moveDraggedItem(sourceSlot, releasedSlotIndex);
      return;
    }

    if (!bounds().contains(mouseX, mouseY)) {
      dropDraggedItem(sourceSlot);
    }
  }

  private void moveDraggedItem(int sourceSlot, int targetSlot) {
    int encodedSourceSlot = encodePlayerInventorySlot(sourceSlot);
    int encodedTargetSlot = encodePlayerInventorySlot(targetSlot);

    if (Game.network().isServer()) {
      HeroController.moveItem(owner, encodedSourceSlot, encodedTargetSlot);
    } else {
      Game.network()
          .send(
              (short) 0,
              new InputMessage(
                  InputMessage.Action.INV_MOVE, Vector2.of(encodedSourceSlot, encodedTargetSlot)),
              true);
    }
  }

  private void dropDraggedItem(int sourceSlot) {
    if (Game.network().isServer()) {
      HeroController.dropItem(owner, inventory, sourceSlot);
    } else {
      Game.network()
          .send(
              (short) 0,
              new InputMessage(InputMessage.Action.INV_DROP, Vector2.of(sourceSlot, 0)),
              true);
    }
  }

  @Override
  protected Item itemOf(ItemGridHitTest.Slot<InventorySide> slot) {
    if (slot == null) {
      return null;
    }

    return inventory.get(slot.slotIndex()).orElse(null);
  }

  @Override
  protected ItemGridDragController.DropTargetFilter<InventorySide> dropTargetFilter() {
    return (source, target) -> target.slotIndex() != source.slotIndex();
  }

  private static int encodePlayerInventorySlot(int slot) {
    return (-slot) - 1;
  }

  private void resetInteractionState() {
    pressedUseSlotIndex = null;
    rightButtonDownLastFrame = false;
    resetDragState();
  }

  @Override
  public Stream<InventoryComponent> inventoryComponents() {
    return Stream.of(inventory);
  }

  enum InventorySide {
    PLAYER
  }
}
