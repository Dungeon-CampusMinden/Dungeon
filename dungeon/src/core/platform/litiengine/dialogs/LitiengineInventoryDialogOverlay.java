package core.platform.litiengine.dialogs;

import contrib.components.InventoryComponent;
import contrib.entities.HeroController;
import contrib.item.Item;
import core.Entity;
import core.Game;
import core.input.MouseButtons;
import core.network.messages.c2s.InputMessage;
import core.platform.litiengine.ui.LitiengineUiOverlay;
import core.ui.StageHandle;
import core.utils.InputManager;
import core.utils.Vector2;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Single-inventory overlay for the LITIENGINE backend.
 *
 * <p>This version keeps right-click item usage for player inventories and additionally supports
 * drag-based item movement inside the player inventory as well as dropping items outside the dialog.
 */
final class LitiengineInventoryDialogOverlay implements LitiengineUiOverlay {

  private static final int DEFAULT_WIDTH = 620;
  private static final int DEFAULT_HEIGHT = 360;

  private static final int DRAG_THRESHOLD_PX = 8;
  private static final int DRAG_PREVIEW_OFFSET_X = 14;
  private static final int DRAG_PREVIEW_OFFSET_Y = 18;
  private static final int DRAG_PREVIEW_PADDING_X = 10;
  private static final int DRAG_PREVIEW_PADDING_Y = 7;

  private final String title;
  private final Entity owner;
  private final InventoryComponent inventory;
  private final boolean allowUseItems;

  private int x;
  private int y;
  private int width = DEFAULT_WIDTH;
  private int height = DEFAULT_HEIGHT;
  private boolean visible = true;

  private Integer pressedSlotIndex = null;
  private boolean rightButtonDownLastFrame = false;

  private Integer pressedDragSlotIndex = null;
  private boolean leftButtonDownLastFrame = false;
  private int pressedMouseX = 0;
  private int pressedMouseY = 0;
  private DragState dragState = null;

  LitiengineInventoryDialogOverlay(
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
    int columns = LitiengineInventoryGridRenderer.columnsFor(slots);
    int rows = LitiengineInventoryGridRenderer.rowsFor(slots, columns);

    width =
      Math.max(
        DEFAULT_WIDTH,
        2 * LitiengineDialogOverlaySupport.PADDING
          + LitiengineInventoryGridRenderer.gridWidth(columns));

    height =
      Math.max(
        DEFAULT_HEIGHT,
        130
          + LitiengineInventoryGridRenderer.gridHeight(rows)
          + LitiengineDialogOverlaySupport.PADDING);

    if (x == 0 && y == 0) {
      x = (Game.windowWidth() - width) / 2;
      y = (Game.windowHeight() - height) / 2;
    }

    int contentY;
    int startX;
    int gridTop;

    LitiengineDialogOverlaySupport.RenderState state =
      LitiengineDialogOverlaySupport.beginDialog(g);

    try {
      contentY = LitiengineDialogOverlaySupport.drawFrameAndTitle(g, x, y, width, height, title);

      LitiengineInventoryGridRenderer.drawInventoryInfo(
        g, inventory, slots, x + LitiengineDialogOverlaySupport.PADDING, contentY);

      gridTop =
        contentY
          + LitiengineInventoryGridRenderer.INFO_LINE_GAP
          + LitiengineInventoryGridRenderer.GRID_TOP_GAP;

      startX = x + (width - LitiengineInventoryGridRenderer.gridWidth(columns)) / 2;

      LitiengineInventoryGridRenderer.drawGrid(g, slots, startX, gridTop, columns);

      if (dragState != null) {
        drawDragPreview(g);
      }
    } finally {
      LitiengineDialogOverlaySupport.finishDialog(g, state);
    }

    handleInput(new GridLayout(startX, gridTop, columns, slots));
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
      pressedSlotIndex = slotIndex >= 0 ? slotIndex : null;
    }

    if (!rightButtonDown && rightButtonDownLastFrame) {
      int releasedSlotIndex = findSlotIndex(grid, mouseX, mouseY);

      Integer previouslyPressedSlot = pressedSlotIndex;
      pressedSlotIndex = null;

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

  private int findSlotIndex(GridLayout grid, int mouseX, int mouseY) {
    return LitiengineInventoryGridRenderer.findSlotIndexAt(
      mouseX, mouseY, grid.slots(), grid.startX(), grid.startY(), grid.columns());
  }

  private Rectangle dialogBounds() {
    return new Rectangle(x, y, width, height);
  }

  private static int encodePlayerInventorySlot(int slot) {
    return (-slot) - 1;
  }

  private void drawDragPreview(Graphics2D g) {
    StageHandle stage = Game.stage().orElse(null);
    if (stage == null || dragState == null) {
      return;
    }

    int previewX = stage.mouseX() + DRAG_PREVIEW_OFFSET_X;
    int previewY = stage.mouseY() + DRAG_PREVIEW_OFFSET_Y;

    String label = dragLabel(dragState.item());
    int textWidth = g.getFontMetrics().stringWidth(label);
    int textHeight = g.getFontMetrics().getAscent();

    int boxWidth = textWidth + 2 * DRAG_PREVIEW_PADDING_X;
    int boxHeight = textHeight + 2 * DRAG_PREVIEW_PADDING_Y;

    g.setColor(new Color(20, 20, 24, 220));
    g.fillRoundRect(previewX, previewY, boxWidth, boxHeight, 10, 10);

    g.setColor(new Color(220, 220, 230, 220));
    g.drawRoundRect(previewX, previewY, boxWidth, boxHeight, 10, 10);

    g.setColor(Color.WHITE);
    g.drawString(
      label,
      previewX + DRAG_PREVIEW_PADDING_X,
      previewY + DRAG_PREVIEW_PADDING_Y + textHeight - 2);
  }

  private String dragLabel(Item item) {
    if (item == null) {
      return "";
    }

    String baseLabel =
      item.displayName() == null || item.displayName().isBlank()
        ? item.getClass().getSimpleName()
        : item.displayName();

    return item.stackSize() > 1 ? baseLabel + " x" + item.stackSize() : baseLabel;
  }

  private void resetInteractionState() {
    pressedSlotIndex = null;
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

  private record GridLayout(int startX, int startY, int columns, Item[] slots) {}

  private record DragState(int sourceSlot, Item item) {}
}
