package contrib.platform.gdx.hud;

import com.badlogic.gdx.graphics.g2d.Batch;
import contrib.crafting.CraftingDialogController;
import contrib.crafting.CraftingDialogLayout;
import contrib.hud.crafting.CraftingDialogBodyRenderer;
import contrib.item.Item;
import core.Game;
import core.platform.gdx.render.GdxAnimationFrames;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.SimpleIPath;
import java.util.List;

/**
 * libGDX-specific renderer for the crafting dialog body.
 *
 * <p>This class owns the remaining background and item-preview rendering that used to live directly
 * in {@code CraftingGUI}. The geometry comes from the backend-neutral {@link CraftingDialogLayout}.
 */
public final class GdxCraftingDialogRenderer implements CraftingDialogBodyRenderer {

  private static final int NUMBER_PADDING = 5;
  private static final String BACKGROUND_TEXTURE_PATH = "hud/crafting/background.png";

  private static final Animation backgroundAnimation;

  static {
    if (Game.isHeadless()) {
      backgroundAnimation = null;
    } else {
      backgroundAnimation = new Animation(new SimpleIPath(BACKGROUND_TEXTURE_PATH));
    }
  }

  private final CraftingDialogLayout layout = new CraftingDialogLayout();

  /**
   * Draws the crafting dialog body.
   *
   * @param batch render batch
   * @param controller crafting dialog controller
   * @param x dialog x
   * @param y dialog y
   * @param width dialog width
   * @param height dialog height
   */
  @Override
  public void draw(
    Batch batch, CraftingDialogController controller, int x, int y, int width, int height) {
    if (batch == null || controller == null || Game.isHeadless()) {
      return;
    }

    if (backgroundAnimation != null) {
      batch.draw(GdxAnimationFrames.toRegion(backgroundAnimation.update()), x, y, width, height);
    }

    drawCraftingItems(batch, controller.craftingSlots(), x, y, width, height);
    drawResultItems(batch, controller.resultItems(), x, y, width, height);
  }

  private void drawCraftingItems(
    Batch batch, Item[] craftingSlots, int x, int y, int width, int height) {
    List<CraftingDialogLayout.SlotBounds> slots =
      layout.visibleCraftingSlots(craftingSlots, x, y, width, height);

    for (int i = 0; i < slots.size(); i++) {
      CraftingDialogLayout.SlotBounds slot = slots.get(i);
      Item item = craftingSlots[slot.slotIndex()];

      GdxHudItemRenderer.drawIndexedItem(
        batch, item, slot.x(), slot.y(), slot.size(), i + 1, NUMBER_PADDING);
    }
  }

  private void drawResultItems(Batch batch, Item[] resultItems, int x, int y, int width, int height) {
    List<CraftingDialogLayout.ItemBounds> slots = layout.resultSlots(resultItems, x, y, width, height);

    for (int i = 0; i < slots.size(); i++) {
      CraftingDialogLayout.ItemBounds slot = slots.get(i);
      GdxHudItemRenderer.drawNamedItem(
        batch,
        resultItems[i],
        slot.x(),
        slot.y(),
        slot.size(),
        resultItems[i].displayName(),
        NUMBER_PADDING);
    }
  }
}
