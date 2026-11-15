package feature.interaction;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import feature.hud.UIUtils;

/**
 * Palette of the engine's default dialogs of the interaction features, so the card reader, the
 * phone, the sorting board and the cables look alike: dark ink on a light panel. A room that shows
 * a feature in its own look registers its own builder for the feature's dialog type instead.
 */
public final class FeatureDialogStyle {
  public static final Color PANEL = Color.valueOf("f2efe6");
  public static final Color INK = Color.valueOf("1b1f27");
  public static final Color MUTED = Color.valueOf("4a5262");
  public static final Color STEEL = Color.valueOf("5b6474");
  public static final Color SCREEN = Color.valueOf("d4e6c9");
  public static final Color GO = Color.valueOf("1f7a3a");
  public static final Color STOP = Color.valueOf("b42323");

  private FeatureDialogStyle() {}

  /**
   * Creates a plain area in one color, e.g. as background of a table.
   *
   * @param color fill color
   * @return a new drawable from the default skin
   */
  public static Drawable tinted(Color color) {
    return UIUtils.defaultSkin().newDrawable("white", color);
  }
}
