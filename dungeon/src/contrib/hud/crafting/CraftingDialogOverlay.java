package contrib.hud.crafting;

import contrib.components.InventoryComponent;
import contrib.crafting.CraftingType;
import contrib.hud.dialogs.DialogCallbackResolver;
import contrib.hud.elements.ImageButton;
import contrib.hud.elements.InventoryComponentProvider;
import contrib.hud.renderers.DialogFrameRenderer;
import contrib.hud.renderers.InventoryGridRenderer;
import contrib.hud.renderers.ItemTooltipRenderer;
import contrib.item.Item;
import core.Game;
import core.input.MouseButtons;
import core.render.AnimationFrameImages;
import core.render.ImageAssets;
import core.ui.overlay.UiOverlay;
import core.ui.StageHandle;
import core.utils.InputManager;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.SimpleIPath;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Represents a crafting dialog interface overlay rendered on top of the game scene.
 *
 * <p>This overlay contains all visual elements and interactive logic required for crafting,
 * such as crafting panels, inventory grids, action buttons, item icons, and drag/drop support.
 *
 * <p>It allows players to interact with their crafting inventory and recipes, displaying results
 * and processing input for crafting actions.
 *
 * <p>This class extends the functionality provided by {@code UiOverlay} and
 * {@code InventoryComponentProvider}, integrating advanced UI behaviors such as rendering, hit
 * detection, and inventory component management.
 *
 * <p>Key features:
 * <ul>
 *   <li>Custom rendering of crafting panels and inventory grids.
 *   <li>Drag-and-drop functionality for transferring items between inventory slots.
 *   <li>Interactive action buttons for crafting-related operations.
 *   <li>Support for legacy and modern crafting panel layouts.
 *   <li>Hover tooltips for items and slot highlights during interactions.
 * </ul>
 */
