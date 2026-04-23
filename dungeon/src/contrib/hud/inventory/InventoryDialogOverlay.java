package contrib.hud.inventory;

import contrib.components.InventoryComponent;
import contrib.entities.HeroController;
import contrib.hud.renderers.DialogFrameRenderer;
import contrib.hud.renderers.InventoryGridRenderer;
import contrib.hud.renderers.InventoryPanelRenderer;
import contrib.hud.utils.GridHitTest;
import contrib.hud.utils.InventoryDragController;
import contrib.hud.utils.InventoryDropHandling;
import contrib.hud.utils.InventoryTooltip;
import contrib.item.Item;
import core.Entity;
import core.Game;
import core.input.MouseButtons;
import core.network.messages.c2s.InputMessage;
import core.ui.StageHandle;
import core.ui.overlay.BaseUiOverlay;
import core.utils.InputManager;
import core.utils.Vector2;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;
import java.util.Optional;
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
final class InventoryDialogOverlay extends BaseUiOverlay implements InventoryComponentProvider {

  private static final int DEFAULT_WIDTH = 560;
  private static final int DEFAULT_HEIGHT = 430;

  private static final int PANEL_PADDING = 8;
  private static final int PANEL_HEADER_GAP = 8;

  private static final int DRAG_THRESHOLD_PX = 8;

  private final String title;
  private final Entity owner;
  private final InventoryComponent inventory;
  private final boolean allowUseItems;
  private final InventoryDragController<InventorySide> dragController =
      InventoryDragController.withDistanceThreshold(DRAG_THRESHOLD_PX);

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
  public void render(Graphics2D g) {
    if (!visible) {
      return;
    }

    Item[] slots = inventory.items();
    Item[] visibleSlots = dragController.visibleSlots(slots, InventorySide.PLAYER);

    int columns = InventoryGridRenderer.columnsFor(slots);
    int rows = InventoryGridRenderer.rowsFor(slots, columns);

    int gridWidth = InventoryGridRenderer.gridWidth(columns);
    int gridHeight = InventoryGridRenderer.gridHeight(rows);

    width =
        Math.max(DEFAULT_WIDTH, 2 * DialogFrameRenderer.PADDING + gridWidth + 2 * PANEL_PADDING);

    height =
        Math.max(DEFAULT_HEIGHT, 96 + gridHeight + 2 * PANEL_PADDING + DialogFrameRenderer.PADDING);

    centerInIfUnpositioned(Game.windowWidth(), Game.windowHeight());

    int contentY;
    int startX;
    int gridTop;

    GridHitTest.Grid<InventorySide> grid;

    DialogFrameRenderer.RenderState state = DialogFrameRenderer.beginDialog(g);

    try {
      contentY = DialogFrameRenderer.drawFrameAndTitle(g, x, y, width, height, title);

      startX = x + (width - gridWidth) / 2;
      gridTop = contentY + PANEL_HEADER_GAP + InventoryGridRenderer.GRID_TOP_GAP;

      Rectangle panelBounds =
          InventoryPanelRenderer.panelBounds(
              startX, gridTop, gridWidth, gridHeight, PANEL_PADDING);

      InventoryPanelRenderer.drawPanelBackground(g, panelBounds);

      grid = new GridHitTest.Grid<>(InventorySide.PLAYER, startX, gridTop, columns, visibleSlots);

      InventoryGridRenderer.drawGrid(g, visibleSlots, startX, gridTop, columns);

      if (dragController.isDragging()) {
        GridHitTest.Slot<InventorySide> hoveredTarget = hoveredDropTarget(grid);
        if (hoveredTarget != null) {
          InventoryDropHandling.drawGridDropHighlight(g, hoveredTarget, grid);
        }
        dragController.drawDragPreview(g);
      } else {
        drawHoverTooltip(g, grid);
      }
    } finally {
      DialogFrameRenderer.finishDialog(g, state);
    }

    handleInput(grid);
  }

  private void drawHoverTooltip(Graphics2D g, GridHitTest.Grid<InventorySide> grid) {
    InventoryTooltip.drawHoveredSlotTooltip(
        g,
        bounds(),
        (mouseX, mouseY) -> findSlotSelection(grid, mouseX, mouseY),
        this::itemOf);
  }

  private void handleInput(GridHitTest.Grid<InventorySide> grid) {
    if (!allowUseItems) {
      resetInteractionState();
      return;
    }

    StageHandle stage = Game.stage().orElse(null);
    if (stage == null) {
      resetInteractionState();
      return;
    }

    handleLeftDragInput(grid);
    int mouseX = stage.mouseX();
    int mouseY = stage.mouseY();
    handleRightUseInput(grid, mouseX, mouseY);
  }

  private void handleLeftDragInput(GridHitTest.Grid<InventorySide> grid) {
    Optional<InventoryDragController.MouseUpdate<InventorySide>> update =
        dragController.updateFromPrimaryMouse(
            (slotMouseX, slotMouseY) -> findSlotSelection(grid, slotMouseX, slotMouseY),
            this::itemOf);

    if (update.isEmpty() || update.get().release().isEmpty()) {
      return;
    }

    InventoryDragController.MouseUpdate<InventorySide> mouseUpdate = update.get();
    InventoryDragController.Release<InventorySide> released = mouseUpdate.release().get();
    if (released.completedDrag() != null) {
      handleDraggedRelease(
          released.completedDrag(),
          released.releasedSlot(),
          mouseUpdate.mouseX(),
          mouseUpdate.mouseY());
    }
  }

  private void handleRightUseInput(GridHitTest.Grid<InventorySide> grid, int mouseX, int mouseY) {
    boolean rightButtonDown = InputManager.isButtonPressed(MouseButtons.RIGHT);

    if (dragController.isDragging()) {
      rightButtonDownLastFrame = rightButtonDown;
      return;
    }

    if (rightButtonDown && !rightButtonDownLastFrame) {
      GridHitTest.Slot<InventorySide> slot = findSlotSelection(grid, mouseX, mouseY);
      pressedUseSlotIndex = slot == null ? null : slot.slotIndex();
    }

    if (!rightButtonDown && rightButtonDownLastFrame) {
      GridHitTest.Slot<InventorySide> releasedSlot = findSlotSelection(grid, mouseX, mouseY);
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
      InventoryDragController.DragState<InventorySide> completedDrag,
      GridHitTest.Slot<InventorySide> releasedSlot,
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

  private GridHitTest.Slot<InventorySide> hoveredDropTarget(GridHitTest.Grid<InventorySide> grid) {
    return InventoryDropHandling.hoveredDropTarget(
        dragController,
        (mouseX, mouseY) -> findSlotSelection(grid, mouseX, mouseY),
        (source, target) -> target.slotIndex() != source.slotIndex());
  }

  private GridHitTest.Slot<InventorySide> findSlotSelection(
      GridHitTest.Grid<InventorySide> grid, int mouseX, int mouseY) {
    return GridHitTest.findGridSlotAt(mouseX, mouseY, List.of(grid));
  }

  private Item itemOf(GridHitTest.Slot<InventorySide> slot) {
    if (slot == null) {
      return null;
    }

    return inventory.get(slot.slotIndex()).orElse(null);
  }

  private static int encodePlayerInventorySlot(int slot) {
    return (-slot) - 1;
  }

  private void resetInteractionState() {
    pressedUseSlotIndex = null;
    rightButtonDownLastFrame = false;
    dragController.reset();
  }

  @Override
  public Stream<InventoryComponent> inventoryComponents() {
    return Stream.of(inventory);
  }

  private enum InventorySide {
    PLAYER
  }
}
