package contrib.hud.utils;

import contrib.hud.renderers.InventoryGridRenderer;
import contrib.item.Item;
import java.awt.Rectangle;
import java.util.List;
import java.util.Objects;

/** Utility methods and value objects for hit-testing inventory-like slot layouts. */
public final class GridHitTest {

  private GridHitTest() {}

  /**
   * Finds a slot at the given screen position.
   *
   * @param mouseX the mouse x coordinate
   * @param mouseY the mouse y coordinate
   * @param grids regular inventory grids to inspect first
   * @param boundedSlots additional custom slot rectangles to inspect after grids
   * @param <S> logical side type of the slot
   * @return the matching slot, or {@code null} when no slot contains the position
   */
  public static <S> Slot<S> findSlotAt(
      int mouseX, int mouseY, List<Grid<S>> grids, List<BoundedSlot<S>> boundedSlots) {
    Slot<S> gridSlot = findGridSlotAt(mouseX, mouseY, grids);
    if (gridSlot != null) {
      return gridSlot;
    }

    return findBoundedSlotAt(mouseX, mouseY, boundedSlots);
  }

  /**
   * Finds a slot in regular inventory grids at the given screen position.
   *
   * @param mouseX the mouse x coordinate
   * @param mouseY the mouse y coordinate
   * @param grids regular inventory grids to inspect
   * @param <S> logical side type of the slot
   * @return the matching slot, or {@code null} when no grid slot contains the position
   */
  public static <S> Slot<S> findGridSlotAt(int mouseX, int mouseY, List<Grid<S>> grids) {
    for (Grid<S> grid : grids) {
      int slotIndex = grid.findSlotIndexAt(mouseX, mouseY);
      if (slotIndex >= 0) {
        return new Slot<>(grid.side(), slotIndex);
      }
    }

    return null;
  }

  /**
   * Finds a custom bounded slot at the given screen position.
   *
   * @param mouseX the mouse x coordinate
   * @param mouseY the mouse y coordinate
   * @param boundedSlots custom slot rectangles to inspect
   * @param <S> logical side type of the slot
   * @return the matching slot, or {@code null} when no custom slot contains the position
   */
  public static <S> Slot<S> findBoundedSlotAt(
      int mouseX, int mouseY, List<BoundedSlot<S>> boundedSlots) {
    for (BoundedSlot<S> slot : boundedSlots) {
      if (slot.contains(mouseX, mouseY)) {
        return new Slot<>(slot.side(), slot.slotIndex());
      }
    }

    return null;
  }

  /**
   * Finds the bounds of a slot in the given regular inventory grids.
   *
   * @param slot the slot to locate
   * @param grids regular inventory grids to inspect
   * @param <S> logical side type of the slot
   * @return the slot bounds, or {@code null} when the slot is not part of these grids
   */
  public static <S> Rectangle boundsOf(Slot<S> slot, List<Grid<S>> grids) {
    if (slot == null) {
      return null;
    }

    for (Grid<S> grid : grids) {
      if (Objects.equals(grid.side(), slot.side())
          && slot.slotIndex() >= 0
          && slot.slotIndex() < grid.slots().length) {
        return grid.slotBounds(slot.slotIndex());
      }
    }

    return null;
  }

  /**
   * Describes a regular inventory grid.
   *
   * @param side the logical side represented by this grid
   * @param startX the x coordinate of the grid's top-left corner
   * @param startY the y coordinate of the grid's top-left corner
   * @param columns the number of columns in the grid
   * @param slots the rendered slot contents
   * @param <S> logical side type of the slot
   */
  public record Grid<S>(S side, int startX, int startY, int columns, Item[] slots) {

    /**
     * Finds a slot index at the given screen position.
     *
     * @param mouseX the mouse x coordinate
     * @param mouseY the mouse y coordinate
     * @return the matching slot index, or {@code -1}
     */
    public int findSlotIndexAt(int mouseX, int mouseY) {
      return InventoryGridRenderer.findSlotIndexAt(mouseX, mouseY, slots, startX, startY, columns);
    }

    /**
     * Calculates the screen bounds of a slot.
     *
     * @param slotIndex the slot index
     * @return the slot bounds
     */
    public Rectangle slotBounds(int slotIndex) {
      return InventoryGridRenderer.slotBounds(slotIndex, startX, startY, columns);
    }
  }

  /**
   * Identifies a slot on a logical inventory side.
   *
   * @param side the logical inventory side
   * @param slotIndex the zero-based slot index
   * @param <S> logical side type of the slot
   */
  public record Slot<S>(S side, int slotIndex) {}

  /**
   * Describes a slot with explicit screen bounds.
   *
   * @param side the logical inventory side
   * @param slotIndex the zero-based slot index
   * @param bounds the screen bounds of the slot
   * @param <S> logical side type of the slot
   */
  public record BoundedSlot<S>(S side, int slotIndex, Rectangle bounds) {

    /**
     * Checks whether a screen position is inside this slot.
     *
     * @param mouseX the mouse x coordinate
     * @param mouseY the mouse y coordinate
     * @return true when the coordinates are inside this slot
     */
    public boolean contains(int mouseX, int mouseY) {
      return bounds.contains(mouseX, mouseY);
    }
  }
}
