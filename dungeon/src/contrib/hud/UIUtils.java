package contrib.hud;

import contrib.components.InventoryComponent;
import contrib.components.UIComponent;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.dialogs.DialogCreationException;
import core.Entity;
import core.Game;
import core.components.PlayerComponent;
import core.ui.UiHandle;
import core.utils.logging.DungeonLogger;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Utility class that provides helper methods for UI-related functionalities.
 *
 * <p>This class contains methods for formatting strings, manipulating dialogs, and retrieving
 * inventory components from UI elements.
 *
 * <p>The class is not instantiable.
 */
public final class UIUtils {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(UIUtils.class.getName());

  /**
   * Limits the length of the string to 40 characters, after which a line break occurs
   * automatically.
   *
   * <p>BlackMagic number that can be tweaked for a better line break. VirtualWindowWidth / FontSize
   * = MAX_ROW_LENGTH 480 / 12 = 40
   */
  private static final int MAX_ROW_LENGTH = 40;

  private static final char LS = '\n';

  private UIUtils() {}

  /**
   * Wrap texts to a maximum line length.
   *
   * @param text text to be soft-wrapped at {@link UIUtils#MAX_ROW_LENGTH} characters per line
   * @return the reformatted text
   */
  public static String formatString(final String text) {
    return formatString(text, MAX_ROW_LENGTH);
  }

  /**
   * Wrap texts to a maximum line length of {@code maxLen} characters.
   *
   * @param text text to be soft-wrapped
   * @param maxLen maximum number of characters per line
   * @return the reformatted text
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
   * Retrieves a stream of {@link InventoryComponent} instances associated with the provided UI component.
   *
   * <p>The returned {@code InventoryComponent}s are extracted from the dialog context of the given UI component
   * using predefined context keys.
   *
   * @param uiComponent the UI component from which to extract associated inventory components;
   *                    if {@code null} or if its dialog context is {@code null}, an empty stream is returned
   * @return a stream containing the retrieved {@link InventoryComponent} instances; returns an empty stream if no inventories can be found
   */
  public static Stream<InventoryComponent> getInventoriesFromUI(UIComponent uiComponent) {
    if (uiComponent == null || uiComponent.dialogContext() == null) {
      return Stream.empty();
    }

    return Stream.of(DialogContextKeys.ENTITY, DialogContextKeys.SECONDARY_ENTITY)
      .map(key -> inventoryFromContext(uiComponent, key))
      .flatMap(Optional::stream);
  }

  private static Optional<InventoryComponent> inventoryFromContext(UIComponent uiComponent, String key) {
    try {
      return Optional.of(uiComponent.dialogContext().requireEntity(key))
        .flatMap(entity -> entity.fetch(InventoryComponent.class));
    } catch (IllegalArgumentException | DialogCreationException exception) {
      return Optional.empty();
    }
  }

  /**
   * Closes the dialog associated with the given UI component and optionally deletes its owner.
   *
   * @param uiComponent the UI component whose dialog is to be closed
   * @param deleteOwner whether to remove the owner entity from the game after closing the dialog
   * @param callDefaultClose whether to call the default onClose callback
   */
  public static void closeDialog(
      UIComponent uiComponent, boolean deleteOwner, boolean callDefaultClose) {
    if (callDefaultClose) {
      uiComponent.onClose().accept(uiComponent);
    }

    uiComponent.dialog().ifPresent(UiHandle::remove);

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
   * Closes the dialog associated with the given UI component and optionally deletes its owner.
   *
   * @param uiComponent the UI component whose dialog is to be closed
   * @param deleteOwner whether to remove the owner entity from the game after closing the dialog
   */
  public static void closeDialog(UIComponent uiComponent, boolean deleteOwner) {
    closeDialog(uiComponent, deleteOwner, true);
  }

  /**
   * Closes the dialog associated with the given UI component.
   *
   * @param uiComponent the UI component whose dialog is to be closed
   */
  public static void closeDialog(UIComponent uiComponent) {
    closeDialog(uiComponent, false);
  }
}
