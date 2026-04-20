package contrib.hud.utils;

import contrib.hud.renderers.InventoryGridRenderer;
import contrib.item.Item;
import core.Game;
import core.ui.StageHandle;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Objects;
import java.util.Optional;

/**
 * Tracks inventory drag state and provides shared rendering helpers for drag feedback.
 *
 * @param <S> logical side type of the slot
 */
public final class InventoryDragController<S> {

  /** Default translucent fill used by inventory drop targets. */
  public static final Color DEFAULT_DROP_FILL = new Color(88, 168, 116, 70);

  /** Default outline used by inventory drop targets. */
  public static final Color DEFAULT_DROP_OUTLINE = new Color(132, 214, 156, 210);

  private static final int DRAG_TARGET_INSET = 3;
  private static final int DRAG_TARGET_ARC = 8;

  private final int thresholdPx;
  private final DragStartCondition dragStartCondition;

  private GridHitTest.Slot<S> pressedSlot = null;
  private boolean buttonDownLastFrame = false;
  private int pressedMouseX = 0;
  private int pressedMouseY = 0;
  private DragState<S> dragState = null;

  private InventoryDragController(int thresholdPx, DragStartCondition dragStartCondition) {
    this.thresholdPx = thresholdPx;
    this.dragStartCondition = dragStartCondition;
  }

  /**
   * Creates a controller using squared-distance drag activation.
   *
   * @param thresholdPx the minimum pointer movement in pixels
   * @param <S> logical side type of the slot
   * @return a drag controller
   */
  public static <S> InventoryDragController<S> withDistanceThreshold(int thresholdPx) {
    return new InventoryDragController<>(
        thresholdPx,
        (deltaX, deltaY, threshold) -> deltaX * deltaX + deltaY * deltaY >= threshold * threshold);
  }

  /**
   * Creates a controller that starts dragging when either axis crosses the threshold.
   *
   * @param thresholdPx the minimum pointer movement in pixels
   * @param <S> logical side type of the slot
   * @return a drag controller
   */
  public static <S> InventoryDragController<S> withAxisThreshold(int thresholdPx) {
    return new InventoryDragController<>(
        thresholdPx,
        (deltaX, deltaY, threshold) ->
            Math.abs(deltaX) >= threshold || Math.abs(deltaY) >= threshold);
  }

  /**
   * Updates drag state for a single input frame.
   *
   * @param buttonDown whether the drag button is currently pressed
   * @param mouseX the current mouse x coordinate
   * @param mouseY the current mouse y coordinate
   * @param slotFinder resolves slots at pointer positions
   * @param itemResolver resolves items inside slots
   * @return a release event when the button was released this frame
   */
  public Optional<Release<S>> update(
      boolean buttonDown,
      int mouseX,
      int mouseY,
      SlotFinder<S> slotFinder,
      ItemResolver<S> itemResolver) {
    Optional<Release<S>> release = Optional.empty();

    if (buttonDown && !buttonDownLastFrame) {
      pressedSlot = slotFinder.findSlot(mouseX, mouseY);
      pressedMouseX = mouseX;
      pressedMouseY = mouseY;
      dragState = null;
    } else if (buttonDown) {
      maybeStartDrag(mouseX, mouseY, itemResolver);
    }

    if (!buttonDown && buttonDownLastFrame) {
      GridHitTest.Slot<S> releasedSlot = slotFinder.findSlot(mouseX, mouseY);
      release = Optional.of(new Release<>(pressedSlot, releasedSlot, dragState));

      pressedSlot = null;
      dragState = null;
    }

    buttonDownLastFrame = buttonDown;
    return release;
  }

  /**
   * Returns the currently active drag state.
   *
   * @return the active drag state, or {@code null}
   */
  public DragState<S> dragState() {
    return dragState;
  }

  /**
   * Checks whether an item is currently being dragged.
   *
   * @return true when a drag is active
   */
  public boolean isDragging() {
    return dragState != null;
  }

  /**
   * Returns the slot array to render while hiding the dragged source item.
   *
   * @param slots the original slot array
   * @param side the side represented by the slot array
   * @return either the original array or a cloned array with the dragged item hidden
   */
  public Item[] visibleSlots(Item[] slots, S side) {
    if (slots == null || dragState == null || dragState.source() == null) {
      return slots;
    }

    if (!Objects.equals(dragState.source().side(), side)) {
      return slots;
    }

    int sourceSlot = dragState.source().slotIndex();
    if (sourceSlot < 0 || sourceSlot >= slots.length) {
      return slots;
    }

    Item[] visibleSlots = slots.clone();
    visibleSlots[sourceSlot] = null;
    return visibleSlots;
  }

