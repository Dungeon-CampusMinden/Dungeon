package contrib.hud.inventory;

import contrib.components.InventoryComponent;
import contrib.entities.HeroController;
import contrib.hud.elements.InventoryComponentProvider;
import contrib.hud.renderers.InventoryGridRenderer;
import contrib.hud.renderers.ItemTooltipRenderer;
import contrib.hud.renderers.DialogFrameRenderer;
import contrib.item.Item;
import core.Entity;
import core.Game;
import core.input.MouseButtons;
import core.network.messages.c2s.InputMessage;
import core.ui.overlay.UiOverlay;
import core.ui.StageHandle;
import core.utils.InputManager;
import core.utils.Vector2;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
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
 * <ul>
 *   <li>Rendering the inventory UI and associated elements.</li>
 *   <li>Drag-and-drop support for moving items between slots or dropping them.</li>
 *   <li>Handling player input, including drag events and item slot interaction.</li>
 *   <li>Visual feedback for hovered or targeted slots.</li>
 * </ul>
 */
final class InventoryDialogOverlay
  implements UiOverlay, InventoryComponentProvider {

  private static final int DEFAULT_WIDTH = 560;
  private static final int DEFAULT_HEIGHT = 430;

  private static final int PANEL_PADDING = 8;
  private static final int PANEL_HEADER_GAP = 8;

  private static final int DRAG_THRESHOLD_PX = 8;
  private static final int DRAG_TARGET_INSET = 3;
  private static final int DRAG_TARGET_ARC = 8;

  private final String title;
  private final Entity owner;
  private final InventoryComponent inventory;
  private final boolean allowUseItems;

  private int x;
  private int y;
  private int width = DEFAULT_WIDTH;
  private int height = DEFAULT_HEIGHT;
  private boolean visible = true;

  private Integer pressedUseSlotIndex = null;
  private boolean rightButtonDownLastFrame = false;

  private Integer pressedDragSlotIndex = null;
  private boolean leftButtonDownLastFrame = false;
  private int pressedMouseX = 0;
  private int pressedMouseY = 0;
  private DragState dragState = null;

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
    Item[] visibleSlots = slots;

    if (dragState != null) {
      visibleSlots = slots.clone();
      int sourceSlot = dragState.sourceSlot();
      if (sourceSlot >= 0 && sourceSlot < visibleSlots.length) {
        visibleSlots[sourceSlot] = null;
      }
    }

    int columns = InventoryGridRenderer.columnsFor(slots);
    int rows = InventoryGridRenderer.rowsFor(slots, columns);

    int gridWidth = InventoryGridRenderer.gridWidth(columns);
    int gridHeight = InventoryGridRenderer.gridHeight(rows);

    width =
      Math.max(
        DEFAULT_WIDTH,
        2 * DialogFrameRenderer.PADDING + gridWidth + 2 * PANEL_PADDING);

    height =
      Math.max(
        DEFAULT_HEIGHT,
        96 + gridHeight + 2 * PANEL_PADDING + DialogFrameRenderer.PADDING);

    if (x == 0 && y == 0) {
      x = (Game.windowWidth() - width) / 2;
      y = (Game.windowHeight() - height) / 2;
    }

    int contentY;
    int startX;
    int gridTop;

    GridLayout grid;

    DialogFrameRenderer.RenderState state =
      DialogFrameRenderer.beginDialog(g);

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

      drawPanelBackground(
        g,
        panelBounds.x,
        panelBounds.y,
        panelBounds.width,
        panelBounds.height);

      grid = new GridLayout(startX, gridTop, columns, visibleSlots);

      InventoryGridRenderer.drawGrid(g, visibleSlots, startX, gridTop, columns);

      if (dragState != null) {
        Integer hoveredTargetSlotIndex = hoveredDropTargetSlotIndex(grid);
        if (hoveredTargetSlotIndex != null) {
          drawDropTargetHighlight(g, grid, hoveredTargetSlotIndex);
        }
        drawDragPreview(g);
      } else {
        drawHoverTooltip(g, grid);
      }
    } finally {
      DialogFrameRenderer.finishDialog(g, state);
    }

    handleInput(grid);
  }

  private void drawDragPreview(Graphics2D g) {
    StageHandle stage = Game.stage().orElse(null);
    if (stage == null || dragState == null || dragState.item() == null) {
      return;
    }

    int previewX = stage.mouseX() - InventoryGridRenderer.SLOT_WIDTH / 2;
    int previewY = stage.mouseY() - InventoryGridRenderer.SLOT_HEIGHT / 2;

    InventoryGridRenderer.drawItemPreview(g, previewX, previewY, dragState.item());
  }

  private void drawHoverTooltip(Graphics2D g, GridLayout grid) {
    StageHandle stage = Game.stage().orElse(null);
    if (stage == null) {
      return;
    }

    int mouseX = stage.mouseX();
    int mouseY = stage.mouseY();

    if (mouseX < x || mouseX > x + width || mouseY < y || mouseY > y + height) {
      return;
    }

    int hoveredSlotIndex = findSlotIndex(grid, mouseX, mouseY);
    if (hoveredSlotIndex < 0) {
      return;
    }

    Item hoveredItem = inventory.get(hoveredSlotIndex).orElse(null);
    if (hoveredItem == null) {
      return;
    }

    ItemTooltipRenderer.drawTooltip(
      g, hoveredItem, mouseX, mouseY, (int) stage.getWidth(), (int) stage.getHeight());
  }

  private void handleInput(GridLayout grid) {
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

  private void handleLeftDragInput(GridLayout grid, int mouseX, int mouseY) {
    boolean leftButtonDown = InputManager.isButtonPressed(MouseButtons.LEFT);

    if (leftButtonDown && !leftButtonDownLastFrame) {
      int slotIndex = findSlotIndex(grid, mouseX, mouseY);
      pressedDragSlotIndex = slotIndex >= 0 ? slotIndex : null;
      pressedMouseX = mouseX;
      pressedMouseY = mouseY;
      dragState = null;
    } else if (leftButtonDown) {
      maybeStartDrag(mouseX, mouseY);
    }

    if (!leftButtonDown && leftButtonDownLastFrame) {
      int releasedSlotIndex = findSlotIndex(grid, mouseX, mouseY);

      DragState completedDrag = dragState;
      pressedDragSlotIndex = null;
      dragState = null;

      if (completedDrag != null) {
        handleDraggedRelease(completedDrag, releasedSlotIndex, mouseX, mouseY);
      }
    }

    leftButtonDownLastFrame = leftButtonDown;
  }

  private void handleRightUseInput(GridLayout grid, int mouseX, int mouseY) {
    boolean rightButtonDown = InputManager.isButtonPressed(MouseButtons.RIGHT);

    if (dragState != null) {
      rightButtonDownLastFrame = rightButtonDown;
      return;
    }

    if (rightButtonDown && !rightButtonDownLastFrame) {
      int slotIndex = findSlotIndex(grid, mouseX, mouseY);
      pressedUseSlotIndex = slotIndex >= 0 ? slotIndex : null;
    }

    if (!rightButtonDown && rightButtonDownLastFrame) {
      int releasedSlotIndex = findSlotIndex(grid, mouseX, mouseY);

      Integer previouslyPressedSlot = pressedUseSlotIndex;
      pressedUseSlotIndex = null;

      if (previouslyPressedSlot != null && previouslyPressedSlot == releasedSlotIndex) {
        HeroController.useItem(owner, releasedSlotIndex);
      }
    }

    rightButtonDownLastFrame = rightButtonDown;
  }

  private void maybeStartDrag(int mouseX, int mouseY) {
    if (dragState != null || pressedDragSlotIndex == null) {
      return;
    }

    int deltaX = mouseX - pressedMouseX;
    int deltaY = mouseY - pressedMouseY;
    int thresholdSquared = DRAG_THRESHOLD_PX * DRAG_THRESHOLD_PX;

    if ((deltaX * deltaX) + (deltaY * deltaY) < thresholdSquared) {
      return;
    }

    Item draggedItem = inventory.get(pressedDragSlotIndex).orElse(null);
    if (draggedItem == null) {
      pressedDragSlotIndex = null;
      return;
    }

    dragState = new DragState(pressedDragSlotIndex, draggedItem);
  }

  private void handleDraggedRelease(
    DragState completedDrag, int releasedSlotIndex, int mouseX, int mouseY) {
    if (releasedSlotIndex >= 0 && releasedSlotIndex != completedDrag.sourceSlot()) {
      moveDraggedItem(completedDrag.sourceSlot(), releasedSlotIndex);
      return;
    }

    if (!dialogBounds().contains(mouseX, mouseY)) {
      dropDraggedItem(completedDrag.sourceSlot());
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
            InputMessage.Action.INV_MOVE,
            Vector2.of(encodedSourceSlot, encodedTargetSlot)),
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

  private Integer hoveredDropTargetSlotIndex(GridLayout grid) {
    if (dragState == null) {
      return null;
    }

    StageHandle stage = Game.stage().orElse(null);
    if (stage == null) {
      return null;
    }

    int hoveredSlotIndex = findSlotIndex(grid, stage.mouseX(), stage.mouseY());
    if (hoveredSlotIndex < 0) {
      return null;
    }

    if (hoveredSlotIndex == dragState.sourceSlot()) {
      return null;
    }

    return hoveredSlotIndex;
  }

  private void drawDropTargetHighlight(Graphics2D g, GridLayout grid, int slotIndex) {
    Rectangle bounds =
      InventoryGridRenderer.slotBounds(
        slotIndex, grid.startX(), grid.startY(), grid.columns());

    int insetX = bounds.x + DRAG_TARGET_INSET;
    int insetY = bounds.y + DRAG_TARGET_INSET;
    int insetWidth = bounds.width - 2 * DRAG_TARGET_INSET;
    int insetHeight = bounds.height - 2 * DRAG_TARGET_INSET;

    g.setColor(new Color(88, 168, 116, 70));
    g.fillRoundRect(insetX, insetY, insetWidth, insetHeight, DRAG_TARGET_ARC, DRAG_TARGET_ARC);

    g.setColor(new Color(132, 214, 156, 210));
    g.drawRoundRect(insetX, insetY, insetWidth, insetHeight, DRAG_TARGET_ARC, DRAG_TARGET_ARC);
  }

  private int findSlotIndex(GridLayout grid, int mouseX, int mouseY) {
    return InventoryGridRenderer.findSlotIndexAt(
      mouseX, mouseY, grid.slots(), grid.startX(), grid.startY(), grid.columns());
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
    pressedDragSlotIndex = null;
    leftButtonDownLastFrame = false;
    dragState = null;
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

  private record GridLayout(int startX, int startY, int columns, Item[] slots) {}

  private record DragState(int sourceSlot, Item item) {}
}
