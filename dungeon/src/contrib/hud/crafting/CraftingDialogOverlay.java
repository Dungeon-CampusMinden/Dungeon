package contrib.hud.crafting;

import contrib.components.InventoryComponent;
import contrib.crafting.CraftingType;
import contrib.hud.InventoryDialogProvider;
import contrib.hud.itemgrid.BaseItemGridOverlay;
import contrib.hud.itemgrid.ItemGridHitTest;
import contrib.hud.itemgrid.InventoryGridRenderer;
import contrib.hud.itemgrid.InventoryPanelRenderer;
import contrib.item.Item;
import core.Game;
import core.input.MouseButtons;
import core.ui.StageHandle;
import core.utils.InputManager;
import java.awt.Color;
import java.awt.Graphics2D;
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
 * <p>It extends a shared dialog overlay base and implements {@code InventoryDialogProvider} to
 * supply associated inventory components.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Rendering the crafting dialog overlay with inventory panels, crafting slots, and result items.</li>
 *   <li>Handling user input for dragging, dropping, and crafting actions.</li>
 *   <li>Displaying dynamic tooltips and interactive feedback within the crafting dialog.</li>
 *   <li>Managing visibility, dimensions, and layout of the overlay.</li>
 * </ul>
 */
final class CraftingDialogOverlay
    extends BaseItemGridOverlay<
        CraftingDialogLayoutState.Measurement, CraftingDialogOverlay.CraftingRenderState>
    implements InventoryDialogProvider {

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
  protected CraftingDialogLayoutState.Measurement measureDialog() {
    return CraftingDialogLayoutState.measure(controller.targetSlots());
  }

  @Override
  protected int dialogWidth(CraftingDialogLayoutState.Measurement measurement) {
    return measurement.dialogWidth();
  }

  @Override
  protected int dialogHeight(CraftingDialogLayoutState.Measurement measurement) {
    return measurement.dialogHeight();
  }

  @Override
  protected String dialogTitle() {
    return "Crafting";
  }

  @Override
  protected CraftingRenderState renderContent(
      Graphics2D g, int contentY, CraftingDialogLayoutState.Measurement measurement) {
    Item[] targetSlots = controller.targetSlots();
    Item[] craftingSlots = controller.craftingSlots();
    Item[] resultItems = currentResultItems();

    Item[] visibleTargetSlots =
        dragDropController.visibleSlots(targetSlots, CraftingInventorySide.TARGET);
    Item[] visibleCraftingSlots =
        dragDropController.visibleSlots(craftingSlots, CraftingInventorySide.CRAFTING);

    CraftingDialogLayoutState layoutState =
        CraftingDialogLayoutState.create(
            x, contentY, g.getFontMetrics(), measurement, visibleTargetSlots);

    g.setColor(Color.WHITE);
    g.drawString(targetTitle, layoutState.leftTitleX(), layoutState.titleBaseline());
    g.drawString(craftingTitle, layoutState.craftingTitleX(), layoutState.titleBaseline());

    InventoryPanelRenderer.drawPanelBackground(g, layoutState.leftPanelBounds());

    ItemGridHitTest.Grid<CraftingInventorySide> leftGrid = layoutState.leftGrid();
    InventoryGridRenderer.drawGrid(
        g, visibleTargetSlots, leftGrid.startX(), leftGrid.startY(), leftGrid.columns());

    List<CraftingDialogLayout.SlotBounds> craftingBounds =
        panelRenderer.craftingSlotBounds(layoutState.rightPanelBounds(), craftingSlots);
    List<CraftingDialogLayout.ItemBounds> resultBounds =
        panelRenderer.resultItemBounds(layoutState.rightPanelBounds(), resultItems);

    actionRenderer.syncButtonBounds(actionRenderer.buttonBounds(layoutState.rightPanelBounds()));
    panelRenderer.draw(
        g,
        layoutState.rightPanelBounds(),
        craftingBounds,
        visibleCraftingSlots,
        resultItems,
        resultBounds);
    actionRenderer.draw(g);

    return new CraftingRenderState(layoutState, leftGrid, craftingBounds, resultItems, resultBounds);
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

  @Override
  protected void handleInput(CraftingRenderState content) {
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
            content.leftGrid(),
            content.layoutState().leftPanelBounds(),
            content.layoutState().rightPanelBounds(),
            content.craftingBounds(),
            actionRenderer::findActionAt);
    action.ifPresent(actionRenderer::trigger);
  }

  @Override
  protected void drawPointerFeedback(Graphics2D g, CraftingRenderState content) {
    if (dragDropController.isDragging()) {
      drawDropHighlights(g, content);
      dragDropController.drawDragPreview(g);
      return;
    }

    tooltipController.drawHoverTooltip(
        g,
        bounds(),
        content.leftGrid(),
        content.craftingBounds(),
        content.resultItems(),
        content.resultBounds());
  }

  private void drawDropHighlights(Graphics2D g, CraftingRenderState content) {
    StageHandle stage = Game.stage().orElse(null);
    if (stage == null) {
      return;
    }

    dragDropController.drawDropHighlights(
        g,
        content.leftGrid(),
        content.layoutState().leftPanelBounds(),
        content.layoutState().rightPanelBounds(),
        content.craftingBounds(),
        stage.mouseX(),
        stage.mouseY());
  }

  @Override
  public Stream<InventoryComponent> inventoryComponents() {
    return Stream.of(controller.targetInventory(), controller.craftingInventory());
  }

  record CraftingRenderState(
      CraftingDialogLayoutState layoutState,
      ItemGridHitTest.Grid<CraftingInventorySide> leftGrid,
      List<CraftingDialogLayout.SlotBounds> craftingBounds,
      Item[] resultItems,
      List<CraftingDialogLayout.ItemBounds> resultBounds) {}
}
