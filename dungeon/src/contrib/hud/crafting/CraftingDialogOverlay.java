package contrib.hud.crafting;

import contrib.components.InventoryComponent;
import contrib.crafting.CraftingType;
import contrib.hud.crafting.CraftingDragDropController.InventorySide;
import contrib.hud.inventory.InventoryComponentProvider;
import contrib.hud.renderers.DialogFrameRenderer;
import contrib.hud.renderers.InventoryGridRenderer;
import contrib.hud.renderers.InventoryPanelRendering;
import contrib.hud.utils.GridHitTest;
import contrib.hud.utils.InventoryTooltip;
import contrib.item.Item;
import core.Game;
import core.input.MouseButtons;
import core.ui.StageHandle;
import core.ui.overlay.UiOverlay;
import core.utils.InputManager;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Represents a crafting dialog interface overlay rendered on top of the game scene.
 *
 * <p>This overlay contains all visual elements and interactive logic required for crafting, such as
 * crafting panels, inventory grids, action buttons, item icons, and drag/drop support.
 *
 * <p>It allows players to interact with their crafting inventory and recipes, displaying results
 * and processing input for crafting actions.
 *
 * <p>This class extends the functionality provided by {@code UiOverlay} and {@code
 * InventoryComponentProvider}, integrating advanced UI behaviors such as rendering, hit detection,
 * and inventory component management.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li>Custom rendering of crafting panels and inventory grids.
 *   <li>Drag-and-drop functionality for transferring items between inventory slots.
 *   <li>Interactive action buttons for crafting-related operations.
 *   <li>Support for legacy and modern crafting panel layouts.
 *   <li>Hover tooltips for items and slot highlights during interactions.
 * </ul>
 */
final class CraftingDialogOverlay implements UiOverlay, InventoryComponentProvider {

  private static final int DEFAULT_WIDTH = 1180;
  private static final int DEFAULT_HEIGHT = 600;

  private static final int PANEL_GAP = 26;
  private static final int PANEL_HEADER_GAP = 14;
  private static final int PANEL_PADDING = 12;
  private static final int CLASSIC_CRAFTING_PANEL_WIDTH = 420;
  private static final int CLASSIC_CRAFTING_PANEL_HEIGHT = 420;

  private static final CraftingDialogLayout CLASSIC_LAYOUT = new CraftingDialogLayout();

  private final String targetTitle;
  private final String craftingTitle;
  private final CraftingDialogController controller;
  private final CraftingDragDropController dragDropController;
  private final CraftingActionRenderer actionRenderer;
  private final CraftingPanelRenderer panelRenderer;

  private int x;
  private int y;
  private int width = DEFAULT_WIDTH;
  private int height = DEFAULT_HEIGHT;
  private boolean visible = true;

  CraftingDialogOverlay(
      String targetTitle,
      String craftingTitle,
      CraftingDialogController controller,
      String dialogId) {
    this.targetTitle = (targetTitle == null || targetTitle.isBlank()) ? "Inventory" : targetTitle;
    this.craftingTitle =
        (craftingTitle == null || craftingTitle.isBlank()) ? "Crafting" : craftingTitle;
    this.controller = controller;
    this.dragDropController = new CraftingDragDropController(controller);
    this.actionRenderer = new CraftingActionRenderer(dialogId, controller);
    this.panelRenderer = new CraftingPanelRenderer(CLASSIC_LAYOUT);
  }