final class CraftingDialogOverlay
  implements UiOverlay, InventoryComponentProvider {

  private static final int DEFAULT_WIDTH = 1180;
  private static final int DEFAULT_HEIGHT = 600;

  private static final int PANEL_GAP = 26;
  private static final int PANEL_HEADER_GAP = 14;
  private static final int PANEL_PADDING = 12;
  private static final int CLASSIC_CRAFTING_PANEL_WIDTH = 420;
  private static final int CLASSIC_CRAFTING_PANEL_HEIGHT = 420;

  private static final int DRAG_THRESHOLD_PX = 8;
  private static final int DRAG_TARGET_INSET = 3;
  private static final int DRAG_TARGET_ARC = 8;

  private static final int RESULT_ICON_PADDING = 2;
  private static final int RESULT_LABEL_TOP_GAP = 6;
  private static final int RESULT_LABEL_PADDING_X = 8;
  private static final int RESULT_LABEL_PADDING_Y = 4;
  private static final int RESULT_LABEL_ARC = 6;

  private static final Color RESULT_LABEL_FILL = new Color(0xFFFF4D4D, true);
  private static final Color RESULT_LABEL_TEXT = Color.WHITE;

  private static final Color ACTION_BOX_FILL = new Color(210, 210, 210, 235);
  private static final Color ACTION_BOX_HOVER_FILL = new Color(232, 232, 232, 245);
  private static final Color ACTION_BOX_PRESSED_FILL = new Color(180, 180, 180, 245);
  private static final Color ACTION_BOX_BORDER = new Color(70, 70, 70, 220);
  private static final Color ACTION_BOX_HOVER_BORDER = new Color(20, 20, 20, 230);

  private static final int ACTION_BOX_ARC = 10;
  private static final int BUTTON_ICON_PADDING = 6;
  private static final int ITEM_ICON_PADDING = 6;

  private static final String LEGACY_BACKGROUND_TEXTURE_PATH = "hud/crafting/background.png";

  private static final Color INVENTORY_PANEL_FILL = new Color(62, 62, 99, 96);
  private static final Color INVENTORY_PANEL_OUTLINE = new Color(0x9dc1ebff, true);

  private static final Color NUMBER_BADGE_FILL = new Color(0xFFFF4D4D, true);
  private static final Color NUMBER_BADGE_TEXT = Color.WHITE;

  private static final Color DRAG_HIGHLIGHT = new Color(157, 193, 235, 180);
  private static final Color DRAG_HIGHLIGHT_FILL = new Color(157, 193, 235, 45);

  private static final int NUMBER_PADDING = 5;

  private static final CraftingDialogLayout CLASSIC_LAYOUT = new CraftingDialogLayout();

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

  CraftingDialogOverlay(
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
            .accept(
              action == CraftingDialogAction.CRAFT
                ? controller.craftingPayload()
                : null));

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
    Item[] resultItems = currentResultItems();

    Item[] visibleTargetSlots = targetSlots;
    Item[] visibleCraftingSlots = craftingSlots;

    if (dragState != null && dragState.source() != null) {
      int sourceSlot = dragState.source().slotIndex();

      if (dragState.source().side() == InventorySide.TARGET) {
        visibleTargetSlots = targetSlots.clone();
        if (sourceSlot >= 0 && sourceSlot < visibleTargetSlots.length) {
          visibleTargetSlots[sourceSlot] = null;
        }
      } else {
        visibleCraftingSlots = craftingSlots.clone();
        if (sourceSlot >= 0 && sourceSlot < visibleCraftingSlots.length) {
          visibleCraftingSlots[sourceSlot] = null;
        }
      }
    }

    int leftColumns = InventoryGridRenderer.columnsFor(targetSlots);
    int leftRows = InventoryGridRenderer.rowsFor(targetSlots, leftColumns);
    int leftGridWidth = InventoryGridRenderer.gridWidth(leftColumns);
    int leftGridHeight = InventoryGridRenderer.gridHeight(leftRows);

    int rightPanelWidth = CLASSIC_CRAFTING_PANEL_WIDTH;
    int rightPanelHeight = Math.max(CLASSIC_CRAFTING_PANEL_HEIGHT, leftGridHeight + 2 * PANEL_PADDING);

    int totalContentWidth = leftGridWidth + PANEL_GAP + rightPanelWidth;
    width =
      Math.max(
        DEFAULT_WIDTH,
        totalContentWidth + 2 * DialogFrameRenderer.PADDING);

    height =
      Math.max(
        DEFAULT_HEIGHT,
        120 + Math.max(leftGridHeight + 2 * PANEL_PADDING, rightPanelHeight)
          + DialogFrameRenderer.PADDING);

    if (x == 0 && y == 0) {
      x = (Game.windowWidth() - width) / 2;
      y = (Game.windowHeight() - height) / 2;
    }

    int contentY;
    int leftStartX;
    int gridTop;

    GridLayout leftGrid;
    Rectangle leftPanelBounds;
    Rectangle rightPanelBounds;
    List<CraftingDialogLayout.SlotBounds> craftingBounds;
    List<CraftingDialogLayout.ItemBounds> resultBounds;

    DialogFrameRenderer.RenderState state =
      DialogFrameRenderer.beginDialog(g);

    try {
      contentY =
        DialogFrameRenderer.drawFrameAndTitle(g, x, y, width, height, "Crafting");

      int titleBaseline = contentY + g.getFontMetrics().getAscent();

      leftStartX = x + (width - totalContentWidth) / 2;
      int rightPanelX = leftStartX + leftGridWidth + PANEL_GAP;

      g.setColor(Color.WHITE);
      g.drawString(targetTitle, leftStartX, titleBaseline);
      g.drawString(craftingTitle, rightPanelX + PANEL_PADDING, titleBaseline);

      gridTop =
        titleBaseline
          + PANEL_HEADER_GAP
          + InventoryGridRenderer.GRID_TOP_GAP;

      leftPanelBounds =
        new Rectangle(
          leftStartX - PANEL_PADDING,
          gridTop - PANEL_PADDING,
          leftGridWidth + 2 * PANEL_PADDING,
          leftGridHeight + 2 * PANEL_PADDING);

      rightPanelBounds =
        new Rectangle(
          rightPanelX,
          gridTop - PANEL_PADDING,
          rightPanelWidth,
          rightPanelHeight);

      drawInventoryPanelBackground(
        g,
        leftPanelBounds.x,
        leftPanelBounds.y,
        leftPanelBounds.width,
        leftPanelBounds.height);

      leftGrid =
        new GridLayout(InventorySide.TARGET, leftStartX, gridTop, leftColumns, visibleTargetSlots);
      InventoryGridRenderer.drawGrid(g, visibleTargetSlots, leftStartX, gridTop, leftColumns);

      craftingBounds =
        mirrorLegacySlotBounds(
          rightPanelBounds,
          CLASSIC_LAYOUT.visibleCraftingSlots(
            craftingSlots,
            rightPanelBounds.x,
            rightPanelBounds.y,
            rightPanelBounds.width,
            rightPanelBounds.height));

      resultBounds =
        mirrorLegacyResultBounds(
          rightPanelBounds,
          CLASSIC_LAYOUT.resultSlots(
            resultItems,
            rightPanelBounds.x,
            rightPanelBounds.y,
            rightPanelBounds.width,
            rightPanelBounds.height));

      syncActionButtonBounds(legacyActionButtonBounds(rightPanelBounds));
      drawLegacyCraftingPanel(g, rightPanelBounds, craftingBounds, visibleCraftingSlots, resultItems, resultBounds);

      if (dragState != null) {
        drawDropHighlights(g, leftGrid, leftPanelBounds, rightPanelBounds, craftingBounds);
      }

      handleInput(leftGrid, leftPanelBounds, rightPanelBounds, craftingBounds);

      if (dragState != null) {
        drawDragPreview(g);
      } else {
        drawHoverTooltip(g, leftGrid, craftingBounds, resultItems, resultBounds);
      }
    } finally {
      DialogFrameRenderer.finishDialog(g, state);
    }
  }

  private Map<CraftingDialogAction, Rectangle> legacyActionButtonBounds(Rectangle panelBounds) {
    Map<CraftingDialogAction, Rectangle> bounds = new EnumMap<>(CraftingDialogAction.class);

    for (CraftingDialogAction action : CraftingDialogAction.values()) {
      int buttonX = panelBounds.x + Math.round(panelBounds.width * action.relativeX());
      int legacyBottomY = Math.round(panelBounds.height * action.relativeY());
      int buttonWidth = Math.round(panelBounds.width * action.relativeWidth());
      int buttonHeight = Math.round(panelBounds.height * action.relativeHeight());

      int buttonY = panelBounds.y + panelBounds.height - legacyBottomY - buttonHeight;

      bounds.put(action, new Rectangle(buttonX, buttonY, buttonWidth, buttonHeight));
    }

    return bounds;
  }

  private void drawLegacyCraftingPanel(
    Graphics2D g,
    Rectangle panelBounds,
    List<CraftingDialogLayout.SlotBounds> craftingBounds,
    Item[] visibleCraftingSlots,
    Item[] resultItems,
    List<CraftingDialogLayout.ItemBounds> resultBounds) {

    drawLegacyCraftingBackground(g, panelBounds);

    for (int i = 0; i < craftingBounds.size(); i++) {
      CraftingDialogLayout.SlotBounds bounds = craftingBounds.get(i);
      Item item =
        bounds.slotIndex() >= 0 && bounds.slotIndex() < visibleCraftingSlots.length
          ? visibleCraftingSlots[bounds.slotIndex()]
          : null;
      if (item == null) {
        continue;
      }

      Rectangle slotBounds = new Rectangle(bounds.x(), bounds.y(), bounds.size(), bounds.size());
      drawCraftingItemIcon(g, slotBounds, item);
      drawIngredientNumberBadge(g, slotBounds, i + 1);
    }

    for (int i = 0; i < resultBounds.size() && i < resultItems.length; i++) {
      CraftingDialogLayout.ItemBounds bounds = resultBounds.get(i);
      Item item = resultItems[i];
      if (item == null) {
        continue;
      }

      Rectangle slotBounds = new Rectangle(bounds.x(), bounds.y(), bounds.size(), bounds.size());
      drawResultItemPresentation(g, slotBounds, item);
    }

    drawLegacyActionBoxes(g);
  }

  private void drawLegacyCraftingBackground(Graphics2D g, Rectangle panelBounds) {
    BufferedImage background = ImageAssets.get(LEGACY_BACKGROUND_TEXTURE_PATH);
    if (background != null) {
      g.drawImage(
        background,
        panelBounds.x,
        panelBounds.y,
        panelBounds.width,
        panelBounds.height,
        null);
      return;
    }

    // Fallback, falls das alte Asset einmal nicht geladen werden kann.
    g.setColor(new Color(196, 224, 241));
    g.fillRoundRect(panelBounds.x, panelBounds.y, panelBounds.width, panelBounds.height, 10, 10);
    g.setColor(Color.BLACK);
    g.drawRoundRect(panelBounds.x, panelBounds.y, panelBounds.width, panelBounds.height, 10, 10);
  }

  private void drawCraftingItemIcon(Graphics2D g, Rectangle bounds, Item item) {
    drawCraftingItemIcon(g, bounds, item, ITEM_ICON_PADDING);
  }

  private void drawCraftingItemIcon(Graphics2D g, Rectangle bounds, Item item, int padding) {
    BufferedImage icon = resolveItemIcon(item);
    if (icon == null) {
      return;
    }

    int maxWidth = bounds.width - 2 * padding;
    int maxHeight = bounds.height - 2 * padding;

    double scale =
      Math.min(
        maxWidth / (double) Math.max(1, icon.getWidth()),
        maxHeight / (double) Math.max(1, icon.getHeight()));

    int drawWidth = Math.max(1, (int) Math.round(icon.getWidth() * scale));
    int drawHeight = Math.max(1, (int) Math.round(icon.getHeight() * scale));
    int drawX = bounds.x + (bounds.width - drawWidth) / 2;
    int drawY = bounds.y + (bounds.height - drawHeight) / 2;

    g.drawImage(icon, drawX, drawY, drawWidth, drawHeight, null);
  }

  private void drawResultItemPresentation(Graphics2D g, Rectangle bounds, Item item) {
    drawCraftingItemIcon(g, bounds, item, RESULT_ICON_PADDING);

    String label = item.displayName() == null || item.displayName().isBlank()
      ? item.getClass().getSimpleName()
      : item.displayName();

    Font oldFont = g.getFont();
    g.setFont(oldFont.deriveFont(Font.BOLD, 12f));

    FontMetrics fm = g.getFontMetrics();
    int labelWidth = fm.stringWidth(label) + 2 * RESULT_LABEL_PADDING_X;
    int labelHeight = fm.getHeight() + 2 * RESULT_LABEL_PADDING_Y;

    int labelX = bounds.x + (bounds.width - labelWidth) / 2;
    int labelY = bounds.y + bounds.height + RESULT_LABEL_TOP_GAP;

    g.setColor(RESULT_LABEL_FILL);
    g.fillRoundRect(labelX, labelY, labelWidth, labelHeight, RESULT_LABEL_ARC, RESULT_LABEL_ARC);

    g.setColor(RESULT_LABEL_TEXT);
    g.drawString(
      label,
      labelX + RESULT_LABEL_PADDING_X,
      labelY + RESULT_LABEL_PADDING_Y + fm.getAscent());

    g.setFont(oldFont);
  }

  private void drawIngredientNumberBadge(Graphics2D g, Rectangle itemBounds, int number) {
    String label = Integer.toString(number);

    Font oldFont = g.getFont();
    g.setFont(oldFont.deriveFont(12f));

    FontMetrics fm = g.getFontMetrics();
    int badgeWidth = fm.stringWidth(label) + 2 * NUMBER_PADDING;
    int badgeHeight = fm.getHeight() + 2;

    int badgeX = itemBounds.x + (itemBounds.width - badgeWidth) / 2;
    int badgeY = itemBounds.y + itemBounds.height + 4;

    g.setColor(NUMBER_BADGE_FILL);
    g.fillRoundRect(badgeX, badgeY, badgeWidth, badgeHeight, 6, 6);

    g.setColor(NUMBER_BADGE_TEXT);
    g.drawString(label, badgeX + NUMBER_PADDING, badgeY + fm.getAscent() + 1);

    g.setFont(oldFont);
  }

  private void drawCenteredButtonIcon(Graphics2D g, BufferedImage icon, Rectangle bounds) {
    int maxWidth = bounds.width - 2 * BUTTON_ICON_PADDING;
    int maxHeight = bounds.height - 2 * BUTTON_ICON_PADDING;

    double scale =
      Math.min(
        maxWidth / (double) Math.max(1, icon.getWidth()),
        maxHeight / (double) Math.max(1, icon.getHeight()));

    int drawWidth = Math.max(1, (int) Math.round(icon.getWidth() * scale));
    int drawHeight = Math.max(1, (int) Math.round(icon.getHeight() * scale));
    int drawX = bounds.x + (bounds.width - drawWidth) / 2;
    int drawY = bounds.y + (bounds.height - drawHeight) / 2;

    g.drawImage(icon, drawX, drawY, drawWidth, drawHeight, null);
  }

  private void drawInventoryPanelBackground(Graphics2D g, int x, int y, int width, int height) {
    g.setColor(INVENTORY_PANEL_FILL);
    g.fillRect(x, y, width, height);

    g.setColor(INVENTORY_PANEL_OUTLINE);
    g.drawRect(x, y, width, height);
  }

  private Item[] currentResultItems() {
    return controller.currentRecipe()
      .map(
        recipe ->
          Arrays.stream(recipe.results())
            .filter(result -> result.resultType() == CraftingType.ITEM && result instanceof Item)
            .map(Item.class::cast)
            .toArray(Item[]::new))
      .orElseGet(() -> new Item[0]);
  }

  private void syncActionButtonBounds(Map<CraftingDialogAction, Rectangle> buttonBounds) {
    for (CraftingDialogAction action : CraftingDialogAction.values()) {
      ImageButton button = actionButtons.get(action);
      Rectangle bounds = buttonBounds.get(action);

      if (button == null || bounds == null) {
        continue;
      }

      button.x(bounds.x);
      button.y(bounds.y);
      button.width(bounds.width);
      button.height(bounds.height);
    }
  }

  private List<CraftingDialogLayout.SlotBounds> mirrorLegacySlotBounds(
    Rectangle panelBounds, List<CraftingDialogLayout.SlotBounds> legacyBounds) {
    List<CraftingDialogLayout.SlotBounds> result = new ArrayList<>(legacyBounds.size());

    for (CraftingDialogLayout.SlotBounds bounds : legacyBounds) {
      int localBottomY = bounds.y() - panelBounds.y;
      int mirroredY = panelBounds.y + panelBounds.height - localBottomY - bounds.size();

      result.add(
        new CraftingDialogLayout.SlotBounds(
          bounds.slotIndex(),
          bounds.x(),
          mirroredY,
          bounds.size()));
    }

    return List.copyOf(result);
  }

  private List<CraftingDialogLayout.ItemBounds> mirrorLegacyResultBounds(
    Rectangle panelBounds, List<CraftingDialogLayout.ItemBounds> legacyBounds) {
    List<CraftingDialogLayout.ItemBounds> result = new ArrayList<>(legacyBounds.size());

    for (CraftingDialogLayout.ItemBounds bounds : legacyBounds) {
      int localBottomY = bounds.y() - panelBounds.y;
      int mirroredY = panelBounds.y + panelBounds.height - localBottomY - bounds.size();

      result.add(new CraftingDialogLayout.ItemBounds(bounds.x(), mirroredY, bounds.size()));
    }

    return List.copyOf(result);
  }

  private void drawLegacyActionBoxes(Graphics2D g) {
    StageHandle stage = Game.stage().orElse(null);
    int mouseX = stage == null ? Integer.MIN_VALUE : stage.mouseX();
    int mouseY = stage == null ? Integer.MIN_VALUE : stage.mouseY();
    boolean leftPressed = InputManager.isButtonPressed(MouseButtons.LEFT);

    for (CraftingDialogAction action : CraftingDialogAction.values()) {
      ImageButton button = actionButtons.get(action);
      if (button == null) {
        continue;
      }

      Rectangle bounds = new Rectangle(button.x(), button.y(), button.width(), button.height());
      boolean hovered = bounds.contains(mouseX, mouseY);
      boolean pressed = hovered && leftPressed;

      drawActionBoxBackground(g, bounds, hovered, pressed);

      BufferedImage icon = AnimationFrameImages.toImage(button.animation().update());
      if (icon != null) {
        drawCenteredButtonIcon(g, icon, bounds);
      }
    }
  }

  private void drawActionBoxBackground(
    Graphics2D g, Rectangle bounds, boolean hovered, boolean pressed) {
    Color fill =
      pressed
        ? ACTION_BOX_PRESSED_FILL
        : hovered ? ACTION_BOX_HOVER_FILL : ACTION_BOX_FILL;

    Color border = hovered ? ACTION_BOX_HOVER_BORDER : ACTION_BOX_BORDER;

    g.setColor(fill);
    g.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, ACTION_BOX_ARC, ACTION_BOX_ARC);

    g.setColor(border);
    g.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, ACTION_BOX_ARC, ACTION_BOX_ARC);
  }

  private void handleInput(
    GridLayout leftGrid,
    Rectangle leftPanelBounds,
    Rectangle rightPanelBounds,
    List<CraftingDialogLayout.SlotBounds> craftingBounds) {
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
        pressedSlotSelection = findSlotSelection(mouseX, mouseY, leftGrid, craftingBounds);
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
        leftButtonDownLastFrame = false;
        return;
      }

      SlotSelection releasedSlotSelection = findSlotSelection(mouseX, mouseY, leftGrid, craftingBounds);
      SlotSelection previouslyPressedSlot = pressedSlotSelection;
      DragState completedDrag = dragState;

      pressedSlotSelection = null;
      dragState = null;

      if (completedDrag != null) {
        transferDraggedItem(
          completedDrag,
          releasedSlotSelection,
          leftPanelBounds,
          rightPanelBounds,
          mouseX,
          mouseY);
      } else if (previouslyPressedSlot != null && previouslyPressedSlot.equals(releasedSlotSelection)) {
        transferClickedItem(previouslyPressedSlot);
      }
    }

    leftButtonDownLastFrame = leftButtonDown;
  }

  private void maybeStartDrag(int mouseX, int mouseY) {
    if (pressedSlotSelection == null || dragState != null) {
      return;
    }

    if (Math.abs(mouseX - pressedMouseX) < DRAG_THRESHOLD_PX
      && Math.abs(mouseY - pressedMouseY) < DRAG_THRESHOLD_PX) {
      return;
    }

    Item item = itemOf(pressedSlotSelection);
    if (item == null) {
      return;
    }

    dragState = new DragState(pressedSlotSelection, item);
  }

  private void transferClickedItem(SlotSelection selection) {
    if (selection == null) {
      return;
    }

    interaction.transferClickedSlot(selection.side().controllerSide(), selection.slotIndex());
  }

  private void transferDraggedItem(
    DragState completedDrag,
    SlotSelection releasedSlotSelection,
    Rectangle leftPanelBounds,
    Rectangle rightPanelBounds,
    int mouseX,
    int mouseY) {
    if (completedDrag == null) {
      return;
    }

    if (releasedSlotSelection != null && completedDrag.source().side() != releasedSlotSelection.side()) {
      interaction.transferDroppedSlot(
        completedDrag.source().side().controllerSide(),
        completedDrag.source().slotIndex(),
        releasedSlotSelection.side().controllerSide(),
        releasedSlotSelection.slotIndex());
      return;
    }

    if (completedDrag.source().side() == InventorySide.TARGET
      && rightPanelBounds.contains(mouseX, mouseY)) {
      interaction.transferClickedSlot(
        CraftingDialogController.InventorySide.TARGET,
        completedDrag.source().slotIndex());
      return;
    }

    if (completedDrag.source().side() == InventorySide.CRAFTING
      && leftPanelBounds.contains(mouseX, mouseY)) {
      interaction.transferClickedSlot(
        CraftingDialogController.InventorySide.CRAFTING,
        completedDrag.source().slotIndex());
    }
  }

  private SlotSelection findSlotSelection(
    int mouseX,
    int mouseY,
    GridLayout leftGrid,
    List<CraftingDialogLayout.SlotBounds> craftingBounds) {
    int leftIndex =
      InventoryGridRenderer.findSlotIndexAt(
        mouseX, mouseY, leftGrid.slots(), leftGrid.startX(), leftGrid.startY(), leftGrid.columns());
    if (leftIndex >= 0) {
      return new SlotSelection(InventorySide.TARGET, leftIndex);
    }

    for (CraftingDialogLayout.SlotBounds bounds : craftingBounds) {
      if (bounds.contains(mouseX, mouseY)) {
        return new SlotSelection(InventorySide.CRAFTING, bounds.slotIndex());
      }
    }

    return null;
  }

  private void drawHoverTooltip(
    Graphics2D g,
    GridLayout leftGrid,
    List<CraftingDialogLayout.SlotBounds> craftingBounds,
    Item[] resultItems,
    List<CraftingDialogLayout.ItemBounds> resultBounds) {
    StageHandle stage = Game.stage().orElse(null);
    if (stage == null) {
      return;
    }

    int mouseX = stage.mouseX();
    int mouseY = stage.mouseY();

    if (mouseX < x || mouseX > x + width || mouseY < y || mouseY > y + height) {
      return;
    }

    int leftIndex =
      InventoryGridRenderer.findSlotIndexAt(
        mouseX, mouseY, leftGrid.slots(), leftGrid.startX(), leftGrid.startY(), leftGrid.columns());
    if (leftIndex >= 0) {
      controller.targetInventory().get(leftIndex).ifPresent(hoveredItem -> ItemTooltipRenderer.drawTooltip(
        g, hoveredItem, mouseX, mouseY, (int) stage.getWidth(), (int) stage.getHeight()));
      return;
    }

    for (CraftingDialogLayout.SlotBounds bounds : craftingBounds) {
      if (bounds.contains(mouseX, mouseY)) {
        controller.craftingInventory().get(bounds.slotIndex()).ifPresent(hoveredItem -> ItemTooltipRenderer.drawTooltip(
          g, hoveredItem, mouseX, mouseY, (int) stage.getWidth(), (int) stage.getHeight()));
        return;
      }
    }

    for (int i = 0; i < resultBounds.size() && i < resultItems.length; i++) {
      CraftingDialogLayout.ItemBounds bounds = resultBounds.get(i);
      Rectangle rect = new Rectangle(bounds.x(), bounds.y(), bounds.size(), bounds.size());
      if (rect.contains(mouseX, mouseY)) {
        ItemTooltipRenderer.drawTooltip(
          g, resultItems[i], mouseX, mouseY, (int) stage.getWidth(), (int) stage.getHeight());
        return;
      }
    }
  }

  private void drawDropHighlights(
    Graphics2D g,
    GridLayout leftGrid,
    Rectangle leftPanelBounds,
    Rectangle rightPanelBounds,
    List<CraftingDialogLayout.SlotBounds> craftingBounds) {
    StageHandle stage = Game.stage().orElse(null);
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

    int hoveredTargetSlotIndex =
      InventoryGridRenderer.findSlotIndexAt(
        mouseX, mouseY, leftGrid.slots(), leftGrid.startX(), leftGrid.startY(), leftGrid.columns());
    if (hoveredTargetSlotIndex >= 0) {
      Rectangle slotBounds =
        InventoryGridRenderer.slotBounds(
          hoveredTargetSlotIndex, leftGrid.startX(), leftGrid.startY(), leftGrid.columns());
      drawHighlight(g, slotBounds);
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
    g.setColor(DRAG_HIGHLIGHT_FILL);
    g.fillRoundRect(
      bounds.x + DRAG_TARGET_INSET,
      bounds.y + DRAG_TARGET_INSET,
      bounds.width - 2 * DRAG_TARGET_INSET,
      bounds.height - 2 * DRAG_TARGET_INSET,
      DRAG_TARGET_ARC,
      DRAG_TARGET_ARC);

    g.setColor(DRAG_HIGHLIGHT);
    g.drawRoundRect(
      bounds.x + DRAG_TARGET_INSET,
      bounds.y + DRAG_TARGET_INSET,
      bounds.width - 2 * DRAG_TARGET_INSET,
      bounds.height - 2 * DRAG_TARGET_INSET,
      DRAG_TARGET_ARC,
      DRAG_TARGET_ARC);
  }

  private void drawDragPreview(Graphics2D g) {
    StageHandle stage = Game.stage().orElse(null);
    if (stage == null || dragState == null || dragState.item() == null) {
      return;
    }

    int previewX = stage.mouseX() - InventoryGridRenderer.SLOT_WIDTH / 2;
    int previewY = stage.mouseY() - InventoryGridRenderer.SLOT_HEIGHT / 2;

    InventoryGridRenderer.drawItemPreview(g, previewX, previewY, dragState.item());
  }

  private Optional<CraftingDialogAction> findActionButtonAt(int mouseX, int mouseY) {
    for (Map.Entry<CraftingDialogAction, ImageButton> entry : actionButtons.entrySet()) {
      ImageButton button = entry.getValue();
      Rectangle bounds = new Rectangle(button.x(), button.y(), button.width(), button.height());
      if (bounds.contains(mouseX, mouseY)) {
        return Optional.of(entry.getKey());
      }
    }

    return Optional.empty();
  }

  private void triggerActionButton(CraftingDialogAction action) {
    if (action == null) {
      return;
    }

    DialogCallbackResolver.createButtonCallback(dialogId, action.callbackKey())
      .accept(action == CraftingDialogAction.CRAFT ? controller.craftingPayload() : null);
  }

  private Item itemOf(SlotSelection selection) {
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

  private BufferedImage resolveItemIcon(Item item) {
    if (item == null || item.inventoryAnimation() == null) {
      return null;
    }

    try {
      return AnimationFrameImages.toImage(item.inventoryAnimation().update());
    } catch (RuntimeException ignored) {
      return null;
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

  private record SlotSelection(InventorySide side, int slotIndex) {}

  private record DragState(SlotSelection source, Item item) {}

  private record GridLayout(
    InventorySide side, int startX, int startY, int columns, Item[] slots) {}
}
