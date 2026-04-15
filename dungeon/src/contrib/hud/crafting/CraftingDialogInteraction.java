package contrib.hud.crafting;

/**
 * A helper class for handling user interactions in the crafting dialog.
 *
 * <p>This class provides methods to translate UI interactions (clicks and drags) into
 * inventory transfer operations using the shared crafting dialog controller.
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
   * Handles a slot-based transfer triggered by click interaction.
   *
   * @param sourceSide the side from which the item should be transferred
   * @param slotIndex  the clicked slot
   */
  public void transferClickedSlot(
    CraftingDialogController.InventorySide sourceSide, int slotIndex) {
    controller.transferBySlot(sourceSide, slotIndex);
  }

  /**
   * Handles a slot-to-slot transfer triggered by drag and drop interaction.
   *
   * @param sourceSide the source inventory side
   * @param sourceSlotIndex the source slot index
   * @param targetSide the target inventory side
   * @param targetSlotIndex the target slot index
   */
  public void transferDroppedSlot(
    CraftingDialogController.InventorySide sourceSide,
    int sourceSlotIndex,
    CraftingDialogController.InventorySide targetSide,
    int targetSlotIndex) {
    controller.transferBySlotToSlot(
      sourceSide, sourceSlotIndex, targetSide, targetSlotIndex);
  }
}
