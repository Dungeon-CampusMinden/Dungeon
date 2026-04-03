package contrib.crafting;

import contrib.item.Item;
import java.util.ArrayList;
import java.util.List;

/**
 * Backend-neutral layout helper for crafting dialogs.
 *
 * <p>This class contains only geometry calculations for the crafting input row and the result row.
 * It does not depend on any rendering backend and can therefore be reused by libGDX and
 * LITIENGINE-based crafting UIs alike.
 */
public final class CraftingDialogLayout {

  private static final int ITEM_GAP = 10;

  // X coordinate of the center of the input item row.
  private static final float INPUT_ITEMS_X = 0.5f;

  // Y coordinate of the bottom edge of the input item row.
  private static final float INPUT_ITEMS_Y = 0.775f;

  // The size is based on the height of the crafting GUI and items are always square.
  private static final float INPUT_ITEMS_MAX_SIZE = 0.2f;

  // X coordinate of the center of the result item row.
  private static final float RESULT_ITEM_X = 0.5f;

  // Y coordinate of the bottom edge of the result item row.
  private static final float RESULT_ITEM_Y = 0.219f;

  // The size is based on the height of the crafting GUI and items are always square.
  private static final float RESULT_ITEM_MAX_SIZE = 0.1f;

  /**
   * Calculates the visible crafting input slots for the given dialog bounds.
   *
   * @param craftingSlots crafting inventory slots
   * @param dialogX dialog x
   * @param dialogY dialog y
   * @param dialogWidth dialog width
   * @param dialogHeight dialog height
   * @return positioned visible crafting slots
   */
  public List<SlotBounds> visibleCraftingSlots(
    Item[] craftingSlots, int dialogX, int dialogY, int dialogWidth, int dialogHeight) {
    if (craftingSlots == null || craftingSlots.length == 0) {
      return List.of();
    }

    List<Integer> visibleIndices = new ArrayList<>();
    for (int i = 0; i < craftingSlots.length; i++) {
      if (craftingSlots[i] != null) {
        visibleIndices.add(i);
      }
    }

    if (visibleIndices.isEmpty()) {
      return List.of();
    }

    int size = craftingItemSize(visibleIndices.size(), dialogWidth, dialogHeight);
    int rowWidth = rowWidth(visibleIndices.size(), size);
    int startX = dialogX + Math.round(dialogWidth * INPUT_ITEMS_X) - rowWidth / 2;
    int startY = dialogY + Math.round(dialogHeight * INPUT_ITEMS_Y);

    List<SlotBounds> bounds = new ArrayList<>(visibleIndices.size());
    for (int i = 0; i < visibleIndices.size(); i++) {
      int itemX = startX + ITEM_GAP * (i + 1) + size * i;
      bounds.add(new SlotBounds(visibleIndices.get(i), itemX, startY, size));
    }

    return List.copyOf(bounds);
  }

  /**
   * Calculates the result item slots for the given dialog bounds.
   *
   * @param resultItems result items to preview
   * @param dialogX dialog x
   * @param dialogY dialog y
   * @param dialogWidth dialog width
   * @param dialogHeight dialog height
   * @return positioned result slots
   */
  public List<ItemBounds> resultSlots(
    Item[] resultItems, int dialogX, int dialogY, int dialogWidth, int dialogHeight) {
    if (resultItems == null || resultItems.length == 0) {
      return List.of();
    }

    int size = resultItemSize(resultItems.length, dialogWidth, dialogHeight);
    int rowWidth = rowWidth(resultItems.length, size);
    int startX = dialogX + Math.round(dialogWidth * RESULT_ITEM_X) - rowWidth / 2;
    int startY = dialogY + Math.round(dialogHeight * RESULT_ITEM_Y);

    List<ItemBounds> bounds = new ArrayList<>(resultItems.length);
    for (int i = 0; i < resultItems.length; i++) {
      int itemX = startX + ITEM_GAP * (i + 1) + size * i;
      bounds.add(new ItemBounds(itemX, startY, size));
    }

    return List.copyOf(bounds);
  }

  private int craftingItemSize(int visibleItemCount, int dialogWidth, int dialogHeight) {
    return Math.min(
      Math.round(dialogHeight * INPUT_ITEMS_MAX_SIZE),
      (dialogWidth - visibleItemCount * ITEM_GAP) / visibleItemCount);
  }

  private int resultItemSize(int resultCount, int dialogWidth, int dialogHeight) {
    return Math.min(
      Math.round(dialogHeight * RESULT_ITEM_MAX_SIZE),
      (dialogWidth - resultCount * ITEM_GAP) / resultCount);
  }

  private int rowWidth(int count, int itemSize) {
    return count * itemSize + (count + 1) * ITEM_GAP;
  }

  /** Positioned visible crafting slot. */
  public record SlotBounds(int slotIndex, int x, int y, int size) {
    public boolean contains(int px, int py) {
      return px >= x && px <= x + size && py >= y && py <= y + size;
    }
  }

  /** Positioned result item preview slot. */
  public record ItemBounds(int x, int y, int size) {}
}
