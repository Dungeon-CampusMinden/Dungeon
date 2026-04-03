package core.platform.litiengine.dialogs;

import contrib.crafting.*;
import contrib.hud.dialogs.DialogCallbackResolver;
import contrib.hud.elements.ImageButton;
import contrib.item.Item;
import core.Game;
import core.input.MouseButtons;
import core.platform.litiengine.ui.LitiengineUiOverlay;
import core.ui.StageHandle;
import core.utils.InputManager;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.SimpleIPath;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.*;

/**
 * Minimal crafting overlay for the LITIENGINE backend.
 *
 * <p>This version shows the target inventory, the crafting input inventory, a recipe preview,
 * real Craft/Cancel buttons, and simple click-based item transfer between both inventories.
 *
 * <p>Drag-and-drop and more advanced slot interaction are still not implemented.
 */
final class LitiengineCraftingDialogOverlay implements LitiengineUiOverlay {

  private static final CraftingDialogLayout LAYOUT = new CraftingDialogLayout();

  private static final int DEFAULT_WIDTH = 1180;
  private static final int DEFAULT_HEIGHT = 600;

  private static final int PANEL_GAP = 26;
  private static final int PANEL_HEADER_GAP = 14;

  private static final int PREVIEW_TOP_GAP = 24;
  private static final int PREVIEW_HEIGHT = 150;
  private static final int PREVIEW_PADDING = 14;
  private static final int BUTTON_GAP = 16;

  private final String targetTitle;
  private final String craftingTitle;
  private final String dialogId;
  private final CraftingDialogController controller;
  private final CraftingDialogInteraction interaction;

  private int x;
  private int y;
  private int width = DEFAULT_WIDTH;
  private int height = DEFAULT_HEIGHT;
  private boolean visible = true;

  private final Map<CraftingDialogAction, ImageButton> actionButtons =
    new EnumMap<>(CraftingDialogAction.class);

  private SlotSelection pressedSlotSelection = null;
  private boolean leftButtonDownLastFrame = false;

  LitiengineCraftingDialogOverlay(
    String targetTitle,
    String craftingTitle,
    CraftingDialogController controller,
    String dialogId) {
    this.targetTitle = (targetTitle == null || targetTitle.isBlank()) ? "Inventory" : targetTitle;
    this.craftingTitle =
      (craftingTitle == null || craftingTitle.isBlank()) ? "Crafting" : craftingTitle;
    this.controller = controller;
    this.interaction = new CraftingDialogInteraction(controller);
    this.dialogId = dialogId;

    for (CraftingDialogAction action : CraftingDialogAction.values()) {
      ImageButton button =
        new ImageButton(
          new Animation(new SimpleIPath(action.iconPath())),
          0,
          0,
          1,
          1);

      button.onClick(
        ignored ->
          DialogCallbackResolver.createButtonCallback(dialogId, action.callbackKey())
            .accept(action == CraftingDialogAction.CRAFT ? controller.craftingPayload() : null));

      actionButtons.put(action, button);
    }
  }

