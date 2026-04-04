package contrib.platform.gdx.hud;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import core.Game;
import core.ui.gdx.GdxUiAssetLoader;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;

/**
 * Shared libGDX assets for {@link contrib.hud.inventory.InventoryGUI}.
 *
 * <p>This keeps static libGDX resource ownership out of the backend-neutral inventory widget.
 */
public final class GdxInventoryGuiAssets {

  private static final IPath FONT_FNT = new SimpleIPath("skin/myFont.fnt");
  private static final IPath FONT_PNG = new SimpleIPath("skin/myFont.png");

  private static final int BACKGROUND_COLOR = 0x3e3e63e1;
  private static final int HOVER_BACKGROUND_COLOR = 0xffffffff;

  private static final BitmapFont BITMAP_FONT;
  private static final Texture BACKGROUND_TEXTURE;
  private static final TextureRegion BACKGROUND_REGION;
  private static final TextureRegion HOVER_BACKGROUND_REGION;

  static {
    if (Game.isHeadless()) {
      BITMAP_FONT = null;
      BACKGROUND_TEXTURE = null;
      BACKGROUND_REGION = null;
      HOVER_BACKGROUND_REGION = null;
    } else {
      BACKGROUND_TEXTURE =
        GdxUiAssetLoader.createHorizontalStripTexture(
          BACKGROUND_COLOR, HOVER_BACKGROUND_COLOR);
      BACKGROUND_REGION = new TextureRegion(BACKGROUND_TEXTURE, 0, 0, 1, 1);
      HOVER_BACKGROUND_REGION = new TextureRegion(BACKGROUND_TEXTURE, 1, 0, 1, 1);
      BITMAP_FONT = GdxUiAssetLoader.loadBitmapFont(FONT_FNT, FONT_PNG);
    }
  }

  private GdxInventoryGuiAssets() {}

  /**
   * Returns the shared bitmap font used by the libGDX inventory renderer.
   *
   * @return bitmap font, may be null in headless mode
   */
  public static BitmapFont bitmapFont() {
    return BITMAP_FONT;
  }

  /**
   * Returns the shared background region used by the libGDX inventory renderer.
   *
   * @return background region, may be null in headless mode
   */
  public static TextureRegion backgroundRegion() {
    return BACKGROUND_REGION;
  }

  /**
   * Returns the shared hover tooltip background region used by the libGDX inventory renderer.
   *
   * @return hover background region, may be null in headless mode
   */
  public static TextureRegion hoverBackgroundRegion() {
    return HOVER_BACKGROUND_REGION;
  }
}
