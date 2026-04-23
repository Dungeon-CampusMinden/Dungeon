package contrib.hud.itemgrid;

import contrib.item.Item;
import core.Game;
import core.ui.StageHandle;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/** Shared tooltip handling for inventory-like overlays. */
public final class InventoryTooltip {

  private InventoryTooltip() {}

  /**
   * Draws a tooltip for the hovered slot inside a hover area.
   *
   * @param g the graphics context
   * @param hoverArea the area where tooltips are allowed
   * @param slotResolver resolves a slot at the current mouse position
   * @param itemResolver resolves the item inside the hovered slot
   * @param <S> logical inventory side type
   * @return true if a tooltip was drawn
   */
  public static <S> boolean drawHoveredSlotTooltip(
      Graphics2D g,
      Rectangle hoverArea,
      SlotResolver<S> slotResolver,
      ItemResolver<S> itemResolver) {
    MousePosition mouse = mousePositionInside(hoverArea);
    if (mouse == null) {
      return false;
    }

    GridHitTest.Slot<S> hoveredSlot = slotResolver.findSlot(mouse.x(), mouse.y());
    if (hoveredSlot == null) {
      return false;
    }

    return drawTooltip(g, itemResolver.itemAt(hoveredSlot), mouse);
  }

  /**
   * Draws a tooltip for an item when the mouse is inside the given bounds.
   *
   * @param g the graphics context
   * @param hoverArea the item hover bounds
   * @param item the hovered item
   * @return true if a tooltip was drawn
   */
  public static boolean drawItemTooltip(Graphics2D g, Rectangle hoverArea, Item item) {
    MousePosition mouse = mousePositionInside(hoverArea);
    if (mouse == null) {
      return false;
    }

    return drawTooltip(g, item, mouse);
  }

  private static boolean drawTooltip(Graphics2D g, Item item, MousePosition mouse) {
    if (item == null) {
      return false;
    }

    ItemTooltipRenderer.drawTooltip(
        g, item, mouse.x(), mouse.y(), mouse.viewportWidth(), mouse.viewportHeight());
    return true;
  }

  private static MousePosition mousePositionInside(Rectangle hoverArea) {
    StageHandle stage = Game.stage().orElse(null);
    if (stage == null || hoverArea == null) {
      return null;
    }

    int mouseX = stage.mouseX();
    int mouseY = stage.mouseY();
    if (!hoverArea.contains(mouseX, mouseY)) {
      return null;
    }

    return new MousePosition(mouseX, mouseY, (int) stage.getWidth(), (int) stage.getHeight());
  }

  /**
   * Functional interface for resolving a slot in an inventory-like overlay
   * based on the current mouse position.
   *
   * @param <S> the type representing the logical inventory side
   */
  @FunctionalInterface
  public interface SlotResolver<S> {

    /**
     * Finds a slot at the given screen position.
     *
     * @param mouseX the mouse x coordinate
     * @param mouseY the mouse y coordinate
     * @return the slot, or {@code null}
     */
    GridHitTest.Slot<S> findSlot(int mouseX, int mouseY);
  }

  /**
   * A functional interface for resolving an item within a logical inventory slot.
   *
   * @param <S> the type representing a logical inventory side
   */
  @FunctionalInterface
  public interface ItemResolver<S> {

    /**
     * Gets the item in a slot.
     *
     * @param slot the slot to inspect
     * @return the item, or {@code null}
     */
    Item itemAt(GridHitTest.Slot<S> slot);
  }

  private record MousePosition(int x, int y, int viewportWidth, int viewportHeight) {}
}
