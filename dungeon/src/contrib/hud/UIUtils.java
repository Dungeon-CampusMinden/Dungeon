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
    return formatText(string, MAX_ROW_LENGTH, true);
  }

  public static String formatString(String string, boolean wrap) {
    return formatText(string, MAX_ROW_LENGTH, wrap);
  }

  public static String formatText(String text, final int maxLen, final boolean wordWrap) {
    if (text == null || text.isEmpty()) {
      throw new IllegalArgumentException("text is null or empty");
    }
    if (maxLen < 1) {
      throw new IllegalArgumentException("max length < 1");
    }
    final StringBuilder result = new StringBuilder();
    final char ls = '\n';
    text = text.replaceAll("\\s+", " ");
    String[] words = text.split(" ");

    int wordIndex = 0;
    int lineIndex = 0;
    while (wordIndex < words.length) {
      final String word = words[wordIndex++];
      final int before = result.length();
      if (lineIndex + word.length() <= maxLen) {
        // word will fit
        result.append(word);
      } else if (!wordWrap && word.length() <= maxLen) {
        // do not split word
        --wordIndex;
        int len = maxLen - lineIndex;
        result.append(" ".repeat(len));
      } else {
        // split word (even if wordWrap == true)
        int splitIndex = maxLen - lineIndex;
        String newWord1 = word.substring(0, splitIndex);
        String newWord2 = word.substring(splitIndex);
        result.append(newWord1);
        --wordIndex;
        words[wordIndex] = newWord2;
      }

      if (wordIndex >= words.length) {
        break;
      }

      lineIndex += result.length() - before;
      if (lineIndex < maxLen) {
        result.append(' ');
        ++lineIndex;
      }
      if (lineIndex >= maxLen) {
        result.append(ls);
        lineIndex = 0;
      }
    }

    return result.toString();
  }
}
