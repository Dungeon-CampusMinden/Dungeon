package contrib.hud.crafting;

import contrib.components.InventoryComponent;
import contrib.crafting.CraftingType;
import contrib.hud.inventory.InventoryComponentProvider;
import contrib.hud.renderers.DialogFrameRenderer;
import contrib.hud.renderers.InventoryGridRenderer;
import contrib.hud.renderers.InventoryPanelRendering;
import contrib.hud.utils.GridHitTest;
import contrib.hud.utils.InventoryDragController;
import contrib.hud.utils.InventoryDropHandling;
import contrib.hud.utils.InventoryTooltip;
import contrib.item.Item;
import core.Game;
import core.input.MouseButtons;
import core.ui.StageHandle;
import core.ui.overlay.UiOverlay;
import core.utils.InputManager;
import java.awt.*;
import java.util.ArrayList;
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

  private static final int DRAG_THRESHOLD_PX = 8;

  private static final Color DRAG_HIGHLIGHT = new Color(157, 193, 235, 180);
  private static final Color DRAG_HIGHLIGHT_FILL = new Color(157, 193, 235, 45);

  private static final CraftingDialogLayout CLASSIC_LAYOUT = new CraftingDialogLayout();

  private final String targetTitle;
  private final String craftingTitle;
  private final CraftingDialogController controller;
  private final InventoryDragController<InventorySide> dragController =
      InventoryDragController.withAxisThreshold(DRAG_THRESHOLD_PX);
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

    Item[] visibleTargetSlots = dragController.visibleSlots(targetSlots, InventorySide.TARGET);
    Item[] visibleCraftingSlots =
        dragController.visibleSlots(craftingSlots, InventorySide.CRAFTING);

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

      if (dragController.isDragging()) {
        drawDropHighlights(g, leftGrid, leftPanelBounds, rightPanelBounds, craftingBounds);
      }

      handleInput(leftGrid, leftPanelBounds, rightPanelBounds, craftingBounds);

      if (dragController.isDragging()) {
        dragController.drawDragPreview(g);
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
      dragController.reset();
      return;
    }

    int mouseX = stage.mouseX();
    int mouseY = stage.mouseY();
    boolean leftButtonDown = InputManager.isButtonPressed(MouseButtons.LEFT);

    Optional<InventoryDragController.Release<InventorySide>> release =
        dragController.update(
            leftButtonDown,
            mouseX,
            mouseY,
            (slotMouseX, slotMouseY) -> {
              if (actionRenderer.findActionAt(slotMouseX, slotMouseY).isPresent()) {
                return null;
              }
              return findSlotSelection(slotMouseX, slotMouseY, leftGrid, craftingBounds);
            },
            this::itemOf);

    if (release.isEmpty()) {
      return;
    }

    InventoryDragController.Release<InventorySide> released = release.get();
    Optional<CraftingDialogAction> releasedButton = actionRenderer.findActionAt(mouseX, mouseY);
    if (releasedButton.isPresent() && released.completedDrag() == null) {
      actionRenderer.trigger(releasedButton.get());
      return;
    }

    if (released.completedDrag() != null) {
      transferDraggedItem(
          released.completedDrag(),
          released.releasedSlot(),
          leftPanelBounds,
          rightPanelBounds,
          mouseX,
          mouseY);
    } else if (released.pressedSlot() != null
        && released.pressedSlot().equals(released.releasedSlot())) {
      transferClickedItem(released.pressedSlot());
    }
  }

  private void transferClickedItem(GridHitTest.Slot<InventorySide> selection) {
    if (selection == null) {
      return;
    }

    controller.transferBySlot(selection.side().controllerSide(), selection.slotIndex());
  }

  private void transferDraggedItem(
      InventoryDragController.DragState<InventorySide> completedDrag,
      GridHitTest.Slot<InventorySide> releasedSlotSelection,
      Rectangle leftPanelBounds,
      Rectangle rightPanelBounds,
      int mouseX,
      int mouseY) {
    if (completedDrag == null) {
      return;
    }

    if (releasedSlotSelection != null
        && completedDrag.source().side() != releasedSlotSelection.side()) {
      controller.transferBySlotToSlot(
          completedDrag.source().side().controllerSide(),
          completedDrag.source().slotIndex(),
          releasedSlotSelection.side().controllerSide(),
          releasedSlotSelection.slotIndex());
      return;
    }

    if (completedDrag.source().side() == InventorySide.TARGET
        && rightPanelBounds.contains(mouseX, mouseY)) {
      controller.transferBySlot(
          CraftingDialogController.InventorySide.TARGET, completedDrag.source().slotIndex());
      return;
    }

    if (completedDrag.source().side() == InventorySide.CRAFTING
        && leftPanelBounds.contains(mouseX, mouseY)) {
      controller.transferBySlot(
          CraftingDialogController.InventorySide.CRAFTING, completedDrag.source().slotIndex());
    }
  }

  private GridHitTest.Slot<InventorySide> findSlotSelection(
      int mouseX,
      int mouseY,
      GridHitTest.Grid<InventorySide> leftGrid,
      List<CraftingDialogLayout.SlotBounds> craftingBounds) {
    return GridHitTest.findSlotAt(
        mouseX, mouseY, List.of(leftGrid), toBoundedSlots(craftingBounds));
  }

  private List<GridHitTest.BoundedSlot<InventorySide>> toBoundedSlots(
      List<CraftingDialogLayout.SlotBounds> craftingBounds) {
    List<GridHitTest.BoundedSlot<InventorySide>> slots = new ArrayList<>(craftingBounds.size());

    for (CraftingDialogLayout.SlotBounds bounds : craftingBounds) {
      slots.add(
          new GridHitTest.BoundedSlot<>(
              InventorySide.CRAFTING,
              bounds.slotIndex(),
              new Rectangle(bounds.x(), bounds.y(), bounds.size(), bounds.size())));
    }

    return List.copyOf(slots);
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
        (mouseX, mouseY) -> findSlotSelection(mouseX, mouseY, leftGrid, craftingBounds),
        this::itemOf)) {
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

  private void drawDropHighlights(
      Graphics2D g,
      GridHitTest.Grid<InventorySide> leftGrid,
      Rectangle leftPanelBounds,
      Rectangle rightPanelBounds,
      List<CraftingDialogLayout.SlotBounds> craftingBounds) {
    StageHandle stage = Game.stage().orElse(null);
    InventoryDragController.DragState<InventorySide> dragState = dragController.dragState();
    if (stage == null || dragState == null) {
      return;
    }

    int mouseX = stage.mouseX();
    int mouseY = stage.mouseY();

    if (dragState.source().side() == InventorySide.TARGET) {
      if (rightPanelBounds.contains(mouseX, mouseY)) {
        drawHighlight(g, rightPanelBounds);
      }
      return;
    }

    GridHitTest.Slot<InventorySide> hoveredTargetSlot =
        InventoryDropHandling.hoveredDropTarget(
            dragController,
            (slotMouseX, slotMouseY) ->
                GridHitTest.findGridSlotAt(slotMouseX, slotMouseY, List.of(leftGrid)),
            (source, target) -> target.side() != source.side());
    if (hoveredTargetSlot != null) {
      drawHighlight(g, leftGrid.slotBounds(hoveredTargetSlot.slotIndex()));
      return;
    }

    if (leftPanelBounds.contains(mouseX, mouseY)) {
      drawHighlight(g, leftPanelBounds);
      return;
    }

    for (CraftingDialogLayout.SlotBounds bounds : craftingBounds) {
      if (bounds.contains(mouseX, mouseY)) {
        drawHighlight(g, new Rectangle(bounds.x(), bounds.y(), bounds.size(), bounds.size()));
        return;
      }
    }
  }

  private void drawHighlight(Graphics2D g, Rectangle bounds) {
    InventoryDropHandling.drawDropHighlight(g, bounds, DRAG_HIGHLIGHT_FILL, DRAG_HIGHLIGHT);
  }

  private Item itemOf(GridHitTest.Slot<InventorySide> selection) {
    if (selection == null) {
      return null;
    }

    return inventoryOf(selection.side()).get(selection.slotIndex()).orElse(null);
  }

  private InventoryComponent inventoryOf(InventorySide side) {
    return side == InventorySide.TARGET
        ? controller.targetInventory()
        : controller.craftingInventory();
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

  private enum InventorySide {
    TARGET(CraftingDialogController.InventorySide.TARGET),
    CRAFTING(CraftingDialogController.InventorySide.CRAFTING);

    private final CraftingDialogController.InventorySide controllerSide;

    InventorySide(CraftingDialogController.InventorySide controllerSide) {
      this.controllerSide = controllerSide;
    }

    public CraftingDialogController.InventorySide controllerSide() {
      return controllerSide;
    }
  }
}