  @Override
  public void render(Graphics2D g) {
    if (!visible) {
      return;
    }

    Item[] targetSlots = controller.targetSlots();
    Item[] craftingSlots = controller.craftingSlots();

    int leftColumns = LitiengineInventoryGridRenderer.columnsFor(targetSlots);
    int rightColumns = LitiengineInventoryGridRenderer.columnsFor(craftingSlots);
    int leftRows = LitiengineInventoryGridRenderer.rowsFor(targetSlots, leftColumns);
    int rightRows = LitiengineInventoryGridRenderer.rowsFor(craftingSlots, rightColumns);

    int leftGridWidth = LitiengineInventoryGridRenderer.gridWidth(leftColumns);
    int rightGridWidth = LitiengineInventoryGridRenderer.gridWidth(rightColumns);

    int contentWidth =
      leftGridWidth
        + rightGridWidth
        + PANEL_GAP
        + 2 * LitiengineDialogOverlaySupport.PADDING;
    width = Math.max(DEFAULT_WIDTH, contentWidth);

    int maxGridHeight =
      Math.max(
        LitiengineInventoryGridRenderer.gridHeight(leftRows),
        LitiengineInventoryGridRenderer.gridHeight(rightRows));

    height =
      Math.max(
        DEFAULT_HEIGHT,
        190
          + maxGridHeight
          + PREVIEW_TOP_GAP
          + PREVIEW_HEIGHT
          + LitiengineDialogOverlaySupport.PADDING);

    if (x == 0 && y == 0) {
      x = (Game.windowWidth() - width) / 2;
      y = (Game.windowHeight() - height) / 2;
    }

    LitiengineDialogOverlaySupport.RenderState state =
      LitiengineDialogOverlaySupport.beginDialog(g);

    try {
      int contentY =
        LitiengineDialogOverlaySupport.drawFrameAndTitle(g, x, y, width, height, "Crafting");

      int totalGridWidth = leftGridWidth + PANEL_GAP + rightGridWidth;
      int leftStartX = x + (width - totalGridWidth) / 2;
      int rightStartX = leftStartX + leftGridWidth + PANEL_GAP;

      int titleBaseline = contentY + g.getFontMetrics().getAscent();
      g.setColor(Color.WHITE);
      g.drawString(targetTitle, leftStartX, titleBaseline);
      g.drawString(craftingTitle, rightStartX, titleBaseline);

      int infoY = contentY + PANEL_HEADER_GAP + LitiengineInventoryGridRenderer.INFO_LINE_GAP;

      LitiengineInventoryGridRenderer.drawInventoryInfo(
        g, controller.targetInventory(), targetSlots, leftStartX, infoY);
      LitiengineInventoryGridRenderer.drawInventoryInfo(
        g, controller.craftingInventory(), craftingSlots, rightStartX, infoY);

      int gridTop = infoY + LitiengineInventoryGridRenderer.GRID_TOP_GAP + 4;

      drawPanelBackground(
        g,
        leftStartX - 12,
        gridTop - 12,
        leftGridWidth + 24,
        LitiengineInventoryGridRenderer.gridHeight(leftRows) + 24);

      drawPanelBackground(
        g,
        rightStartX - 12,
        gridTop - 12,
        rightGridWidth + 24,
        LitiengineInventoryGridRenderer.gridHeight(rightRows) + 24);

      LitiengineInventoryGridRenderer.drawGrid(g, targetSlots, leftStartX, gridTop, leftColumns);
      LitiengineInventoryGridRenderer.drawGrid(
        g, craftingSlots, rightStartX, gridTop, rightColumns);

      Rectangle previewPanelBounds = previewPanelBounds(gridTop, maxGridHeight);
      List<Rectangle> buttonBounds = buttonBounds(previewPanelBounds.y);
      syncActionButtonBounds(buttonBounds);

      GridLayout leftGrid =
        new GridLayout(InventorySide.TARGET, leftStartX, gridTop, leftColumns, targetSlots);
      GridLayout rightGrid =
        new GridLayout(InventorySide.CRAFTING, rightStartX, gridTop, rightColumns, craftingSlots);

      handleInput(leftGrid, rightGrid);
      drawRecipePreview(g, previewPanelBounds, buttonBounds);
    } finally {
      LitiengineDialogOverlaySupport.finishDialog(g, state);
    }
  }

  private void drawRecipePreview(Graphics2D g, Rectangle previewPanelBounds, List<Rectangle> buttons) {
    drawPanelBackground(
      g,
      previewPanelBounds.x,
      previewPanelBounds.y,
      previewPanelBounds.width,
      previewPanelBounds.height);

    g.setColor(Color.WHITE);
    g.drawString(
      "Recipe Preview",
      previewPanelBounds.x + PREVIEW_PADDING,
      previewPanelBounds.y + 24);

    g.setColor(new Color(210, 210, 210));
    g.drawString(
      previewStatusLine(),
      previewPanelBounds.x + PREVIEW_PADDING,
      previewPanelBounds.y + 42);

    Rectangle previewLayoutBounds = previewLayoutBounds(previewPanelBounds, buttons);

    drawCraftingPreview(g, previewLayoutBounds);
    drawResultPreview(g, previewLayoutBounds);

    for (CraftingDialogAction action : CraftingDialogAction.values()) {
      LitiengineButtonRenderer.draw(g, actionButtons.get(action), action.label());
    }
  }

  private Rectangle previewPanelBounds(int gridTop, int maxGridHeight) {
    int previewX = x + LitiengineDialogOverlaySupport.PADDING;
    int previewY = gridTop + maxGridHeight + PREVIEW_TOP_GAP;
    int previewWidth = width - 2 * LitiengineDialogOverlaySupport.PADDING;
    return new Rectangle(previewX, previewY, previewWidth, PREVIEW_HEIGHT);
  }

  private Rectangle previewLayoutBounds(Rectangle previewPanelBounds, List<Rectangle> buttons) {
    int layoutX = previewPanelBounds.x + PREVIEW_PADDING;
    int layoutY = previewPanelBounds.y + 52;
    int layoutWidth = previewPanelBounds.width - 2 * PREVIEW_PADDING;

    int buttonsTop =
      buttons.stream()
        .mapToInt(rect -> rect.y)
        .min()
        .orElse(previewPanelBounds.y + previewPanelBounds.height);

    int layoutBottom = buttonsTop - 10;
    int layoutHeight = Math.max(50, layoutBottom - layoutY);

    return new Rectangle(layoutX, layoutY, layoutWidth, layoutHeight);
  }

