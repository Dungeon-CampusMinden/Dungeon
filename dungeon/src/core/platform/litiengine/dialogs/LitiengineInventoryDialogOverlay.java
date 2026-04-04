package core.platform.litiengine.dialogs;

import contrib.components.InventoryComponent;
import contrib.entities.HeroController;
import contrib.hud.UIUtils;
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
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Single-inventory overlay for the LITIENGINE backend.
 *
 * <p>This version renders inventory slots, supports right-click item usage for player inventories,
 * shows item hover information and adds drag-based item movement inside the player inventory as
 * well as dropping items outside the dialog.
 */
final class LitiengineInventoryDialogOverlay implements LitiengineUiOverlay {

  private static final int DEFAULT_WIDTH = 620;
  private static final int DEFAULT_HEIGHT = 360;

  private static final int TOOLTIP_OFFSET_X = 12;
  private static final int TOOLTIP_OFFSET_Y = 14;
  private static final int TOOLTIP_PADDING_X = 12;
  private static final int TOOLTIP_PADDING_Y = 10;
  private static final int TOOLTIP_LINE_GAP = 6;
  private static final int TOOLTIP_CORNER_RADIUS = 10;

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

  private Integer pressedUseSlotIndex = null;
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

    GridLayout grid;

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
      grid = new GridLayout(startX, gridTop, columns, slots);

      LitiengineInventoryGridRenderer.drawGrid(g, slots, startX, gridTop, columns);

      if (dragState == null) {
        drawHoverTooltip(g, grid);
      } else {
        drawDragPreview(g);
      }
    } finally {
      LitiengineDialogOverlaySupport.finishDialog(g, state);
    }

    handleInput(grid);
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

    String itemTitle = safeDisplayName(hoveredItem);
    String formattedDescription =
      UIUtils.formatString(hoveredItem.description() == null ? "" : hoveredItem.description());

    String[] descriptionLines =
      formattedDescription.isBlank() ? new String[0] : formattedDescription.split("\\R");

    FontMetrics metrics = g.getFontMetrics();

    int descriptionWidth = 0;
    for (String line : descriptionLines) {
      descriptionWidth = Math.max(descriptionWidth, metrics.stringWidth(line));
    }

    int tooltipWidth =
      Math.max(metrics.stringWidth(itemTitle), descriptionWidth) + 2 * TOOLTIP_PADDING_X;

    int tooltipHeight = 2 * TOOLTIP_PADDING_Y + metrics.getAscent();
    if (descriptionLines.length > 0) {
      tooltipHeight += TOOLTIP_LINE_GAP + descriptionLines.length * metrics.getHeight();
    }

    int tooltipX = mouseX + TOOLTIP_OFFSET_X;
    int tooltipY = mouseY + TOOLTIP_OFFSET_Y;

    if (tooltipX + tooltipWidth > stage.getWidth()) {
      tooltipX = mouseX - tooltipWidth - TOOLTIP_OFFSET_X;
    }

    if (tooltipY + tooltipHeight > stage.getHeight()) {
      tooltipY = mouseY - tooltipHeight - TOOLTIP_OFFSET_Y;
    }

    g.setColor(new Color(248, 248, 252, 235));
    g.fillRoundRect(
      tooltipX,
      tooltipY,
      tooltipWidth,
      tooltipHeight,
      TOOLTIP_CORNER_RADIUS,
      TOOLTIP_CORNER_RADIUS);

    g.setColor(new Color(84, 88, 96, 220));
    g.drawRoundRect(
      tooltipX,
      tooltipY,
      tooltipWidth,
      tooltipHeight,
      TOOLTIP_CORNER_RADIUS,
      TOOLTIP_CORNER_RADIUS);

    int textX = tooltipX + TOOLTIP_PADDING_X;
    int baselineY = tooltipY + TOOLTIP_PADDING_Y + metrics.getAscent();

    g.setColor(Color.BLACK);
    g.drawString(itemTitle, textX, baselineY);

    if (descriptionLines.length > 0) {
      g.setColor(new Color(0x000000b0, true));
      baselineY += TOOLTIP_LINE_GAP + metrics.getHeight();
      for (String line : descriptionLines) {
        g.drawString(line, textX, baselineY);
        baselineY += metrics.getHeight();
      }
    }
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

    String baseLabel = safeDisplayName(item);
    return item.stackSize() > 1 ? baseLabel + " x" + item.stackSize() : baseLabel;
  }

  private String safeDisplayName(Item item) {
    if (item == null) {
      return "";
    }

    String displayName = item.displayName();
    return displayName == null || displayName.isBlank()
      ? item.getClass().getSimpleName()
      : displayName;
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

  private record GridLayout(int startX, int startY, int columns, Item[] slots) {}

  private record DragState(int sourceSlot, Item item) {}
}
