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

/** UI utility functions, such as a formatter for the window or dialog. */
public final class UIUtils {

  /** The default UI-Skin. */
  private static final IPath SKIN_FOR_DIALOG = new SimpleIPath("skin/uiskin.json");

  private static Skin DEFAULT_SKIN;

  /**
   * Retrieve the default skin.
   *
   * <p>Load the skin on demand (singleton with lazy initialisation). This allows to write JUnit
   * tests for this class w/o mocking libGDX.
   *
   * @return the default skin.
   */
  public static Skin defaultSkin() {
    if (DEFAULT_SKIN == null) {
      DEFAULT_SKIN = new Skin(Gdx.files.internal(SKIN_FOR_DIALOG.pathString()));
    }
    return DEFAULT_SKIN;
  }

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
    StringBuilder result = new StringBuilder();
    String text = string.replaceAll("\\s+", " ");
    String[] words = text.split(" ");
    for (int wordsIndex = 0, lineLength = 0; wordsIndex < words.length; wordsIndex++) {
      while (lineLength < MAX_ROW_LENGTH) {
        int toAdd = words[wordsIndex].length() + 1;
        if (lineLength + toAdd > MAX_ROW_LENGTH * 1.25) {
          // This line would be significantly longer than allowed.
          break;
        }
        result.append(words[wordsIndex]).append(" ");
        lineLength += toAdd;
        wordsIndex++;
        if (wordsIndex >= words.length) {
          break;
        }
      }
      while (lineLength < MAX_ROW_LENGTH * 1.25) {
        result.append(" ");
        lineLength++;
      }
      result.append(System.lineSeparator());
      lineLength = 0;
    }
    result.delete(result.length() - System.lineSeparator().length(), result.length());
    return result.toString();
  }
}