  private void drawCraftingPreview(Graphics2D g, Rectangle previewLayoutBounds) {
    Item[] craftingSlots = controller.craftingSlots();
    List<CraftingDialogLayout.SlotBounds> slots =
      LAYOUT.visibleCraftingSlots(
        craftingSlots,
        previewLayoutBounds.x,
        previewLayoutBounds.y,
        previewLayoutBounds.width,
        previewLayoutBounds.height);

    for (int i = 0; i < slots.size(); i++) {
      CraftingDialogLayout.SlotBounds slot = slots.get(i);
      Item item = craftingSlots[slot.slotIndex()];
      Rectangle bounds =
        mirrorLayoutBounds(slot.x(), slot.y(), slot.size(), previewLayoutBounds);

      drawPreviewItem(g, item, bounds.x, bounds.y, bounds.width, Integer.toString(i + 1));
    }
  }

  private void drawResultPreview(Graphics2D g, Rectangle previewLayoutBounds) {
    Item[] resultItems = controller.resultItems();
    List<CraftingDialogLayout.ItemBounds> resultSlots =
      LAYOUT.resultSlots(
        resultItems,
        previewLayoutBounds.x,
        previewLayoutBounds.y,
        previewLayoutBounds.width,
        previewLayoutBounds.height);

    for (int i = 0; i < resultSlots.size(); i++) {
      CraftingDialogLayout.ItemBounds slot = resultSlots.get(i);
      Rectangle bounds =
        mirrorLayoutBounds(slot.x(), slot.y(), slot.size(), previewLayoutBounds);

      drawPreviewItem(g, resultItems[i], bounds.x, bounds.y, bounds.width, resultItems[i].displayName());
    }
  }

  private Rectangle mirrorLayoutBounds(int absoluteX, int absoluteY, int size, Rectangle layoutBounds) {
    int relativeY = absoluteY - layoutBounds.y;
    int mirroredY = layoutBounds.y + layoutBounds.height - relativeY - size;
    return new Rectangle(absoluteX, mirroredY, size, size);
  }

  private void drawPreviewItem(Graphics2D g, Item item, int x, int y, int size, String label) {
    g.setColor(new Color(44, 47, 58, 210));
    g.fillRoundRect(x, y, size, size, 10, 10);

    g.setColor(new Color(102, 107, 124, 220));
    g.drawRoundRect(x, y, size, size, 10, 10);

    if (item != null) {
      String itemText = item.displayName();
      if (itemText.length() > 16) {
        itemText = itemText.substring(0, 16);
      }

      g.setColor(Color.WHITE);
      g.drawString(itemText, x + 6, y + Math.max(18, size / 2));
    }

    if (label != null && !label.isBlank()) {
      g.setColor(new Color(210, 210, 210));
      g.drawString(label, x + 6, y + size - 8);
    }
  }

  private String previewStatusLine() {
    String text = buildRecipePreviewText();
    int newline = text.indexOf('\n');
    return newline >= 0 ? text.substring(0, newline) : text;
  }

  private String buildRecipePreviewText() {
    Optional<Recipe> recipe = controller.currentRecipe();

    if (recipe.isEmpty()) {
      if (controller.craftingInventory().isEmpty()) {
        return "Click items to move them into the crafting inventory.\n"
          + "Craft and Cancel already trigger the existing server-side callbacks.";
      }

      return "No matching recipe found for the current crafting inputs.\n"
        + "Click items to move them back or adjust the current crafting inputs.";
    }

    Recipe currentRecipe = recipe.get();

    String resultText =
      Arrays.stream(currentRecipe.results())
        .filter(result -> result.resultType() == CraftingType.ITEM)
        .map(CraftingDialogLogic::resultLabel)
        .filter(label -> !label.isBlank())
        .reduce((a, b) -> a + ", " + b)
        .orElse("Crafting result available.");

    String orderText = currentRecipe.ordered() ? "Ordered recipe." : "Unordered recipe.";

    return "Result: "
      + resultText
      + "\n"
      + orderText
      + "\nClick items between both inventories and use Craft to forward the current input.";
  }

