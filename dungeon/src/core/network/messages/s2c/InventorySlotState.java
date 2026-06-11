package core.network.messages.s2c;

/**
 * Immutable network representation of one inventory slot.
 *
 * @param slotIndex slot index in the inventory
 * @param item item state, or null when the slot is empty
 */
public record InventorySlotState(int slotIndex, ItemState item) {

  /**
   * Creates an inventory slot state.
   *
   * @param slotIndex slot index in the inventory
   * @param item item state, or null when the slot is empty
   */
  public InventorySlotState {
    if (slotIndex < 0) {
      throw new IllegalArgumentException("slotIndex must not be negative.");
    }
  }
}
