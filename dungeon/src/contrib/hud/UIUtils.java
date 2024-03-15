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
    return formatString(string, true);
  }

  public static String formatString(String string, boolean wrap) {
    return formatString(string, wrap, 1.25);
  }

  public static String formatString(String string, boolean wrap, double greyZoneFactor) {
    final StringBuilder result = new StringBuilder();
    final char ls = '\n';
    final int lsLen = 1;
    final int maxLen2 = (int) (MAX_ROW_LENGTH * greyZoneFactor);

    if (wrap) {
      String text = string.replaceAll("\\s+", " ");
      String[] words = text.split(" ");
      for (int wordsIndex = 0, lineLength = 0; wordsIndex < words.length; ) {
        if (words[wordsIndex].length() > maxLen2) {
          // This word would be significantly longer than allowed.
          while (words[wordsIndex].length() > maxLen2) {
            String newWord = words[wordsIndex].substring(0, maxLen2);
            String newWord2 = words[wordsIndex].substring(maxLen2);
            result.append(newWord).append(ls);
            words[wordsIndex] = newWord2;
          }
          continue;
        }
        while (lineLength < MAX_ROW_LENGTH) {
          int toAdd = words[wordsIndex].length() + lsLen;
          if (lineLength + toAdd > maxLen2) {
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
        while (lineLength < maxLen2) {
          result.append(" ");
          lineLength++;
        }
        result.append(ls);
        lineLength = 0;
      }
      result.delete(result.length() - lsLen, result.length());
      return result.toString();
    }

    for (int i = 0; i < string.length(); i++) {
      int j = 0;
      for (; j < maxLen2 && i + j < string.length(); j++) {
        result.append(string.charAt(i + j));
      }
      for (; j < maxLen2; j++) {
        result.append(' ');
      }
      result.append(ls);
      i += j - 1;
    }
    if (!result.isEmpty()) {
      result.delete(result.length() - lsLen, result.length());
    }
    return result.toString();
  }
}
