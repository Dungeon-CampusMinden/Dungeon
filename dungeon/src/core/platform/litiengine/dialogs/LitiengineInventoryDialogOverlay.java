package core.platform.litiengine.dialogs;

import contrib.components.InventoryComponent;
import contrib.entities.HeroController;
import contrib.item.Item;
import core.Entity;
import core.Game;
import core.input.MouseButtons;
import core.platform.litiengine.ui.LitiengineUiOverlay;
import core.ui.StageHandle;
import core.utils.InputManager;
import java.awt.Graphics2D;

/**
 * Minimal single-inventory overlay for the LITIENGINE backend.
 *
 * <p>This version renders inventory slots and supports simple right-click item usage for player
 * inventories.
 */
final class LitiengineInventoryDialogOverlay implements LitiengineUiOverlay {

  private static final int DEFAULT_WIDTH = 620;
  private static final int DEFAULT_HEIGHT = 360;

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
        g,
        inventory,
        slots,
        x + LitiengineDialogOverlaySupport.PADDING,
        contentY);

      gridTop =
        contentY
          + LitiengineInventoryGridRenderer.INFO_LINE_GAP
          + LitiengineInventoryGridRenderer.GRID_TOP_GAP;

      startX = x + (width - LitiengineInventoryGridRenderer.gridWidth(columns)) / 2;

      LitiengineInventoryGridRenderer.drawGrid(g, slots, startX, gridTop, columns);
    } finally {
      LitiengineDialogOverlaySupport.finishDialog(g, state);
    }

    handleInput(new GridLayout(startX, gridTop, columns, slots));
  }

  private void handleInput(GridLayout grid) {
    if (!allowUseItems) {
      return;
    }

    StageHandle stage = Game.stage().orElse(null);
    if (stage == null) {
      return;
    }

    int mouseX = stage.mouseX();
    int mouseY = stage.mouseY();

    if (InputManager.isButtonJustPressed(MouseButtons.RIGHT)) {
      int slotIndex =
        LitiengineInventoryGridRenderer.findSlotIndexAt(
          mouseX, mouseY, grid.slots(), grid.startX(), grid.startY(), grid.columns());
      pressedSlotIndex = slotIndex >= 0 ? slotIndex : null;
    }

    if (InputManager.isButtonJustReleased(MouseButtons.RIGHT)) {
      int releasedSlotIndex =
        LitiengineInventoryGridRenderer.findSlotIndexAt(
          mouseX, mouseY, grid.slots(), grid.startX(), grid.startY(), grid.columns());

      Integer previouslyPressedSlot = pressedSlotIndex;
      pressedSlotIndex = null;

      if (previouslyPressedSlot != null && previouslyPressedSlot == releasedSlotIndex) {
        HeroController.useItem(owner, releasedSlotIndex);
      }
    }
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
}
