package contrib.hud.crafting;

import contrib.components.InventoryComponent;
import contrib.crafting.CraftingType;
import contrib.hud.inventory.InventoryComponentProvider;
import contrib.hud.renderers.DialogFrameRenderer;
import contrib.hud.renderers.InventoryGridRenderer;
import contrib.hud.renderers.InventoryPanelRenderer;
import contrib.hud.utils.GridHitTest;
import contrib.item.Item;
import core.Game;
import core.input.MouseButtons;
import core.ui.StageHandle;
import core.ui.overlay.BaseUiOverlay;
import core.utils.InputManager;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Represents a dialog overlay component in the user interface for crafting items.
 *
 * <p>This overlay handles rendering, input processing, and managing the crafting UI logic, such as displaying
 * inventory slots, crafting slots, crafting results, and tooltips. It also provides drag-and-drop
 * functionality and interacts with the crafting system to facilitate item crafting.
 *
 * <p>The overlay operates based on the provided crafting and target inventories, as managed by the
 * {@code CraftingDialogController}. It dynamically calculates and adjusts its layout and dimensions
 * based on the crafting requirements and the visible inventory slots.
 *
 * <p>This class ensures responsiveness to user interactions, including mouse input, drag-and-drop
 * operations, and tooltip rendering for crafting items. It maintains an internal state for the
 * visibility, dimensions, and positioning of the dialog.
 *
 * <p>Extends {@code BaseUiOverlay} to represent a customizable UI component and the
 * {@code InventoryComponentProvider} interface to supply associated inventory components.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Rendering the crafting dialog overlay with inventory panels, crafting slots, and result items.</li>
 *   <li>Handling user input for dragging, dropping, and crafting actions.</li>
 *   <li>Displaying dynamic tooltips and interactive feedback within the crafting dialog.</li>
 *   <li>Managing visibility, dimensions, and layout of the overlay.</li>
 * </ul>
 */
final class CraftingDialogOverlay extends BaseUiOverlay implements InventoryComponentProvider {

  private static final CraftingDialogLayout CLASSIC_LAYOUT = new CraftingDialogLayout();

  private final String targetTitle;
  private final String craftingTitle;
  private final CraftingDialogController controller;
  private final CraftingDragDropController dragDropController;
  private final CraftingActionRenderer actionRenderer;
  private final CraftingPanelRenderer panelRenderer;
  private final CraftingTooltipController tooltipController;

  CraftingDialogOverlay(
      String targetTitle,
      String craftingTitle,
      CraftingDialogController controller,
      String dialogId) {
    super(CraftingDialogLayoutState.DEFAULT_WIDTH, CraftingDialogLayoutState.DEFAULT_HEIGHT);
    this.targetTitle = (targetTitle == null || targetTitle.isBlank()) ? "Inventory" : targetTitle;
    this.craftingTitle =
        (craftingTitle == null || craftingTitle.isBlank()) ? "Crafting" : craftingTitle;
    this.controller = controller;
    this.dragDropController = new CraftingDragDropController(controller);
    this.actionRenderer = new CraftingActionRenderer(dialogId, controller);
    this.panelRenderer = new CraftingPanelRenderer(CLASSIC_LAYOUT);
    this.tooltipController = new CraftingTooltipController(dragDropController);
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
        dragDropController.visibleSlots(targetSlots, CraftingInventorySide.TARGET);
    Item[] visibleCraftingSlots =
        dragDropController.visibleSlots(craftingSlots, CraftingInventorySide.CRAFTING);

    CraftingDialogLayoutState.Measurement measurement =
        CraftingDialogLayoutState.measure(targetSlots);
    width = measurement.dialogWidth();
    height = measurement.dialogHeight();

    centerInIfUnpositioned(Game.windowWidth(), Game.windowHeight());

    int contentY;
    List<CraftingDialogLayout.SlotBounds> craftingBounds;
    List<CraftingDialogLayout.ItemBounds> resultBounds;

    DialogFrameRenderer.RenderState state = DialogFrameRenderer.beginDialog(g);

    try {
      contentY = DialogFrameRenderer.drawFrameAndTitle(g, x, y, width, height, "Crafting");

      CraftingDialogLayoutState layoutState =
          CraftingDialogLayoutState.create(
              x, contentY, g.getFontMetrics(), measurement, visibleTargetSlots);

      g.setColor(Color.WHITE);
      g.drawString(targetTitle, layoutState.leftTitleX(), layoutState.titleBaseline());
      g.drawString(craftingTitle, layoutState.craftingTitleX(), layoutState.titleBaseline());

      InventoryPanelRenderer.drawPanelBackground(g, layoutState.leftPanelBounds());

      GridHitTest.Grid<CraftingInventorySide> leftGrid = layoutState.leftGrid();
      InventoryGridRenderer.drawGrid(
          g, visibleTargetSlots, leftGrid.startX(), leftGrid.startY(), leftGrid.columns());

      craftingBounds =
          panelRenderer.craftingSlotBounds(layoutState.rightPanelBounds(), craftingSlots);

      resultBounds = panelRenderer.resultItemBounds(layoutState.rightPanelBounds(), resultItems);

      actionRenderer.syncButtonBounds(actionRenderer.buttonBounds(layoutState.rightPanelBounds()));
      panelRenderer.draw(
          g,
          layoutState.rightPanelBounds(),
          craftingBounds,
          visibleCraftingSlots,
          resultItems,
          resultBounds);
      actionRenderer.draw(g);

      if (dragDropController.isDragging()) {
        drawDropHighlights(
            g,
            leftGrid,
            layoutState.leftPanelBounds(),
            layoutState.rightPanelBounds(),
            craftingBounds);
      }

      handleInput(
          leftGrid, layoutState.leftPanelBounds(), layoutState.rightPanelBounds(), craftingBounds);

      if (dragDropController.isDragging()) {
        dragDropController.drawDragPreview(g);
      } else {
        tooltipController.drawHoverTooltip(
            g, bounds(), leftGrid, craftingBounds, resultItems, resultBounds);
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
      GridHitTest.Grid<CraftingInventorySide> leftGrid,
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
      GridHitTest.Grid<CraftingInventorySide> leftGrid,
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

  @Override
  public Stream<InventoryComponent> inventoryComponents() {
    return Stream.of(controller.targetInventory(), controller.craftingInventory());
  }
}