  @Override
  public void render(Graphics2D g) {
    if (!visible) {
      return;
    }

    Item[] targetSlots = controller.targetSlots();
    Item[] craftingSlots = controller.craftingSlots();
    Item[] resultItems = currentResultItems();

    Item[] visibleTargetSlots =
        dragDropController.visibleSlots(targetSlots, InventorySide.TARGET);
    Item[] visibleCraftingSlots =
        dragDropController.visibleSlots(craftingSlots, InventorySide.CRAFTING);

    int leftColumns = InventoryGridRenderer.columnsFor(targetSlots);
    int leftRows = InventoryGridRenderer.rowsFor(targetSlots, leftColumns);
    int leftGridWidth = InventoryGridRenderer.gridWidth(leftColumns);
    int leftGridHeight = InventoryGridRenderer.gridHeight(leftRows);

    int rightPanelWidth = CLASSIC_CRAFTING_PANEL_WIDTH;
    int rightPanelHeight =
        Math.max(CLASSIC_CRAFTING_PANEL_HEIGHT, leftGridHeight + 2 * PANEL_PADDING);

    int totalContentWidth = leftGridWidth + PANEL_GAP + rightPanelWidth;
    width = Math.max(DEFAULT_WIDTH, totalContentWidth + 2 * DialogFrameRenderer.PADDING);

    height =
        Math.max(
            DEFAULT_HEIGHT,
            120
                + Math.max(leftGridHeight + 2 * PANEL_PADDING, rightPanelHeight)
                + DialogFrameRenderer.PADDING);

    if (x == 0 && y == 0) {
      x = (Game.windowWidth() - width) / 2;
      y = (Game.windowHeight() - height) / 2;
    }

    int contentY;
    int leftStartX;
    int gridTop;

    GridHitTest.Grid<InventorySide> leftGrid;
    Rectangle leftPanelBounds;
    Rectangle rightPanelBounds;
    List<CraftingDialogLayout.SlotBounds> craftingBounds;
    List<CraftingDialogLayout.ItemBounds> resultBounds;

    DialogFrameRenderer.RenderState state = DialogFrameRenderer.beginDialog(g);

    try {
      contentY = DialogFrameRenderer.drawFrameAndTitle(g, x, y, width, height, "Crafting");

      int titleBaseline = contentY + g.getFontMetrics().getAscent();

      leftStartX = x + (width - totalContentWidth) / 2;
      int rightPanelX = leftStartX + leftGridWidth + PANEL_GAP;

      g.setColor(Color.WHITE);
      g.drawString(targetTitle, leftStartX, titleBaseline);
      g.drawString(craftingTitle, rightPanelX + PANEL_PADDING, titleBaseline);

      gridTop = titleBaseline + PANEL_HEADER_GAP + InventoryGridRenderer.GRID_TOP_GAP;

      leftPanelBounds =
          InventoryPanelRendering.panelBounds(
              leftStartX, gridTop, leftGridWidth, leftGridHeight, PANEL_PADDING);

      rightPanelBounds =
          new Rectangle(rightPanelX, gridTop - PANEL_PADDING, rightPanelWidth, rightPanelHeight);

      InventoryPanelRendering.drawPanelBackground(g, leftPanelBounds);

      leftGrid =
          new GridHitTest.Grid<>(
              InventorySide.TARGET, leftStartX, gridTop, leftColumns, visibleTargetSlots);
      InventoryGridRenderer.drawGrid(g, visibleTargetSlots, leftStartX, gridTop, leftColumns);

      craftingBounds =
          panelRenderer.craftingSlotBounds(rightPanelBounds, craftingSlots);

      resultBounds = panelRenderer.resultItemBounds(rightPanelBounds, resultItems);

      actionRenderer.syncButtonBounds(actionRenderer.buttonBounds(rightPanelBounds));
      panelRenderer.draw(
          g, rightPanelBounds, craftingBounds, visibleCraftingSlots, resultItems, resultBounds);
      actionRenderer.draw(g);

      if (dragDropController.isDragging()) {
        drawDropHighlights(g, leftGrid, leftPanelBounds, rightPanelBounds, craftingBounds);
      }

      handleInput(leftGrid, leftPanelBounds, rightPanelBounds, craftingBounds);

      if (dragDropController.isDragging()) {
        dragDropController.drawDragPreview(g);
      } else {
        drawHoverTooltip(g, leftGrid, craftingBounds, resultItems, resultBounds);
      }
    } finally {
      DialogFrameRenderer.finishDialog(g, state);
    }
  }

  private Item[] currentResultItems() {
    return controller
        .currentRecipe()
        .map(
            recipe ->
                Arrays.stream(recipe.results())
                    .filter(
                        result ->
                            result.resultType() == CraftingType.ITEM && result instanceof Item)
                    .map(Item.class::cast)
                    .toArray(Item[]::new))
        .orElseGet(() -> new Item[0]);
  }

  private void handleInput(
      GridHitTest.Grid<InventorySide> leftGrid,
      Rectangle leftPanelBounds,
      Rectangle rightPanelBounds,
      List<CraftingDialogLayout.SlotBounds> craftingBounds) {
    StageHandle stage = Game.stage().orElse(null);
    if (stage == null) {
      dragDropController.reset();
      return;
    }

    int mouseX = stage.mouseX();
    int mouseY = stage.mouseY();
    boolean leftButtonDown = InputManager.isButtonPressed(MouseButtons.LEFT);

    Optional<CraftingDialogAction> action =
        dragDropController.handleInput(
            leftButtonDown,
            mouseX,
            mouseY,
            leftGrid,
            leftPanelBounds,
            rightPanelBounds,
            craftingBounds,
            actionRenderer::findActionAt);
    action.ifPresent(actionRenderer::trigger);
  }

  private void drawDropHighlights(
      Graphics2D g,
      GridHitTest.Grid<InventorySide> leftGrid,
      Rectangle leftPanelBounds,
      Rectangle rightPanelBounds,
      List<CraftingDialogLayout.SlotBounds> craftingBounds) {
    StageHandle stage = Game.stage().orElse(null);
    if (stage == null) {
      return;
    }

    dragDropController.drawDropHighlights(
        g,
        leftGrid,
        leftPanelBounds,
        rightPanelBounds,
        craftingBounds,
        stage.mouseX(),
        stage.mouseY());
  }

  private void drawHoverTooltip(
      Graphics2D g,
      GridHitTest.Grid<InventorySide> leftGrid,
      List<CraftingDialogLayout.SlotBounds> craftingBounds,
      Item[] resultItems,
      List<CraftingDialogLayout.ItemBounds> resultBounds) {
    if (InventoryTooltip.drawHoveredSlotTooltip(
        g,
        dialogBounds(),
        (mouseX, mouseY) ->
            dragDropController.findSlotSelection(mouseX, mouseY, leftGrid, craftingBounds),
        dragDropController::itemOf)) {
      return;
    }

    for (int i = 0; i < resultBounds.size() && i < resultItems.length; i++) {
      CraftingDialogLayout.ItemBounds bounds = resultBounds.get(i);
      Rectangle rect = new Rectangle(bounds.x(), bounds.y(), bounds.size(), bounds.size());
      if (InventoryTooltip.drawItemTooltip(g, rect, resultItems[i])) {
        return;
      }
    }
  }

  private Rectangle dialogBounds() {
    return new Rectangle(x, y, width, height);
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
    return Stream.of(controller.targetInventory(), controller.craftingInventory());
  }
}
