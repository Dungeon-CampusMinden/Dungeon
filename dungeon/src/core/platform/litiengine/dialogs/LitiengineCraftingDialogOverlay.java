package core.platform.litiengine.dialogs;

import contrib.crafting.CraftingDialogAction;
import contrib.hud.dialogs.DialogCallbackResolver;
import contrib.hud.elements.ImageButton;
import contrib.item.Item;
import contrib.crafting.CraftingDialogController;
import contrib.crafting.CraftingDialogInteraction;
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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Crafting overlay for the LITIENGINE backend.
 *
 * <p>This version shows the target inventory, the crafting input inventory, a recipe preview,
 * real Craft/Cancel buttons, drag-based slot interaction and exact slot-to-slot drop feedback.
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

  private static final int DRAG_THRESHOLD_PX = 8;
  private static final int DRAG_PREVIEW_OFFSET_X = 14;
  private static final int DRAG_PREVIEW_OFFSET_Y = 18;
  private static final int DRAG_PREVIEW_PADDING_X = 10;
  private static final int DRAG_PREVIEW_PADDING_Y = 7;
  private static final int DRAG_TARGET_INSET = 3;
  private static final int DRAG_TARGET_ARC = 8;

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
  private int pressedMouseX = 0;
  private int pressedMouseY = 0;
  private DragState dragState = null;

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
        new ImageButton(new Animation(new SimpleIPath(action.iconPath())), 0, 0, 1, 1);

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
      leftGridWidth + rightGridWidth + PANEL_GAP + 2 * LitiengineDialogOverlaySupport.PADDING;
    width = Math.max(DEFAULT_WIDTH, contentWidth);

    int maxGridHeight =
      Math.max(
        LitiengineInventoryGridRenderer.gridHeight(leftRows),
        LitiengineInventoryGridRenderer.gridHeight(rightRows));

    height =
      Math.max(
        DEFAULT_HEIGHT,
        210
          + maxGridHeight
          + PREVIEW_TOP_GAP
          + PREVIEW_HEIGHT
          + LitiengineDialogOverlaySupport.PADDING);

    if (x == 0 && y == 0) {
      x = (Game.windowWidth() - width) / 2;
      y = (Game.windowHeight() - height) / 2;
    }

    int contentY;
    int leftStartX;
    int rightStartX;
    int gridTop;

    GridLayout leftGrid;
    GridLayout rightGrid;

    LitiengineDialogOverlaySupport.RenderState state =
      LitiengineDialogOverlaySupport.beginDialog(g);

    try {
      contentY =
        LitiengineDialogOverlaySupport.drawFrameAndTitle(g, x, y, width, height, "Crafting");

      int totalGridWidth = leftGridWidth + PANEL_GAP + rightGridWidth;
      leftStartX = x + (width - totalGridWidth) / 2;
      rightStartX = leftStartX + leftGridWidth + PANEL_GAP;

      int titleBaseline = contentY + g.getFontMetrics().getAscent();
      g.setColor(Color.WHITE);
      g.drawString(targetTitle, leftStartX, titleBaseline);
      g.drawString(craftingTitle, rightStartX, titleBaseline);

      int infoY = contentY + PANEL_HEADER_GAP + LitiengineInventoryGridRenderer.INFO_LINE_GAP;

      LitiengineInventoryGridRenderer.drawInventoryInfo(
        g, controller.targetInventory(), targetSlots, leftStartX, infoY);
      LitiengineInventoryGridRenderer.drawInventoryInfo(
        g, controller.craftingInventory(), craftingSlots, rightStartX, infoY);

      gridTop =
        infoY
          + LitiengineInventoryGridRenderer.GRID_TOP_GAP
          + LitiengineInventoryGridRenderer.INFO_LINE_GAP;

      int leftGridHeight = LitiengineInventoryGridRenderer.gridHeight(leftRows);
      int rightGridHeight = LitiengineInventoryGridRenderer.gridHeight(rightRows);

      Rectangle leftPanelBounds =
        new Rectangle(
          leftStartX - 12, gridTop - 12, leftGridWidth + 24, leftGridHeight + 24);

      Rectangle rightPanelBounds =
        new Rectangle(
          rightStartX - 12, gridTop - 12, rightGridWidth + 24, rightGridHeight + 24);

      drawPanelBackground(
        g,
        leftPanelBounds.x,
        leftPanelBounds.y,
        leftPanelBounds.width,
        leftPanelBounds.height);
      drawPanelBackground(
        g,
        rightPanelBounds.x,
        rightPanelBounds.y,
        rightPanelBounds.width,
        rightPanelBounds.height);

      LitiengineInventoryGridRenderer.drawGrid(g, targetSlots, leftStartX, gridTop, leftColumns);
      LitiengineInventoryGridRenderer.drawGrid(
        g, craftingSlots, rightStartX, gridTop, rightColumns);

      Rectangle previewPanelBounds = previewPanelBounds(gridTop, maxGridHeight);
      List<Rectangle> buttonBounds = buttonBounds(previewPanelBounds.y);
      syncActionButtonBounds(buttonBounds);

      leftGrid = new GridLayout(InventorySide.TARGET, leftStartX, gridTop, leftColumns, targetSlots);
      rightGrid =
        new GridLayout(InventorySide.CRAFTING, rightStartX, gridTop, rightColumns, craftingSlots);

      if (dragState != null) {
        SlotSelection hoveredTarget = hoveredDropTarget(leftGrid, rightGrid);
        if (hoveredTarget != null) {
          drawDropTargetHighlight(g, hoveredTarget, leftGrid, rightGrid);
        }
      }

      handleInput(leftGrid, rightGrid);
      drawRecipePreview(g, previewPanelBounds, buttonBounds);

      if (dragState != null) {
        drawDragPreview(g);
      } else {
        drawHoverTooltip(g, leftGrid, rightGrid);
      }
    } finally {
      LitiengineDialogOverlaySupport.finishDialog(g, state);
    }
  }

  private void handleInput(GridLayout leftGrid, GridLayout rightGrid) {
    StageHandle stage = Game.stage().orElse(null);
    if (stage == null) {
      pressedSlotSelection = null;
      dragState = null;
      leftButtonDownLastFrame = false;
      return;
    }

    int mouseX = stage.mouseX();
    int mouseY = stage.mouseY();
    boolean leftButtonDown = InputManager.isButtonPressed(MouseButtons.LEFT);

    if (leftButtonDown && !leftButtonDownLastFrame) {
      dragState = null;

      if (findActionButtonAt(mouseX, mouseY).isPresent()) {
        pressedSlotSelection = null;
      } else {
        pressedSlotSelection = findSlotSelection(mouseX, mouseY, leftGrid, rightGrid);
        pressedMouseX = mouseX;
        pressedMouseY = mouseY;
      }
    } else if (leftButtonDown) {
      maybeStartDrag(mouseX, mouseY);
    }

    if (!leftButtonDown && leftButtonDownLastFrame) {
      Optional<CraftingDialogAction> releasedButton = findActionButtonAt(mouseX, mouseY);
      if (releasedButton.isPresent() && dragState == null) {
        pressedSlotSelection = null;
        triggerActionButton(releasedButton.get());
        leftButtonDownLastFrame = leftButtonDown;
        return;
      }

      SlotSelection releasedSlotSelection = findSlotSelection(mouseX, mouseY, leftGrid, rightGrid);
      SlotSelection previouslyPressedSlot = pressedSlotSelection;
      DragState completedDrag = dragState;

      pressedSlotSelection = null;
      dragState = null;

      if (completedDrag != null) {
        transferDraggedItem(completedDrag, releasedSlotSelection);
      } else if (previouslyPressedSlot != null && previouslyPressedSlot.equals(releasedSlotSelection)) {
        transferClickedItem(previouslyPressedSlot);
      }
    }

    leftButtonDownLastFrame = leftButtonDown;
  }

  private void maybeStartDrag(int mouseX, int mouseY) {
    if (dragState != null || pressedSlotSelection == null) {
      return;
    }

    int deltaX = mouseX - pressedMouseX;
    int deltaY = mouseY - pressedMouseY;
    int thresholdSquared = DRAG_THRESHOLD_PX * DRAG_THRESHOLD_PX;

    if ((deltaX * deltaX) + (deltaY * deltaY) < thresholdSquared) {
      return;
    }

    Item draggedItem = itemAt(pressedSlotSelection);
    if (draggedItem == null) {
      pressedSlotSelection = null;
      return;
    }

    dragState = new DragState(pressedSlotSelection, draggedItem);
  }

  private void transferClickedItem(SlotSelection slotSelection) {
    interaction.transferClickedSlot(slotSelection.side().controllerSide(), slotSelection.slotIndex());
  }

  private void transferDraggedItem(DragState drag, SlotSelection releasedSlotSelection) {
    if (releasedSlotSelection == null) {
      return;
    }

    if (releasedSlotSelection.side() == drag.source().side()) {
      return;
    }

    interaction.transferDroppedSlot(
      drag.source().side().controllerSide(),
      drag.source().slotIndex(),
      releasedSlotSelection.side().controllerSide(),
      releasedSlotSelection.slotIndex());
  }

  private Item itemAt(SlotSelection slotSelection) {
    if (slotSelection == null) {
      return null;
    }

    Item[] slots =
      slotSelection.side() == InventorySide.TARGET
        ? controller.targetSlots()
        : controller.craftingSlots();

    int slotIndex = slotSelection.slotIndex();
    if (slotIndex < 0 || slotIndex >= slots.length) {
      return null;
    }

    return slots[slotIndex];
  }

  private SlotSelection hoveredDropTarget(GridLayout leftGrid, GridLayout rightGrid) {
    if (dragState == null) {
      return null;
    }

    StageHandle stage = Game.stage().orElse(null);
    if (stage == null) {
      return null;
    }

    SlotSelection hovered = findSlotSelection(stage.mouseX(), stage.mouseY(), leftGrid, rightGrid);

    if (hovered == null) {
      return null;
    }

    if (hovered.side() == dragState.source().side()) {
      return null;
    }

    return hovered;
  }

  private void drawHoverTooltip(Graphics2D g, GridLayout leftGrid, GridLayout rightGrid) {
    StageHandle stage = Game.stage().orElse(null);
    if (stage == null) {
      return;
    }

    int mouseX = stage.mouseX();
    int mouseY = stage.mouseY();

    if (mouseX < x || mouseX > x + width || mouseY < y || mouseY > y + height) {
      return;
    }

    if (findActionButtonAt(mouseX, mouseY).isPresent()) {
      return;
    }

    SlotSelection hoveredSlot = findSlotSelection(mouseX, mouseY, leftGrid, rightGrid);
    if (hoveredSlot == null) {
      return;
    }

    Item hoveredItem = itemAt(hoveredSlot);
    if (hoveredItem == null) {
      return;
    }

    LitiengineItemTooltipSupport.drawTooltip(
      g, hoveredItem, mouseX, mouseY, (int) stage.getWidth(), (int) stage.getHeight());
  }

  private Optional<CraftingDialogAction> findActionButtonAt(int mouseX, int mouseY) {
    return actionButtons.entrySet().stream()
      .filter(entry -> pointInsideButton(entry.getValue(), mouseX, mouseY))
      .map(Map.Entry::getKey)
      .findFirst();
  }

  private boolean pointInsideButton(ImageButton button, int mouseX, int mouseY) {
    return mouseX >= button.x()
      && mouseX <= button.x() + button.width()
      && mouseY >= button.y()
      && mouseY <= button.y() + button.height();
  }

  private void triggerActionButton(CraftingDialogAction action) {
    if (action == null) {
      return;
    }

    DialogCallbackResolver.createButtonCallback(dialogId, action.callbackKey())
      .accept(action == CraftingDialogAction.CRAFT ? controller.craftingPayload() : null);
  }

  private void syncActionButtonBounds(List<Rectangle> buttonBounds) {
    CraftingDialogAction[] actions = CraftingDialogAction.values();
    for (int i = 0; i < actions.length && i < buttonBounds.size(); i++) {
      Rectangle bounds = buttonBounds.get(i);
      ImageButton button = actionButtons.get(actions[i]);
      button.x(bounds.x);
      button.y(bounds.y);
      button.width(bounds.width);
      button.height(bounds.height);
    }
  }

  private Rectangle previewPanelBounds(int gridTop, int maxGridHeight) {
    int previewY = gridTop + maxGridHeight + PREVIEW_TOP_GAP;
    int previewX = x + LitiengineDialogOverlaySupport.PADDING;
    int previewWidth = width - 2 * LitiengineDialogOverlaySupport.PADDING;
    return new Rectangle(previewX, previewY, previewWidth, PREVIEW_HEIGHT);
  }

  private List<Rectangle> buttonBounds(int previewY) {
    int buttonSize = 64;
    int totalWidth =
      CraftingDialogAction.values().length * buttonSize
        + (CraftingDialogAction.values().length - 1) * BUTTON_GAP;
    int startX = x + (width - totalWidth) / 2;
    int buttonY = previewY + PREVIEW_HEIGHT - buttonSize - PREVIEW_PADDING;

    return java.util.stream.IntStream.range(0, CraftingDialogAction.values().length)
      .mapToObj(i -> new Rectangle(startX + i * (buttonSize + BUTTON_GAP), buttonY, buttonSize, buttonSize))
      .toList();
  }

  private void drawRecipePreview(Graphics2D g, Rectangle previewPanelBounds, List<Rectangle> buttonBounds) {
    g.setColor(new Color(28, 30, 38, 170));
    g.fillRoundRect(
      previewPanelBounds.x,
      previewPanelBounds.y,
      previewPanelBounds.width,
      previewPanelBounds.height,
      12,
      12);
    g.setColor(new Color(90, 94, 108, 180));
    g.drawRoundRect(
      previewPanelBounds.x,
      previewPanelBounds.y,
      previewPanelBounds.width,
      previewPanelBounds.height,
      12,
      12);

    int textX = previewPanelBounds.x + PREVIEW_PADDING;
    int titleY = previewPanelBounds.y + PREVIEW_PADDING + g.getFontMetrics().getAscent();

    g.setColor(Color.WHITE);
    g.drawString("Preview", textX, titleY);

    String recipeText =
      controller.currentRecipe()
        .map(
          recipe ->
            recipe.results().length > 0
              ? LitiengineItemTooltipSupport.displayName((Item) recipe.results()[0])
              : "No result")
        .orElse("No matching recipe");

    g.setColor(new Color(220, 220, 230));
    g.drawString(recipeText, textX, titleY + g.getFontMetrics().getHeight() + 8);

    for (int i = 0; i < CraftingDialogAction.values().length && i < buttonBounds.size(); i++) {
      Rectangle bounds = buttonBounds.get(i);
      drawActionButton(g, actionButtons.get(CraftingDialogAction.values()[i]), bounds);
    }
  }

  private void drawActionButton(Graphics2D g, ImageButton button, Rectangle bounds) {
    g.setColor(new Color(50, 54, 66, 210));
    g.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 10, 10);
    g.setColor(new Color(160, 166, 184, 220));
    g.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 10, 10);

    Rectangle oldBounds = new Rectangle(button.x(), button.y(), button.width(), button.height());
    button.x(bounds.x);
    button.y(bounds.y);
    button.width(bounds.width);
    button.height(bounds.height);
    LitiengineButtonRenderer.draw(g, button, "");
    button.x(oldBounds.x);
    button.y(oldBounds.y);
    button.width(oldBounds.width);
    button.height(oldBounds.height);
  }

  private SlotSelection findSlotSelection(
    int mouseX, int mouseY, GridLayout leftGrid, GridLayout rightGrid) {
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

  private void drawDropTargetHighlight(
    Graphics2D g, SlotSelection targetSlot, GridLayout leftGrid, GridLayout rightGrid) {
    GridLayout targetGrid = targetSlot.side() == InventorySide.TARGET ? leftGrid : rightGrid;

    Rectangle bounds =
      LitiengineInventoryGridRenderer.slotBounds(
        targetSlot.slotIndex(),
        targetGrid.startX(),
        targetGrid.startY(),
        targetGrid.columns());

    int insetX = bounds.x + DRAG_TARGET_INSET;
    int insetY = bounds.y + DRAG_TARGET_INSET;
    int insetWidth = bounds.width - 2 * DRAG_TARGET_INSET;
    int insetHeight = bounds.height - 2 * DRAG_TARGET_INSET;

    g.setColor(new Color(88, 168, 116, 70));
    g.fillRoundRect(insetX, insetY, insetWidth, insetHeight, DRAG_TARGET_ARC, DRAG_TARGET_ARC);

    g.setColor(new Color(132, 214, 156, 210));
    g.drawRoundRect(insetX, insetY, insetWidth, insetHeight, DRAG_TARGET_ARC, DRAG_TARGET_ARC);
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

    String baseLabel = LitiengineItemTooltipSupport.displayName(item);
    return item.stackSize() > 1 ? baseLabel + " x" + item.stackSize() : baseLabel;
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

    CraftingDialogController.InventorySide controllerSide() {
      return controllerSide;
    }
  }

  private record SlotSelection(InventorySide side, int slotIndex) {}

  private record DragState(SlotSelection source, Item item) {}

  private record GridLayout(
    InventorySide side, int startX, int startY, int columns, Item[] slots) {}
}
