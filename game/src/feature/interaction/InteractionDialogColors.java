package feature.interaction;

import com.badlogic.gdx.graphics.Color;

/**
 * Shared colors of the light interaction dialogs, so they look alike: dark ink on a light panel. A
 * room that wants its own look for a feature registers its own builder for the feature's dialog
 * type instead.
 */
public final class InteractionDialogColors {
  public static final Color PANEL = Color.valueOf("f2efe6");
  public static final Color INK = Color.valueOf("1b1f27");
  public static final Color MUTED = Color.valueOf("4a5262");
  public static final Color STEEL = Color.valueOf("5b6474");
  public static final Color STEEL_DARK = Color.valueOf("2b313b");
  public static final Color DIM = Color.valueOf("8a919e");
  public static final Color CARD = Color.valueOf("fbfbf8");
  public static final Color ACCENT = Color.valueOf("3b6ea8");
  public static final Color SELECTED = Color.valueOf("f0b43c");
  public static final Color SCREEN = Color.valueOf("d4e6c9");
  public static final Color GO = Color.valueOf("1f7a3a");
  public static final Color STOP = Color.valueOf("b42323");

  private InteractionDialogColors() {}
}
