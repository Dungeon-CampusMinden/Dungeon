package core.components;

import com.badlogic.gdx.scenes.scene2d.Group;

import core.Component;

/**
 * A simple implementation for a UI Component which allows to define a Group of
 * com.badlogic.gdx.scenes.scene2d.ui Elements. Also allows to define whether the Elements are
 * pausing the Game or not.
 */
public class UIComponent implements Component {
    private final Group dialog;
    private final boolean willPauseGame;

    /**
     * @param dialog a Group of Elements which should be shown
     * @param willPauseGame if the UI should pause the Game or not
     */
    public UIComponent(Group dialog, boolean willPauseGame) {
        this.dialog = dialog;
        this.willPauseGame = willPauseGame;
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
}
