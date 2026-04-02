package contrib.platform.gdx.hud;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Align;
import contrib.item.Item;
import core.Game;
import core.platform.gdx.render.GdxAnimationFrames;
import core.ui.gdx.GdxUiAssetLoader;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;

/**
 * Small libGDX-only helper for rendering HUD items with labels.
 *
 * <p>This keeps concrete libGDX rendering details such as {@link Sprite}, {@link TextureRegion},
 * {@link BitmapFont}, and text badge drawing out of higher-level HUD widgets like CraftingGUI.
 */
public final class GdxHudItemRenderer {

  private static final IPath FONT_FNT = new SimpleIPath("skin/myFont.fnt");
  private static final IPath FONT_PNG = new SimpleIPath("skin/myFont.png");
  private static final int DEFAULT_LABEL_BACKGROUND_COLOR = 0xd93030ff;

  private static final Texture labelBackgroundTexture;
  private static final TextureRegion labelBackground;
  private static final BitmapFont bitmapFont;

  static {
    if (Game.isHeadless()) {
      labelBackgroundTexture = null;
      labelBackground = null;
      bitmapFont = null;
    } else {
      labelBackgroundTexture =
        GdxUiAssetLoader.createHorizontalStripTexture(DEFAULT_LABEL_BACKGROUND_COLOR);
      labelBackground = new TextureRegion(labelBackgroundTexture, 0, 0, 1, 1);
      bitmapFont = GdxUiAssetLoader.loadBitmapFont(FONT_FNT, FONT_PNG);
    }
  }

  private GdxHudItemRenderer() {}

  /**
   * Calculates the width of a row of equally sized items including the outer and inner gaps.
   *
   * @param itemCount number of items
   * @param itemSize side length of each square item
   * @param gap gap between items and around the row
   * @return total row width
   */
  public static int rowWidth(int itemCount, int itemSize, int gap) {
    if (itemCount <= 0) {
      return 0;
    }
    return itemSize * itemCount + gap * (itemCount + 1);
  }

  /**
   * Draws an item with a centered numeric badge below it.
   *
   * @param batch target batch
   * @param item item to draw
   * @param x item x position
   * @param y item y position
   * @param size item size
   * @param index label number
   * @param padding badge padding
   */
  public static void drawIndexedItem(
    Batch batch, Item item, int x, int y, int size, int index, int padding) {
    if (batch == null || item == null || Game.isHeadless()) {
      return;
    }

    drawItem(batch, item, x, y, size);

    String label = Integer.toString(index);
    GlyphLayout layout = new GlyphLayout(bitmapFont, label);

    int boxX = x + (size / 2) - Math.round(layout.height / 2f) - padding;
    int boxY = y - padding;

    batch.draw(
      labelBackground,
      boxX,
      boxY,
      layout.height + 2f * padding,
      layout.height + 2f * padding);

    bitmapFont.draw(
      batch,
      label,
      boxX + padding,
      boxY + padding + layout.height,
      layout.width,
      Align.center,
      false);
  }

  /**
   * Draws an item with a centered text badge below it.
   *
   * @param batch target batch
   * @param item item to draw
   * @param x item x position
   * @param y item y position
   * @param size item size
   * @param label badge text
   * @param padding badge padding
   */
  public static void drawNamedItem(
    Batch batch, Item item, int x, int y, int size, String label, int padding) {
    if (batch == null || item == null || Game.isHeadless()) {
      return;
    }

    drawItem(batch, item, x, y, size);

    if (label == null || label.isBlank()) {
      return;
    }

    GlyphLayout layout = new GlyphLayout(bitmapFont, label);

    int boxX = x + (size / 2) - Math.round(layout.width / 2f) - padding;
    int boxY = y - padding;

    batch.draw(
      labelBackground,
      boxX,
      boxY,
      layout.width + 2f * padding,
      layout.height + 2f * padding);

    bitmapFont.draw(
      batch,
      label,
      boxX + padding,
      boxY + padding + layout.height,
      layout.width,
      Align.center,
      false);
  }

  private static void drawItem(Batch batch, Item item, int x, int y, int size) {
    Sprite sprite = GdxAnimationFrames.toSprite(item.inventoryAnimation().update());
    batch.draw(sprite, x, y, size, size);
  }
}
