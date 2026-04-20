package contrib.hud.inventory;

import contrib.components.InventoryComponent;
import contrib.entities.HeroController;
import contrib.hud.elements.InventoryComponentProvider;
import contrib.hud.renderers.DialogFrameRenderer;
import contrib.hud.renderers.InventoryGridRenderer;
import contrib.hud.renderers.ItemTooltipRenderer;
import contrib.hud.utils.GridHitTest;
import contrib.hud.utils.InventoryDragController;
import contrib.item.Item;
import core.Entity;
import core.Game;
import core.input.MouseButtons;
import core.network.messages.c2s.InputMessage;
import core.ui.StageHandle;
import core.ui.overlay.UiOverlay;
import core.utils.InputManager;
import core.utils.Vector2;
import java.awt.Color;
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
final class InventoryDialogOverlay implements UiOverlay, InventoryComponentProvider {

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

  private int x;
  private int y;
  private int width = DEFAULT_WIDTH;
  private int height = DEFAULT_HEIGHT;
  private boolean visible = true;

  private Integer pressedUseSlotIndex = null;
  private boolean rightButtonDownLastFrame = false;

  InventoryDialogOverlay(
      String title, Entity owner, InventoryComponent inventory, boolean allowUseItems) {
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

    if (x == 0 && y == 0) {
      x = (Game.windowWidth() - width) / 2;
      y = (Game.windowHeight() - height) / 2;
    }

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
          new Rectangle(
              startX - PANEL_PADDING,
              gridTop - PANEL_PADDING,
              gridWidth + 2 * PANEL_PADDING,
              gridHeight + 2 * PANEL_PADDING);

      drawPanelBackground(g, panelBounds.x, panelBounds.y, panelBounds.width, panelBounds.height);

      grid = new GridHitTest.Grid<>(InventorySide.PLAYER, startX, gridTop, columns, visibleSlots);

      InventoryGridRenderer.drawGrid(g, visibleSlots, startX, gridTop, columns);

      if (dragController.isDragging()) {
        GridHitTest.Slot<InventorySide> hoveredTarget = hoveredDropTarget(grid);
        if (hoveredTarget != null) {
          drawDropTargetHighlight(g, grid, hoveredTarget.slotIndex());
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
    StageHandle stage = Game.stage().orElse(null);
    if (stage == null) {
      return;
    }

    int mouseX = stage.mouseX();
    int mouseY = stage.mouseY();

    if (mouseX < x || mouseX > x + width || mouseY < y || mouseY > y + height) {
      return;
    }

    GridHitTest.Slot<InventorySide> hoveredSlot = findSlotSelection(grid, mouseX, mouseY);
    if (hoveredSlot == null) {
      return;
    }

    Item hoveredItem = inventory.get(hoveredSlot.slotIndex()).orElse(null);
    if (hoveredItem == null) {
      return;
    }

    ItemTooltipRenderer.drawTooltip(
        g, hoveredItem, mouseX, mouseY, (int) stage.getWidth(), (int) stage.getHeight());
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

    int mouseX = stage.mouseX();
    int mouseY = stage.mouseY();

    handleLeftDragInput(grid, mouseX, mouseY);
    handleRightUseInput(grid, mouseX, mouseY);
  }

  private void handleLeftDragInput(GridHitTest.Grid<InventorySide> grid, int mouseX, int mouseY) {
    boolean leftButtonDown = InputManager.isButtonPressed(MouseButtons.LEFT);
    Optional<InventoryDragController.Release<InventorySide>> release =
        dragController.update(
            leftButtonDown,
            mouseX,
            mouseY,
            (slotMouseX, slotMouseY) -> findSlotSelection(grid, slotMouseX, slotMouseY),
            this::itemOf);

    if (release.isEmpty()) {
      return;
    }

    InventoryDragController.Release<InventorySide> released = release.get();
    if (released.completedDrag() != null) {
      handleDraggedRelease(released.completedDrag(), released.releasedSlot(), mouseX, mouseY);
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

    if (!dialogBounds().contains(mouseX, mouseY)) {
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
    InventoryDragController.DragState<InventorySide> dragState = dragController.dragState();
    if (dragState == null) {
      return null;
    }

    StageHandle stage = Game.stage().orElse(null);
    if (stage == null) {
      return null;
    }

    GridHitTest.Slot<InventorySide> hoveredSlot =
        findSlotSelection(grid, stage.mouseX(), stage.mouseY());
    if (hoveredSlot == null) {
      return null;
    }

    if (hoveredSlot.slotIndex() == dragState.source().slotIndex()) {
      return null;
    }

    return hoveredSlot;
  }

  private void drawDropTargetHighlight(
      Graphics2D g, GridHitTest.Grid<InventorySide> grid, int slotIndex) {
    InventoryDragController.drawDropHighlight(
        g,
        grid.slotBounds(slotIndex),
        InventoryDragController.DEFAULT_DROP_FILL,
        InventoryDragController.DEFAULT_DROP_OUTLINE);
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

  private Rectangle dialogBounds() {
    return new Rectangle(x, y, width, height);
  }

  private static int encodePlayerInventorySlot(int slot) {
    return (-slot) - 1;
  }

  private void drawPanelBackground(Graphics2D g, int x, int y, int width, int height) {
    g.setColor(new Color(62, 62, 99, 96));
    g.fillRect(x, y, width, height);

    g.setColor(new Color(0x9dc1ebff, true));
    g.drawRect(x, y, width, height);
  }

  private void resetInteractionState() {
    pressedUseSlotIndex = null;
    rightButtonDownLastFrame = false;
    dragController.reset();
  }

  @Override
  public int x() {
    return x;
  }

  @Override
  public void x(int x) {
    this.x = x;
  }

  @Override
  public int y() {
    return y;
  }

  @Override
  public void y(int y) {
    this.y = y;
  }

  @Override
  public int width() {
    return width;
  }

  @Override
  public void width(int width) {
    this.width = width;
  }

  @Override
  public int height() {
    return height;
  }

  @Override
  public void height(int height) {
    this.height = height;
  }

  @Override
  public boolean visible() {
    return visible;
  }

  @Override
  public void visible(boolean visible) {
    this.visible = visible;
  }

  @Override
  public Stream<InventoryComponent> inventoryComponents() {
    return Stream.of(inventory);
  }

  private enum InventorySide {
    PLAYER
  }
}
