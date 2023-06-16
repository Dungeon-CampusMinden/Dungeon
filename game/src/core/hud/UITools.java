package core.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import core.Entity;
import core.Game;
import core.components.UIComponent;
import core.utils.Constants;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * Formatting of the window or dialog and controls the creation of a dialogue object depending on an
 * event.
 */
public class UITools {
    public static final Skin DEFAULT_SKIN = new Skin(Gdx.files.internal(Constants.SKIN_FOR_DIALOG));
    public static final String DEFAULT_DIALOG_CONFIRM = "confirm";
    public static final String DEFAULT_DIALOG_TITLE = "Question";

    /**
     * Show the given Dialog on the screen.
     *
     * @param provider return the dialog
     * @param entity entity that stores the {@link UIComponent} with the UI-Elements
     */
    public static void show(Supplier<Dialog> provider, Entity entity) {
        new UIComponent(entity, provider.get(), true);
    }

    /**
     * created dialog for displaying the text-message
     *
     * @param content text which should be shown in the body of the dialog
     * @param buttonText text which should be shown in the button for closing the TextDialog
     * @param windowText text which should be shown as the name for the TextDialog
     */
    public static Entity generateNewTextDialog(
            String content, String buttonText, String windowText) {
        Entity entity = new Entity();
        show(
                new Supplier<Dialog>() {
                    @Override
                    public Dialog get() {
                        return DialogFactory.createTextDialog(
                                DEFAULT_SKIN,
                                content,
                                buttonText,
                                windowText,
                                createResultHandler(entity, buttonText));
                    }
                },
                entity);
        return entity;
    }

    /**
     * Create a {@link BiFunction} that removes the UI-Entity from the Game and close the Dialog, if
     * the close-button was pressed.
     *
     * @param entity UI-Entity
     * @param closeButtonID id of the close-button. The handler will use the id to execute the
     *     correct close-logic,
     * @return the configurated BiFunction that closes the window and removes the entity from the
     *     Game, if the close-button was pressed.
     */
    public static BiFunction<TextDialog, String, Boolean> createResultHandler(
            final Entity entity, final String closeButtonID) {
        return (d, id) -> {
            if (Objects.equals(id, closeButtonID)) {
                Game.removeEntity(entity);
                return true;
            }
            return false;
        };
    }
}
