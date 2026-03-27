package contrib.hud;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import contrib.components.InventoryComponent;
import contrib.components.UIComponent;
import contrib.hud.dialogs.DialogCreationException;
import contrib.hud.elements.InventoryComponentProvider;
import core.Entity;
import core.Game;
import core.components.PlayerComponent;
import core.ui.gdx.GdxUiAssetLoader;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import core.utils.logging.DungeonLogger;
import java.util.Optional;
import java.util.stream.Stream;

/** UI utility functions, such as a formatter for the window or dialog. */
public final class UIUtils {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(UIUtils.class.getName());

  /** The default UI-Skin. */
  private static final IPath SKIN_FOR_DIALOG = new SimpleIPath("skin/uiskin.json");

  private static Skin DEFAULT_SKIN;

  /**
   * Returns the default UI skin, loading it from the asset loader if not already cached.
   *
   * <p>The skin is loaded lazily on first access and then cached for subsequent calls. If loading
   * fails, an IllegalStateException is thrown.
   *
   * @return the default UI skin
   * @throws IllegalStateException if the skin cannot be loaded
   */
  public static Skin defaultSkin() {
    if (DEFAULT_SKIN == null) {
      try {
        DEFAULT_SKIN = GdxUiAssetLoader.loadSkin(SKIN_FOR_DIALOG);
      } catch (RuntimeException e) {
        throw new IllegalStateException(
          "Could not load default skin. Are you running without the libGDX UI backend?", e);
      }
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

  /**
   * Retrieves all InventoryComponents from the UI dialog.
   *
   * <p>This method extracts InventoryComponents from the dialog associated with the given
   * UIComponent. It unwraps the dialog to find all InventoryComponentProviders and then streams
   * their inventory components.
   *
   * @param ui the UIComponent whose dialog contains the inventory components
   * @return a Stream of InventoryComponents found in the dialog
   */
  public static Stream<InventoryComponent> getInventoriesFromUI(UIComponent ui) {
    return ui.dialog()
      .unwrap(InventoryComponentProvider.class)
      .stream()
      .flatMap(InventoryComponentProvider::inventoryComponents);
  }

  /**
   * Recursively searches for an Actor of the specified type within the given Group and its
   * subgroups.
   *
   * @param dialog the Group to search within
   * @param type the Class type of the Actor to find
   * @param <T> the type of the Actor
   * @return an Optional containing the found Actor, or an empty Optional if not found
   */
  public static <T> Optional<T> findTypeInGroup(Group dialog, Class<T> type) {
    for (Actor actor : dialog.getChildren()) {
      if (type.isInstance(actor)) {
        return Optional.of(type.cast(actor));
      } else if (actor instanceof Group group) {
        Optional<T> result = findTypeInGroup(group, type);
        if (result.isPresent()) {
          return result;
        }
      }
    }
    return Optional.empty();
  }

  /**
   * Closes the dialog associated with the given UIComponent and optionally deletes its owner.
   *
   * <p>This method removes the UIComponent from its owner entity and optionally removes the owner
   * entity from the game. If the owner of the UIComponent has a PlayerComponent, the number of open
   * dialogs is decremented. All target entities are notified of the dialog closure.
   *
   * @param uiComponent the UIComponent whose dialog is to be closed
   * @param deleteOwner whether to remove the owner entity from the game after closing the dialog
   * @param callDefaultClose whether to call the onClose (default close behavior) callback of the
   *     UIComponent
   */
  public static void closeDialog(
      UIComponent uiComponent, boolean deleteOwner, boolean callDefaultClose) {
    if (callDefaultClose) {
      uiComponent.onClose().accept(uiComponent); // onClose callback
    }

    try {
      Entity ownerEntity = uiComponent.dialogContext().ownerEntity();
      ownerEntity.remove(UIComponent.class);
      // decrease open dialog count for all target entities
      for (Integer targetId : uiComponent.targetEntityIds()) {
        Optional<Entity> target = Game.findEntityById(targetId);
        target
            .flatMap(t -> t.fetch(PlayerComponent.class))
            .ifPresent(PlayerComponent::decrementOpenDialogs);
      }
      LOGGER.debug("Closed dialog on entity {}", ownerEntity.id());

      if (deleteOwner) {
        Game.remove(ownerEntity);
      }
    } catch (DialogCreationException e) {
      LOGGER.warn("Could not close dialog: {}", e.getMessage());
    }
  }

  /**
   * Closes the dialog associated with the given UIComponent and optionally deletes its owner.
   *
   * <p>This method removes the UIComponent from its owner entity and optionally removes the owner
   * entity from the game. If the owner of the UIComponent has a PlayerComponent, the number of open
   * dialogs is decremented. All target entities are notified of the dialog closure.
   *
   * @param uiComponent the UIComponent whose dialog is to be closed
   * @param deleteOwner whether to remove the owner entity from the game after closing the dialog
   */
  public static void closeDialog(UIComponent uiComponent, boolean deleteOwner) {
    closeDialog(uiComponent, deleteOwner, true);
  }

  /**
   * Closes the dialog associated with the given UIComponent.
   *
   * <p>If the owner of the UIComponent has a PlayerComponent, the number of open dialogs is
   * decremented.
   *
   * <p>By default, the owner entity is not deleted.
   *
   * @param uiComponent the UIComponent whose dialog is to be closed
   */
  public static void closeDialog(UIComponent uiComponent) {
    closeDialog(uiComponent, false);
  }
}
