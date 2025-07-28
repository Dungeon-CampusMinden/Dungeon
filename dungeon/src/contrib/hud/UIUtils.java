package contrib.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import contrib.components.UIComponent;
import core.Entity;
import core.Game;
import core.components.PlayerComponent;
import core.utils.IVoidFunction;
import core.utils.MissingHeroException;
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
   * Line break character to use in the {@link UIUtils#formatString} method.
   *
   * <p>No need for {@code System.lineSeparator()} as libGDX wants {@code '\n'}
   */
  private static final char LS = '\n';

  /**
   * Show the given dialog on the screen.
   *
   * @param provider Returns the dialog.
   * @param entity Entity that stores the {@link UIComponent} with the UI elements.
   */
  public static void show(final Supplier<Dialog> provider, final Entity entity) {
    // displays this dialog, caches the dialog callback, and increments and decrements the dialog
    // counter so that the inventory is not opened while the dialog is displayed
    PlayerComponent heroPC = Game.hero().orElseThrow(MissingHeroException::new).fetch(PlayerComponent.class).orElseThrow();
    heroPC.incrementOpenDialogs();

    UIComponent uiComponent = new UIComponent(provider.get(), true);
    IVoidFunction oldOnClose = uiComponent.onClose();
    uiComponent.onClose(
        () -> {
          heroPC.decrementOpenDialogs();

          // execute the original on-close callback
          oldOnClose.execute();
        });
    entity.add(uiComponent);
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
   * Wrap texts to a maximum line length.
   *
   * <p>This function can be used to soft-wrap texts to a maximum of {@link UIUtils#MAX_ROW_LENGTH}
   * characters per line. The text is sanitised before wrapping by replacing multiple consecutive
   * whitespace characters (including line breaks) with single spaces. The text is then wrapped
   * according to the following algorithm: The last space before the maximum line length of {@link
   * UIUtils#MAX_ROW_LENGTH} characters is replaced by a line break. Any words exceeding the maximum
   * line length are cut off and wrapped at the position of the maximum line length.
   *
   * @param text text to be soft-wrapped at {@link UIUtils#MAX_ROW_LENGTH} characters per line
   * @return the reformatted text where all lines have been soft-wrapped to at maximum {@link
   *     UIUtils#MAX_ROW_LENGTH} characters per line
   */
  public static String formatString(final String text) {
    return formatString(text, MAX_ROW_LENGTH);
  }

  /**
   * Wrap texts to a maximum line length of {@code maxLen} characters.
   *
   * <p>This function can be used to soft-wrap texts to a maximum of {@code maxLen} characters per
   * line. The text is sanitised before wrapping by replacing multiple consecutive whitespace
   * characters (including line breaks) with single spaces. The text is then wrapped according to
   * the following algorithm: The last space before the maximum line length of {@code maxLen}
   * characters is replaced by a line break. Any words exceeding the maximum line length are cut off
   * and wrapped at the position of the maximum line length.
   *
   * @param text text to be soft-wrapped at {@code maxLen} characters per line
   * @param maxLen maximum number of characters per line
   * @return the reformatted text where all lines have been soft-wrapped to at maximum {@code
   *     maxLen} characters per line
   */
  public static String formatString(final String text, int maxLen) {
    if (text == null) {
      return null;
    }

    final StringBuilder sb = new StringBuilder();
    final String[] words = text.trim().replaceAll("\\s+", " ").split(" ");

    int wordIndex = 0;
    int lineIndex = 0;

    while (wordIndex < words.length) {
      final String word = words[wordIndex++];
      final int before = sb.length();

      if (lineIndex + word.length() <= maxLen) {
        // word will fit
        sb.append(word);
      } else if (word.length() <= maxLen) {
        // do not split word
        --wordIndex;
        int len = maxLen - lineIndex;
        lineIndex += len;
      } else {
        // split word
        int splitIndex = maxLen - lineIndex;
        String newWord1 = word.substring(0, splitIndex);
        String newWord2 = word.substring(splitIndex);
        sb.append(newWord1);
        --wordIndex;
        words[wordIndex] = newWord2;
      }

      if (wordIndex >= words.length) {
        break;
      }

      lineIndex += sb.length() - before;
      if (lineIndex < maxLen) {
        sb.append(' ');
        ++lineIndex;
      }

      if (lineIndex >= maxLen) {
        while (!sb.isEmpty() && sb.charAt(sb.length() - 1) == ' ') {
          sb.deleteCharAt(sb.length() - 1);
        }
        sb.append(LS);
        lineIndex = 0;
      }
    }

    return sb.toString();
  }
}
