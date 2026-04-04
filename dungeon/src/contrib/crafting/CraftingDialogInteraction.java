package contrib.crafting;

import contrib.hud.inventory.ItemDragPayload;

/**
 * Backend-neutral interaction logic for crafting dialogs.
 *
 * <p>This class owns the semantic transfer behavior between the target inventory and the crafting
 * inventory. Concrete UI backends such as libGDX and LITIENGINE can delegate user interactions to
 * this class instead of implementing transfer rules themselves.
 */
public final class CraftingDialogInteraction {

  private final CraftingDialogController controller;

  /**
   * Creates a new crafting dialog interaction helper.
   *
   * @param controller the shared crafting dialog controller
   */
  public CraftingDialogInteraction(CraftingDialogController controller) {
    if (controller == null) {
      throw new IllegalArgumentException("controller must not be null");
    }
    this.controller = controller;
  }

  /**
   * Checks whether the given dragged payload can be accepted by the crafting input side.
   *
   * @param payload the dragged payload
   * @return true if the payload contains a transferable item
   */
  public boolean acceptsDraggedItem(ItemDragPayload payload) {
    return payload != null && payload.item() != null;
  }

  /**
   * Handles dropping an inventory item into the crafting inventory.
   *
   * <p>The crafting dialog accepts dragged items as input for the crafting side.
   *
   * @param payload the dragged payload
   * @return true if the transfer succeeded
   */
  public boolean handleDraggedItem(ItemDragPayload payload) {
    if (!acceptsDraggedItem(payload)) {
      return false;
    }

    return controller.transferByItem(
      CraftingDialogController.InventorySide.TARGET, payload.item());
  }

  /**
   * Handles a slot-based transfer triggered by click interaction.
   *
   * @param sourceSide the side from which the item should be transferred
   * @param slotIndex the clicked slot
   * @return true if the transfer succeeded
   */
  public boolean transferClickedSlot(
    CraftingDialogController.InventorySide sourceSide, int slotIndex) {
    return controller.transferBySlot(sourceSide, slotIndex);
  }

  /**
   * Handles a slot-to-slot transfer triggered by a drag/drop style interaction.
   *
   * <p>This method allows UI backends to express "move item from this exact source slot to that
   * exact target slot" without implementing crafting transfer rules themselves.
   *
   * @param sourceSide source inventory side
   * @param sourceSlotIndex source slot index
   * @param targetSide target inventory side
   * @param targetSlotIndex target slot index
   * @return true if the transfer succeeded
   */
  public boolean transferDroppedSlot(
    CraftingDialogController.InventorySide sourceSide,
    int sourceSlotIndex,
    CraftingDialogController.InventorySide targetSide,
    int targetSlotIndex) {
    return controller.transferBySlotToSlot(
      sourceSide, sourceSlotIndex, targetSide, targetSlotIndex);
  }
}