  /**
   * Draws the dragged item at the current pointer position.
   *
   * @param g the graphics context
   */
  public void drawDragPreview(Graphics2D g) {
    StageHandle stage = Game.stage().orElse(null);
    if (stage == null || dragState == null || dragState.item() == null) {
      return;
    }

    int previewX = stage.mouseX() - InventoryGridRenderer.SLOT_WIDTH / 2;
    int previewY = stage.mouseY() - InventoryGridRenderer.SLOT_HEIGHT / 2;

    InventoryGridRenderer.drawItemPreview(g, previewX, previewY, dragState.item());
  }

  /** Resets all pressed and dragged state. */
  public void reset() {
    pressedSlot = null;
    buttonDownLastFrame = false;
    dragState = null;
  }

  /**
   * Draws a rounded drop-target highlight inside the given bounds.
   *
   * @param g the graphics context
   * @param bounds the bounds to highlight
   * @param fill the highlight fill color
   * @param outline the highlight outline color
   */
  public static void drawDropHighlight(Graphics2D g, Rectangle bounds, Color fill, Color outline) {
    int insetX = bounds.x + DRAG_TARGET_INSET;
    int insetY = bounds.y + DRAG_TARGET_INSET;
    int insetWidth = bounds.width - 2 * DRAG_TARGET_INSET;
    int insetHeight = bounds.height - 2 * DRAG_TARGET_INSET;

    g.setColor(fill);
    g.fillRoundRect(insetX, insetY, insetWidth, insetHeight, DRAG_TARGET_ARC, DRAG_TARGET_ARC);

    g.setColor(outline);
    g.drawRoundRect(insetX, insetY, insetWidth, insetHeight, DRAG_TARGET_ARC, DRAG_TARGET_ARC);
  }

  private void maybeStartDrag(int mouseX, int mouseY, ItemResolver<S> itemResolver) {
    if (dragState != null || pressedSlot == null) {
      return;
    }

    int deltaX = mouseX - pressedMouseX;
    int deltaY = mouseY - pressedMouseY;
    if (!dragStartCondition.shouldStart(deltaX, deltaY, thresholdPx)) {
      return;
    }

    Item draggedItem = itemResolver.itemAt(pressedSlot);
    if (draggedItem == null) {
      pressedSlot = null;
      return;
    }

    dragState = new DragState<>(pressedSlot, draggedItem);
  }

  private interface DragStartCondition {
    boolean shouldStart(int deltaX, int deltaY, int thresholdPx);
  }

  /**
   * Resolves a slot at a screen position.
   *
   * @param <S> logical side type of the slot
   */
  @FunctionalInterface
  public interface SlotFinder<S> {

    /**
     * Finds a slot at the given screen position.
     *
     * @param mouseX the mouse x coordinate
     * @param mouseY the mouse y coordinate
     * @return the matching slot, or {@code null}
     */
    GridHitTest.Slot<S> findSlot(int mouseX, int mouseY);
  }

  /**
   * Resolves an item for a slot.
   *
   * @param <S> logical side type of the slot
   */
  @FunctionalInterface
  public interface ItemResolver<S> {

    /**
     * Gets the item in a slot.
     *
     * @param slot the slot to inspect
     * @return the item in the slot, or {@code null}
     */
    Item itemAt(GridHitTest.Slot<S> slot);
  }

  /**
   * Describes an active item drag.
   *
   * @param source the source slot
   * @param item the dragged item
   * @param <S> logical side type of the slot
   */
  public record DragState<S>(GridHitTest.Slot<S> source, Item item) {}

  /**
   * Describes a completed button release.
   *
   * @param pressedSlot the slot that was pressed, or {@code null}
   * @param releasedSlot the slot where the button was released, or {@code null}
   * @param completedDrag the completed drag, or {@code null} for a click
   * @param <S> logical side type of the slot
   */
  public record Release<S>(
      GridHitTest.Slot<S> pressedSlot,
      GridHitTest.Slot<S> releasedSlot,
      DragState<S> completedDrag) {}
}
