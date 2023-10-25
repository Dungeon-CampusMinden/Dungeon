package core.components;

import com.badlogic.gdx.scenes.scene2d.Group;

import core.Component;
import core.utils.IVoidFunction;

/**
 * A simple implementation for a UI Component which allows to define a Group of
 * com.badlogic.gdx.scenes.scene2d.ui Elements. Also allows to define whether the Elements are
 * pausing the Game or not.
 */
public class UIComponent implements Component {
    private final Group dialog;
    private final boolean willPauseGame;
    private final boolean closeOnUICloseKey;
    private IVoidFunction onClose = () -> {};

    /**
     * @param dialog a Group of Elements which should be shown
     * @param willPauseGame if the UI should pause the Game or not
     * @param closeOnUICloseKey if the UI should close when the UI Close Key was pressed
     */
    public UIComponent(Group dialog, boolean willPauseGame, boolean closeOnUICloseKey) {
        this.dialog = dialog;
        this.willPauseGame = willPauseGame;
        this.closeOnUICloseKey = closeOnUICloseKey;
    }
    /**
     * @param dialog a Group of Elements which should be shown
     * @param willPauseGame if the UI should pause the Game or not
     */
    public UIComponent(Group dialog, boolean willPauseGame) {
        this(dialog, willPauseGame, true);
    }

    /**
     * Creates an Empty Group which can be populated with Elements and pauses the Game when visible.
     */
    public UIComponent() {
        this(new Group(), true);
    }

    /**
     * @return true when the dialog is shown
     */
    public boolean isVisible() {
        return dialog.isVisible();
    }

    /**
     * @return true if this hud should pause the Game when visible
     */
    public boolean willPauseGame() {
        return willPauseGame;
    }

    /**
     * @return the UI Elements which should be shown
     */
    public Group dialog() {
        return dialog;
    }

    /**
     * @return true when the UI should be closed with a press of the close key otherwise false
     */
    public boolean closeOnUICloseKey() {
        return closeOnUICloseKey;
    }

    /**
     * @return the functions which should be called once the UI gets closed/removed
     */
    public IVoidFunction onClose() {
        return onClose;
    }

    /**
     * @param onClose the function which should be called once the UI gets closed/removed
     */
    public void onClose(IVoidFunction onClose) {
        this.onClose = onClose;
    }
}
