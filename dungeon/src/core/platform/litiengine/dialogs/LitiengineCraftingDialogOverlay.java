package core.platform.litiengine.dialogs;

import contrib.components.InventoryComponent;
import contrib.crafting.Crafting;
import contrib.crafting.CraftingResult;
import contrib.crafting.CraftingType;
import contrib.crafting.Recipe;
import contrib.hud.dialogs.DialogCallbackResolver;
import contrib.item.Item;
import core.Game;
import core.input.MouseButtons;
import core.platform.litiengine.ui.LitiengineUiOverlay;
import core.ui.StageHandle;
import core.utils.InputManager;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Minimal crafting overlay for the LITIENGINE backend.
 *
 * <p>This version shows the target inventory, the crafting input inventory, a recipe preview,
 * real Craft/Cancel buttons, and simple click-based item transfer between both inventories.
 *
 * <p>Drag-and-drop and more advanced slot interaction are still not implemented.
 */
final class LitiengineCraftingDialogOverlay implements LitiengineUiOverlay {

  private static final int DEFAULT_WIDTH = 1180;
  private static final int DEFAULT_HEIGHT = 600;

  private static final int PANEL_GAP = 26;
  private static final int PANEL_HEADER_GAP = 14;

  private static final int PREVIEW_TOP_GAP = 24;
  private static final int PREVIEW_HEIGHT = 150;
  private static final int PREVIEW_PADDING = 14;
  private static final int BUTTON_GAP = 16;

  private static final String CRAFT_BUTTON = "Craft";
  private static final String CANCEL_BUTTON = "Cancel";

  private static final String CALLBACK_CRAFT = "craft";
  private static final String CALLBACK_CANCEL = "cancel";

  private final String targetTitle;
  private final InventoryComponent targetInventory;
  private final String craftingTitle;
  private final InventoryComponent craftingInventory;
  private final String dialogId;

  private int x;
  private int y;
  private int width = DEFAULT_WIDTH;
  private int height = DEFAULT_HEIGHT;
  private boolean visible = true;

  private int pressedButtonIndex = -1;
  private SlotSelection pressedSlotSelection = null;
  private boolean leftButtonDownLastFrame = false;

  LitiengineCraftingDialogOverlay(
    String targetTitle,
    InventoryComponent targetInventory,
    String craftingTitle,
    InventoryComponent craftingInventory,
    String dialogId) {
    this.targetTitle = (targetTitle == null || targetTitle.isBlank()) ? "Inventory" : targetTitle;
    this.targetInventory = targetInventory;
    this.craftingTitle =
      (craftingTitle == null || craftingTitle.isBlank()) ? "Crafting" : craftingTitle;
    this.craftingInventory = craftingInventory;
    this.dialogId = dialogId;
  }

  @Override
  public void render(Graphics2D g) {
    if (!visible) {
      return;
    }

    Item[] targetSlots = targetInventory.items();
    Item[] craftingSlots = craftingInventory.items();

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
        g, targetInventory, targetSlots, leftStartX, infoY);
      LitiengineInventoryGridRenderer.drawInventoryInfo(
        g, craftingInventory, craftingSlots, rightStartX, infoY);

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

      int previewY = gridTop + maxGridHeight + PREVIEW_TOP_GAP;
      List<Rectangle> buttons = buttonBounds(previewY);

      GridLayout leftGrid =
        new GridLayout(InventorySide.TARGET, leftStartX, gridTop, leftColumns, targetSlots);
      GridLayout rightGrid =
        new GridLayout(InventorySide.CRAFTING, rightStartX, gridTop, rightColumns, craftingSlots);