  private void handleInput(GridLayout leftGrid, GridLayout rightGrid) {
    StageHandle stage = Game.stage().orElse(null);
    if (stage == null) {
      pressedSlotSelection = null;
      leftButtonDownLastFrame = false;
      return;
    }

    int mouseX = stage.mouseX();
    int mouseY = stage.mouseY();
    boolean leftButtonDown = InputManager.isButtonPressed(MouseButtons.LEFT);

    if (leftButtonDown && !leftButtonDownLastFrame) {
      if (findActionButtonAt(mouseX, mouseY).isPresent()) {
        pressedSlotSelection = null;
      } else {
        pressedSlotSelection = findSlotSelection(mouseX, mouseY, leftGrid, rightGrid);
      }
    }

    if (!leftButtonDown && leftButtonDownLastFrame) {
      if (findActionButtonAt(mouseX, mouseY).isPresent()) {
        pressedSlotSelection = null;
        leftButtonDownLastFrame = leftButtonDown;
        return;
      }

      SlotSelection releasedSlotSelection = findSlotSelection(mouseX, mouseY, leftGrid, rightGrid);
      SlotSelection previouslyPressedSlot = pressedSlotSelection;
      pressedSlotSelection = null;

      if (previouslyPressedSlot != null && previouslyPressedSlot.equals(releasedSlotSelection)) {
        transferClickedItem(previouslyPressedSlot);
      }
    }

    leftButtonDownLastFrame = leftButtonDown;
  }

  private void transferClickedItem(SlotSelection slotSelection) {
    interaction.transferClickedSlot(slotSelection.side().controllerSide(), slotSelection.slotIndex());
  }

  private void syncActionButtonBounds(List<Rectangle> bounds) {
    CraftingDialogAction[] actions = CraftingDialogAction.values();

    for (int i = 0; i < actions.length && i < bounds.size(); i++) {
      Rectangle rect = bounds.get(i);
      ImageButton button = actionButtons.get(actions[i]);
      button.x(rect.x);
      button.y(rect.y);
      button.width(rect.width);
      button.height(rect.height);
    }
  }

  private Optional<CraftingDialogAction> findActionButtonAt(int mouseX, int mouseY) {
    for (CraftingDialogAction action : CraftingDialogAction.values()) {
      ImageButton button = actionButtons.get(action);
      if (button == null) {
        continue;
      }

      if (mouseX >= button.x()
        && mouseX <= button.x() + button.width()
        && mouseY >= button.y()
        && mouseY <= button.y() + button.height()) {
        return Optional.of(action);
      }
    }

    return Optional.empty();
  }

  private SlotSelection findSlotSelection(int mouseX, int mouseY, GridLayout leftGrid, GridLayout rightGrid) {
    int leftIndex =
      LitiengineInventoryGridRenderer.findSlotIndexAt(
        mouseX, mouseY, leftGrid.slots(), leftGrid.startX(), leftGrid.startY(), leftGrid.columns());
    if (leftIndex >= 0) {
      return new SlotSelection(leftGrid.side(), leftIndex);
    }

    int rightIndex =
      LitiengineInventoryGridRenderer.findSlotIndexAt(
        mouseX,
        mouseY,
        rightGrid.slots(),
        rightGrid.startX(),
        rightGrid.startY(),
        rightGrid.columns());
    if (rightIndex >= 0) {
      return new SlotSelection(rightGrid.side(), rightIndex);
    }

    return null;
  }

  private List<Rectangle> buttonBounds(int previewY) {
    int previewX = x + LitiengineDialogOverlaySupport.PADDING;
    int previewWidth = width - 2 * LitiengineDialogOverlaySupport.PADDING;

    return LitiengineDialogOverlaySupport.centeredButtonRow(
      previewX,
      previewY,
      previewWidth,
      PREVIEW_HEIGHT,
      CraftingDialogAction.values().length,
      BUTTON_GAP);
  }

  private void drawPanelBackground(Graphics2D g, int x, int y, int width, int height) {
    g.setColor(new Color(28, 30, 38, 170));
    g.fillRoundRect(x, y, width, height, 12, 12);
    g.setColor(new Color(90, 94, 108, 180));
    g.drawRoundRect(x, y, width, height, 12, 12);
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

  private enum InventorySide {
    TARGET(CraftingDialogController.InventorySide.TARGET),
    CRAFTING(CraftingDialogController.InventorySide.CRAFTING);

    private final CraftingDialogController.InventorySide controllerSide;

    InventorySide(CraftingDialogController.InventorySide controllerSide) {
      this.controllerSide = controllerSide;
    }

    private CraftingDialogController.InventorySide controllerSide() {
      return controllerSide;
    }
  }

  private record SlotSelection(InventorySide side, int slotIndex) {}

  private record GridLayout(
    InventorySide side, int startX, int startY, int columns, Item[] slots) {}
}
