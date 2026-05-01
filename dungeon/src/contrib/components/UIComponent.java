package contrib.components;

import com.badlogic.gdx.scenes.scene2d.Group;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogFactory;
import core.Component;
import core.network.messages.c2s.DialogResponseMessage;
import java.util.*;
import java.util.function.Consumer;

/**
 * A UI Component which stores dialog configuration and callbacks.
 *
 * <p>Contains the {@link DialogContext} for creating the visual dialog and a map of callbacks that
 * are executed when the user interacts with the dialog. Callbacks are stored server-side only and
 * are not serialized.
 *
 * <p>Also allows to define whether the Elements are pausing the Game or not.
 */
public final class UIComponent implements Component {

  private final boolean willPauseGame;
  private final boolean canBeClosed;
  private final int[] targetEntityIds;
  private final DialogContext dialogContext;

  /** Server-side callbacks map. Keys match callback keys sent by clients. */
  private final Map<String, Consumer<DialogResponseMessage.Payload>> callbacks = new HashMap<>();

  private Group dialog = null;

  /**
   * Create a new UIComponent.
   *
   * @param dialogContext the context that defines the dialog to be shown
   * @param willPauseGame if the UI should pause the Game or not
   * @param canBeClosed if the UI can be closed (e.g. with the close key)
   * @param targetEntityIds the target entity ids this UI should be shown for (e.g. for inventory
   *     UIs). Empty array for all entities.
   */
  public UIComponent(
      DialogContext dialogContext,
      boolean willPauseGame,
      boolean canBeClosed,
      int... targetEntityIds) {
    this.dialogContext = dialogContext;
    this.willPauseGame = willPauseGame;
    this.canBeClosed = canBeClosed;
    this.targetEntityIds = targetEntityIds;
  }

  /**
   * Create a new UIComponent.
   *
   * @param dialogContext the context that defines the dialog to be shown
   * @param willPauseGame if the UI should pause the Game or not
   * @param targetEntityIds the target entity ids this UI should be shown for (e.g. for inventory
   *     UIs). Empty array for all entities.
   */
  public UIComponent(DialogContext dialogContext, boolean willPauseGame, int... targetEntityIds) {
    this(dialogContext, willPauseGame, true, targetEntityIds);
  }

  /**
   * Create a new UIComponent.
   *
   * <p>By default no target entity ids are set, meaning the UI will be shown for all entities.
   *
   * @param dialogContext the context that defines the dialog to be shown
   * @param willPauseGame if the UI should pause the Game or not
   */
  public UIComponent(DialogContext dialogContext, boolean willPauseGame) {
    this(dialogContext, willPauseGame, new int[] {});
  }

  /**
   * Registers a callback for the given key.
   *
   * <p>Callbacks are stored server-side only. When a client sends a {@link
   * core.network.messages.c2s.DialogResponseMessage}, the server looks up the callback by key and
   * executes it with the provided data.
   *
   * @param key the callback key (e.g., "onConfirm", "craft", "cancel")
   * @param callback the callback to execute, receives optional custom payload
   * @return this UIComponent for method chaining
   */
  public UIComponent registerCallback(
      String key, Consumer<DialogResponseMessage.Payload> callback) {
    if (key == null || callback == null) {
      throw new IllegalArgumentException("key and callback must not be null");
    }
    callbacks.put(key, callback);
    return this;
  }

  /**
   * Gets all registered callbacks.
   *
   * @return the callbacks map (unmodifiable view)
   */
  public Map<String, Consumer<DialogResponseMessage.Payload>> callbacks() {
    return Map.copyOf(callbacks);
  }

  /**
   * Check if the dialog is visible at the moment.
   *
   * @return true when the dialog is shown
   */
  public boolean isVisible() {
    return dialog().isVisible();
  }

  /**
   * Check if the dialog will pause the game.
   *
   * @return true if this hud should pause the Game when visible
   */
  public boolean willPauseGame() {
    return willPauseGame;
  }

  /**
   * Check if the Dialog can be closed.
   *
   * <p>Dialogs which can be closed will be closed when the close key is pressed.
   *
   * @return true when the dialog can be closed
   */
  public boolean canBeClosed() {
    return canBeClosed;
  }

  /**
   * Get the target entity ids this UI should be shown for.
   *
   * @return the target entity ids
   */
  public int[] targetEntityIds() {
    return targetEntityIds;
  }

  /**
   * Get the dialog context, which defines the dialog to be shown.
   *
   * @return the dialog context
   */
  public DialogContext dialogContext() {
    return dialogContext;
  }

  /**
   * Get the dialog Group.
   *
   * <p>Creates the dialog if it does not exist yet.
   *
   * @return the dialog Group
   */
  public Group dialog() {
    if (dialog == null) {
      dialog = DialogFactory.create(dialogContext);
    }
    return dialog;
  }
}
