package contrib.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Disposable;
import contrib.components.InventoryComponent;
import contrib.components.UIComponent;
import contrib.hud.dialogs.DialogCreationException;
import contrib.hud.elements.GUICombination;
import contrib.hud.inventory.InventoryGUI;
import core.Entity;
import core.Game;
import core.components.PlayerComponent;
import core.game.PreRunConfiguration;
import core.network.server.DialogTracker;
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
   * Retrieve the default skin.
   *
   * <p>Load the skin on demand (singleton with lazy initialisation). This allows to write JUnit
   * tests for this class w/o mocking libGDX.
   *
   * @return the default skin.
   * @throws IllegalStateException if the skin cannot be loaded (e.g. in headless mode).
   */
  public static Skin defaultSkin() {
    if (DEFAULT_SKIN == null) {
      try {
        DEFAULT_SKIN = new Skin(Gdx.files.internal(SKIN_FOR_DIALOG.pathString()));
      } catch (UnsatisfiedLinkError e) {
        throw new IllegalStateException(
            "Could not load default skin. Are you running in a headless environment?", e);
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
   * Retrieves all {@link InventoryComponent}s from the given {@link UIComponent}'s dialog, if it is
   * a {@link GUICombination}.
   *
   * @param ui the UIComponent whose dialog to check for inventories
   * @return a stream of InventoryComponents found in the dialog's combinable GUIs
   */
  public static Stream<InventoryComponent> getInventoriesFromUI(UIComponent ui) {
    return (ui.dialog() instanceof GUICombination guiCombination)
        ? guiCombination.combinableGuis().stream()
            .filter(IInventoryHolder.class::isInstance)
            .map(IInventoryHolder.class::cast)
            .map(IInventoryHolder::inventoryComponent)
        : Stream.empty();
  }

  /**
   * Retrieves the first {@link InventoryComponent} from the given {@link UIComponent}'s dialog, if
   * it is a {@link GUICombination}.
   *
   * @param ui the UIComponent whose dialog to check for inventories
   * @return an Optional containing the first InventoryComponent found, or empty if none found
   */
  public static Optional<InventoryComponent> getFirstInventoryFromUI(UIComponent ui) {
    return getInventoriesFromUI(ui).findFirst();
  }

  /**
   * Retrieves the InventoryGUI associated with the given player's inventory, if it exists.
   *
   * @param player the player entity
   * @return an Optional containing the InventoryGUI if found, or empty if not found
   */
  public static Optional<InventoryGUI> getPlayerInventoryGUI(Entity player) {
    Optional<UIComponent> uiComponentOpt =
        player.fetch(UIComponent.class).filter(ui -> ui.dialog() instanceof GUICombination);
    if (uiComponentOpt.isEmpty()) {
      return Optional.empty();
    }

    Optional<InventoryComponent> playerInventory = player.fetch(InventoryComponent.class);
    return playerInventory.flatMap(
        inventoryComponent ->
            findAllTypesInGroup(uiComponentOpt.get().dialog(), InventoryGUI.class)
                .filter(inventoryGUI -> inventoryGUI.inventoryComponent() == inventoryComponent)
                .findFirst());
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
   * Recursively searches for all Actors of the specified type within the given Group and its
   * subgroups.
   *
   * @param dialog the Group to search within
   * @param type the Class type of the Actors to find
   * @param <T> the type of the Actors
   * @return a stream of all found Actors of the specified type
   */
  public static <T> Stream<T> findAllTypesInGroup(Group dialog, Class<T> type) {
    Stream.Builder<T> results = Stream.builder();
    for (Actor actor : dialog.getChildren()) {
      if (type.isInstance(actor)) {
        results.add(type.cast(actor));
      } else if (actor instanceof Group group) {
        findAllTypesInGroup(group, type).forEach(results::add);
      }
    }
    return results.build();
  }

  /**
   * Closes the dialog associated with the given UIComponent and optionally deletes its owner.
   *
   * <p>This method removes the UIComponent from its owner entity and removes the owner entity from
   * the game if the owner has no more Components after the UIComponent is removed. If the owner of
   * the UIComponent has a PlayerComponent, the number of open dialogs is decremented. All target
   * entities are notified of the dialog closure.
   *
   * @param uiComponent the UIComponent whose dialog is to be closed
   */
  public static void closeDialog(UIComponent uiComponent) {
    if (!uiComponent.canBeClosed()) {
      LOGGER.debug(
          "Tried to close a non-closable dialog on entity {}",
          uiComponent.dialogContext().ownerEntity().id());
      return;
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

      if (ownerEntity.componentStream().toList().isEmpty()) {
        Game.remove(ownerEntity);
      }

      if (PreRunConfiguration.isNetworkServer()) {
        DialogTracker.instance().closeDialog(uiComponent.dialogContext().dialogId(), true);
      }

      if (uiComponent.dialog() instanceof Disposable disposable) {
        disposable.dispose();
      }
    } catch (DialogCreationException e) {
      LOGGER.warn("Could not close dialog: {}", e.getMessage());
    }
  }
}
