package contrib.components;

import com.badlogic.gdx.scenes.scene2d.Group;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogFactory;
import core.Component;
import core.utils.IVoidFunction;

/**
 * A simple implementation for a UI Component which allows to define a Group of {@link
 * com.badlogic.gdx.scenes.scene2d.ui Elements}
 *
 * <p>Also allows to define whether the Elements are pausing the Game or not.
 */
public final class UIComponent implements Component {
  private final boolean willPauseGame;
  private final boolean canBeClosed;
  private final int[] targetEntityIds;
  private final DialogContext dialogContext;
  private Group dialog = null;
  private IVoidFunction onClose = () -> {};

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
      int[] targetEntityIds) {
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
  public UIComponent(DialogContext dialogContext, boolean willPauseGame, int[] targetEntityIds) {
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
   * Function to execute on close.
   *
   * @return the functions which should be called once the UI gets closed/removed
   */
  public IVoidFunction onClose() {
    return onClose;
  }

  /**
   * Set the function to execute on close.
   *
   * @param onClose the function which should be called once the UI gets closed/removed
   */
  public void onClose(IVoidFunction onClose) {
    this.onClose = onClose;
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
