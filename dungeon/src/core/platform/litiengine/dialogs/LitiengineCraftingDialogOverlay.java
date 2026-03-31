package core.platform.litiengine.dialogs;

import contrib.components.InventoryComponent;
import contrib.crafting.Crafting;
import contrib.crafting.CraftingResult;
import contrib.crafting.CraftingType;
import contrib.crafting.Recipe;
import contrib.item.Item;
import core.Game;
import core.platform.litiengine.ui.LitiengineUiOverlay;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * Minimal crafting overlay for the LITIENGINE backend.
 *
 * <p>This version is intentionally visual-only. It shows the target inventory, the crafting input
 * inventory, and a recipe preview, but does not yet implement drag-and-drop, craft, cancel, or
 * transfer interaction.
 */
final class LitiengineCraftingDialogOverlay implements LitiengineUiOverlay {

  private static final int DEFAULT_WIDTH = 1180;
  private static final int DEFAULT_HEIGHT = 560;

  private static final int PANEL_GAP = 26;
  private static final int PANEL_HEADER_GAP = 14;

  private static final int PREVIEW_TOP_GAP = 24;
  private static final int PREVIEW_HEIGHT = 120;
  private static final int PREVIEW_PADDING = 14;

  private final String targetTitle;
  private final InventoryComponent targetInventory;
  private final String craftingTitle;
  private final InventoryComponent craftingInventory;

  private int x;
  private int y;
  private int width = DEFAULT_WIDTH;
  private int height = DEFAULT_HEIGHT;
  private boolean visible = true;

  LitiengineCraftingDialogOverlay(
    String targetTitle,
    InventoryComponent targetInventory,
    String craftingTitle,
    InventoryComponent craftingInventory) {
    this.targetTitle = (targetTitle == null || targetTitle.isBlank()) ? "Inventory" : targetTitle;
    this.targetInventory = targetInventory;
    this.craftingTitle =
      (craftingTitle == null || craftingTitle.isBlank()) ? "Crafting" : craftingTitle;
    this.craftingInventory = craftingInventory;
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
      drawRecipePreview(g, previewY);
    } finally {
      LitiengineDialogOverlaySupport.finishDialog(g, state);
    }
  }

  private void drawRecipePreview(Graphics2D g, int previewY) {
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
  }

  private String buildRecipePreviewText() {
    Optional<Recipe> recipe = currentRecipe();

    if (recipe.isEmpty()) {
      if (craftingInventory.isEmpty()) {
        return "Add items to the crafting inventory to preview a matching recipe.\n"
          + "Crafting interaction is not implemented in this minimal LITIENGINE version yet.";
      }

      return "No matching recipe found for the current crafting inputs.\n"
        + "Crafting interaction is not implemented in this minimal LITIENGINE version yet.";
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
      + "\nCrafting interaction is not implemented in this minimal LITIENGINE version yet.";
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
}
