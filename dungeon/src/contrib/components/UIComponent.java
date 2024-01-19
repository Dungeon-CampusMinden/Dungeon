package contrib.components;

import com.badlogic.gdx.scenes.scene2d.Group;
import core.Component;
import core.utils.IVoidFunction;

/**
 * A simple implementation for a UI Component which allows to define a Group of {@link
 * com.badlogic.gdx.scenes.scene2d.ui Elements}
 *
 * <p>Also allows to define whether the Elements are pausing the Game or not.
 */
public final class UIComponent implements Component {
  private final Group dialog;
  private final boolean willPauseGame;
  private final boolean closeOnUICloseKey;
  private IVoidFunction onClose = () -> {};

  /**
   * Create a new UIComponent.
   *
   * @param dialog a Group of Elements which should be shown
   * @param willPauseGame if the UI should pause the Game or not
   * @param closeOnUICloseKey if the UI should close when the UI Close Key was pressed
   */
  public UIComponent(final Group dialog, boolean willPauseGame, boolean closeOnUICloseKey) {
    this.dialog = dialog;
    this.willPauseGame = willPauseGame;
    this.closeOnUICloseKey = closeOnUICloseKey;
  }

  /**
   * Create a new UIComponent.
   *
   * @param dialog a Group of Elements which should be shown
   * @param willPauseGame if the UI should pause the Game or not
   */
  public UIComponent(final Group dialog, boolean willPauseGame) {
    this(dialog, willPauseGame, true);
  }

  /**
   * Create a new UIComponent.
   *
   * <p>Creates an Empty Group which can be populated with Elements and pauses the Game when
   * visible.
   */
  public UIComponent() {
    this(new Group(), true);
  }

  /**
   * Check if the dialog is visible at the moment.
   *
   * @return true when the dialog is shown
   */
  public boolean isVisible() {
    return dialog.isVisible();
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
   * Get the dialog to show on the screen.
   *
   * @return the UI Elements which should be shown
   */
  public Group dialog() {
    return dialog;
  }

  /**
   * Check if the Dialog will close on a press on the close key.
   *
   * @return true when the UI should be closed with a press of the close key otherwise false
   */
  public boolean closeOnUICloseKey() {
    return closeOnUICloseKey;
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
}
