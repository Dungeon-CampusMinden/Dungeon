package contrib.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import contrib.components.UIComponent;
import core.Entity;
import core.Game;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.function.Supplier;
import org.apache.commons.text.WordUtils;

/** UI utility functions, such as a formatter for the window or dialog. */
public final class UIUtils {

  /** The default UI-Skin. */
  private static final IPath SKIN_FOR_DIALOG = new SimpleIPath("skin/uiskin.json");

  public static final Skin DEFAULT_SKIN =
      new Skin(Gdx.files.internal(SKIN_FOR_DIALOG.pathString()));

  /**
   * Limits the length of the string to 40 characters, after which a line break occurs
   * automatically.
   *
   * <p>BlackMagic number which can be tweaked for better line break. VirtualWindowWidth / FontSize
   * = MAX_ROW_LENGTH 480 / 12 = 40
   */
  private static final int MAX_ROW_LENGTH = 40;

  /**
   * Show the given dialog on the screen.
   *
   * @param provider Returns the dialog.
   * @param entity Entity that stores the {@link UIComponent} with the UI elements.
   */
  public static void show(final Supplier<Dialog> provider, final Entity entity) {
    entity.add(new UIComponent(provider.get(), true));
  }

  /**
   * Centers the actor based on the current window width and height.
   *
   * @param actor Actor whose position should be updated.
   */
  public static void center(final Actor actor) {
    actor.setPosition(
        (Game.windowWidth() - actor.getWidth()) / 2, (Game.windowHeight() - actor.getHeight()) / 2);
  }

  /**
   * Creates line breaks after a word once a certain character count is reached.
   *
   * @param string String which should be reformatted.
   */
  public static String formatString(final String string) {
    return WordUtils.wrap(string, MAX_ROW_LENGTH, " \n", true);
  }
}
