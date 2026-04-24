package contrib.hud.crafting;

import contrib.item.Item;
import core.game.render.image.ImageFrameResolver;
import core.game.render.image.ImageAssets;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/** Renders the crafting panel background, ingredient previews, and result previews. */
final class CraftingPanelRenderer {

  private static final int RESULT_ICON_PADDING = 2;
  private static final int RESULT_LABEL_TOP_GAP = 6;
  private static final int RESULT_LABEL_PADDING_X = 8;
  private static final int RESULT_LABEL_PADDING_Y = 4;
  private static final int RESULT_LABEL_ARC = 6;

  private static final Color RESULT_LABEL_FILL = new Color(0xFFFF4D4D, true);
  private static final Color RESULT_LABEL_TEXT = Color.WHITE;

  private static final int ITEM_ICON_PADDING = 6;

  private static final String BACKGROUND_TEXTURE_PATH = "hud/crafting/background.png";

  private static final Color NUMBER_BADGE_FILL = new Color(0xFFFF4D4D, true);
  private static final Color NUMBER_BADGE_TEXT = Color.WHITE;

  private static final int NUMBER_PADDING = 5;

  private final CraftingDialogLayout layout;

  CraftingPanelRenderer(CraftingDialogLayout layout) {
    this.layout = layout;
  }

  List<CraftingDialogLayout.SlotBounds> craftingSlotBounds(
      Rectangle panelBounds, Item[] craftingSlots) {
    return mirrorSlotBounds(
        panelBounds,
        layout.visibleCraftingSlots(
            craftingSlots,
            panelBounds.x,
            panelBounds.y,
            panelBounds.width,
            panelBounds.height));
  }

  List<CraftingDialogLayout.ItemBounds> resultItemBounds(Rectangle panelBounds, Item[] resultItems) {
    return mirrorResultBounds(
        panelBounds,
        layout.resultSlots(
            resultItems, panelBounds.x, panelBounds.y, panelBounds.width, panelBounds.height));
  }

  void draw(
      Graphics2D g,
      Rectangle panelBounds,
      List<CraftingDialogLayout.SlotBounds> craftingBounds,
      Item[] visibleCraftingSlots,
      Item[] resultItems,
      List<CraftingDialogLayout.ItemBounds> resultBounds) {

    drawCraftingBackground(g, panelBounds);
    drawCraftingItems(g, craftingBounds, visibleCraftingSlots);
    drawResultItems(g, resultItems, resultBounds);
  }

  private List<CraftingDialogLayout.SlotBounds> mirrorSlotBounds(
      Rectangle panelBounds, List<CraftingDialogLayout.SlotBounds> slotBounds) {
    List<CraftingDialogLayout.SlotBounds> result = new ArrayList<>(slotBounds.size());

    for (CraftingDialogLayout.SlotBounds bounds : slotBounds) {
      int localBottomY = bounds.y() - panelBounds.y;
      int mirroredY = panelBounds.y + panelBounds.height - localBottomY - bounds.size();

      result.add(
          new CraftingDialogLayout.SlotBounds(
              bounds.slotIndex(), bounds.x(), mirroredY, bounds.size()));
    }

    return List.copyOf(result);
  }

  private List<CraftingDialogLayout.ItemBounds> mirrorResultBounds(
      Rectangle panelBounds, List<CraftingDialogLayout.ItemBounds> itemBounds) {
    List<CraftingDialogLayout.ItemBounds> result = new ArrayList<>(itemBounds.size());

    for (CraftingDialogLayout.ItemBounds bounds : itemBounds) {
      int localBottomY = bounds.y() - panelBounds.y;
      int mirroredY = panelBounds.y + panelBounds.height - localBottomY - bounds.size();

      result.add(new CraftingDialogLayout.ItemBounds(bounds.x(), mirroredY, bounds.size()));
    }

    return List.copyOf(result);
  }

  private void drawCraftingBackground(Graphics2D g, Rectangle panelBounds) {
    BufferedImage background = ImageAssets.get(BACKGROUND_TEXTURE_PATH);
    if (background != null) {
      g.drawImage(
          background, panelBounds.x, panelBounds.y, panelBounds.width, panelBounds.height, null);
      return;
    }

    // Fallback if the themed background asset is unavailable.
    g.setColor(new Color(196, 224, 241));
    g.fillRoundRect(panelBounds.x, panelBounds.y, panelBounds.width, panelBounds.height, 10, 10);
    g.setColor(Color.BLACK);
    g.drawRoundRect(panelBounds.x, panelBounds.y, panelBounds.width, panelBounds.height, 10, 10);
  }

  private void drawCraftingItems(
      Graphics2D g,
      List<CraftingDialogLayout.SlotBounds> craftingBounds,
      Item[] visibleCraftingSlots) {
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
  }

  private void drawResultItems(
      Graphics2D g,
      Item[] resultItems,
      List<CraftingDialogLayout.ItemBounds> resultBounds) {
    for (int i = 0; i < resultBounds.size() && i < resultItems.length; i++) {
      CraftingDialogLayout.ItemBounds bounds = resultBounds.get(i);
      Item item = resultItems[i];
      if (item == null) {
        continue;
      }

      Rectangle slotBounds = new Rectangle(bounds.x(), bounds.y(), bounds.size(), bounds.size());
      drawResultItemPresentation(g, slotBounds, item);
    }
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

    String label =
        item.displayName() == null || item.displayName().isBlank()
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
        label, labelX + RESULT_LABEL_PADDING_X, labelY + RESULT_LABEL_PADDING_Y + fm.getAscent());

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

  private BufferedImage resolveItemIcon(Item item) {
    if (item == null || item.inventoryAnimation() == null) {
      return null;
    }

    try {
      return ImageFrameResolver.toImage(item.inventoryAnimation().update());
    } catch (RuntimeException ignored) {
      return null;
    }
  }
}