      handleInput(buttons, leftGrid, rightGrid);
      drawRecipePreview(g, previewY, buttons);
    } finally {
      LitiengineDialogOverlaySupport.finishDialog(g, state);
    }
  }

  private void drawRecipePreview(Graphics2D g, int previewY, List<Rectangle> buttons) {
    int previewX = x + LitiengineDialogOverlaySupport.PADDING;
    int previewWidth = width - 2 * LitiengineDialogOverlaySupport.PADDING;

    drawPanelBackground(g, previewX, previewY, previewWidth, PREVIEW_HEIGHT);

    g.setColor(Color.WHITE);
    g.drawString("Recipe Preview", previewX + PREVIEW_PADDING, previewY + 24);

    String previewText = buildRecipePreviewText();
    LitiengineDialogOverlaySupport.drawWrappedText(
      g,
      previewText,
      previewX + PREVIEW_PADDING,
      previewY + 50,
      previewWidth - 2 * PREVIEW_PADDING);

    LitiengineDialogOverlaySupport.drawButton(
      g, buttons.get(0), CRAFT_BUTTON, pressedButtonIndex == 0);
    LitiengineDialogOverlaySupport.drawButton(
      g, buttons.get(1), CANCEL_BUTTON, pressedButtonIndex == 1);
  }

  private String buildRecipePreviewText() {
    Optional<Recipe> recipe = currentRecipe();

    if (recipe.isEmpty()) {
      if (craftingInventory.isEmpty()) {
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
        .map(this::resultLabel)
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

  private Optional<Recipe> currentRecipe() {
    Item[] ingredients =
      Arrays.stream(craftingInventory.items()).filter(Objects::nonNull).toArray(Item[]::new);

    if (ingredients.length == 0) {
      return Optional.empty();
    }

    return Crafting.recipeByIngredients(ingredients);
  }

  private String resultLabel(CraftingResult result) {
    if (result instanceof Item item) {
      return item.displayName();
    }
    return result.resultType().name();
  }

  private void handleInput(List<Rectangle> buttons, GridLayout leftGrid, GridLayout rightGrid) {
    StageHandle stage = Game.stage().orElse(null);
    if (stage == null) {
      pressedButtonIndex = -1;
      pressedSlotSelection = null;
      leftButtonDownLastFrame = false;
      return;
    }

    int mouseX = stage.mouseX();
    int mouseY = stage.mouseY();
    boolean leftButtonDown = InputManager.isButtonPressed(MouseButtons.LEFT);

    if (leftButtonDown && !leftButtonDownLastFrame) {
      pressedButtonIndex = buttonIndexAt(mouseX, mouseY, buttons);

      if (pressedButtonIndex < 0) {
        pressedSlotSelection = findSlotSelection(mouseX, mouseY, leftGrid, rightGrid);
      } else {
        pressedSlotSelection = null;
      }
    }

    if (!leftButtonDown && leftButtonDownLastFrame) {
      int releasedButtonIndex = buttonIndexAt(mouseX, mouseY, buttons);
      int previouslyPressedButton = pressedButtonIndex;
      pressedButtonIndex = -1;

      if (previouslyPressedButton >= 0 && previouslyPressedButton == releasedButtonIndex) {
        if (previouslyPressedButton == 0) {
          onCraft();
        } else if (previouslyPressedButton == 1) {
          onCancel();
        }

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

  private int buttonIndexAt(int mouseX, int mouseY, List<Rectangle> buttons) {
    for (int i = 0; i < buttons.size(); i++) {
      if (buttons.get(i).contains(mouseX, mouseY)) {
        return i;
      }
    }
    return -1;
  }

  private void transferClickedItem(SlotSelection slotSelection) {
    InventoryComponent source =
      slotSelection.side() == InventorySide.TARGET ? targetInventory : craftingInventory;
    InventoryComponent destination =
      slotSelection.side() == InventorySide.TARGET ? craftingInventory : targetInventory;

    Item item = source.get(slotSelection.slotIndex()).orElse(null);
    if (item == null) {
      return;
    }

    source.transfer(item, destination);
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

  private int findButtonIndex(int mouseX, int mouseY, List<Rectangle> buttons) {
    for (int i = 0; i < buttons.size(); i++) {
      if (buttons.get(i).contains(mouseX, mouseY)) {
        return i;
      }
    }
    return -1;
  }

  private void onCraft() {
    DialogCallbackResolver.createButtonCallback(dialogId, CALLBACK_CRAFT)
      .accept(craftingInventory.items());
  }

  private void onCancel() {
    DialogCallbackResolver.createButtonCallback(dialogId, CALLBACK_CANCEL).accept(null);
  }

  private List<Rectangle> buttonBounds(int previewY) {
    int previewX = x + LitiengineDialogOverlaySupport.PADDING;
    int previewWidth = width - 2 * LitiengineDialogOverlaySupport.PADDING;

    return LitiengineDialogOverlaySupport.centeredButtonRow(
      previewX, previewY, previewWidth, PREVIEW_HEIGHT, 2, BUTTON_GAP);
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
    TARGET,
    CRAFTING
  }

  private record SlotSelection(InventorySide side, int slotIndex) {}

  private record GridLayout(
    InventorySide side, int startX, int startY, int columns, Item[] slots) {}
}
