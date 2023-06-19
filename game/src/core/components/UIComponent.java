package core.components;

import com.badlogic.gdx.scenes.scene2d.Group;

import core.Component;
import core.Entity;

/**
 * The UIComponent stores a {@link Group} (the dialog) of {@link com.badlogic.gdx.scenes.scene2d.ui
 * Element} and links it to the associated entity.
 *
 * <p>The UIComponent is used by the {@link core.systems.HudSystem} to display UI elements on the
 * screen.
 *
 * <p>It also allows for defining whether the elements pause the game or not.
 *
 * <p>Use {@link #dialog} to get the Dialog of this Component and add or remove UI-Elements to it.
 *
 * @see core.systems.HudSystem
 * @see Group
 * @see com.badlogic.gdx.scenes.scene2d.ui
 */
public final class UIComponent extends Component {
    private final Group dialog;
    private final boolean willPauseGame;

    /**
     * Create a new UIComponent and add it to the associated entity.
     *
     * @param entity The associated entity.
     * @param dialog A {@link Group} of elements which should be shown on the UI.
     * @param willPauseGame Specifies if the UI should pause the game or not.
     * @see Group
     */
    public UIComponent(final Entity entity, final Group dialog, final boolean willPauseGame) {
        super(entity);
        this.dialog = dialog;
        this.willPauseGame = willPauseGame;
    }

    /**
     * Create a new UIComponent and add it to the associated entity.
     *
     * <p>Creates an empty {@link Group} which can be populated with elements and pauses the game
     * when visible.
     *
     * @param entity The associated entity.
     * @see Group
     */
    public UIComponent(final Entity entity) {
        this(entity, new Group(), true);
    }

    /**
     * Check if the dialog for this component is currently shown.
     *
     * @return true if the dialog is shown, false if not.
     */
    public boolean isVisible() {
        return dialog.isVisible();
    }

    /**
     * Checks if the HUD will pause the game when the dialog of this component is shown.
     *
     * @return true if the HUD will pause the game when visible, false if not.
     */
    public boolean willPauseGame() {
        return willPauseGame;
    }

    /**
     * Retrieves the group of UI elements comprising the dialog for this component.
     *
     * @return The UI elements to be shown in the dialog.
     */
    public Group dialog() {
        return dialog;
    }
}
