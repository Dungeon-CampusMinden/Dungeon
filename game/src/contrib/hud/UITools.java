package contrib.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import contrib.components.UIComponent;

import core.Entity;
import core.Game;
import core.utils.Constants;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * Formatting of the window or dialog and controls the creation of a dialogue object depending on an
 * event.
 */
public class UITools {

    /**
     * Limits the length of the string to 40 characters, after which a line break occurs
     * automatically.
     *
     * <p>BlackMagic number which can be tweaked for better line break VirtualWindowWidth / FontSize
     * = MAX_ROW_LENGTH 480 / 12 = 40
     */
    public static final int MAX_ROW_LENGTH = 40;

    public static final Skin DEFAULT_SKIN = new Skin(Gdx.files.internal(Constants.SKIN_FOR_DIALOG));
    public static final String DEFAULT_DIALOG_CONFIRM = "Bestätigen";
    public static final String DEFAULT_DIALOG_ABORT = "Abbrechen";
    public static final String DEFAULT_DIALOG_YES = "Ja";
    public static final String DEFAULT_DIALOG_NO = "Nein";

    /**
     * Show the given Dialog on the screen.
     *
     * @param provider return the dialog
     * @param entity entity that stores the {@link UIComponent} with the UI-Elements
     */
    public static void show(Supplier<Dialog> provider, Entity entity) {
        entity.add(new UIComponent(provider.get(), true));
    }

    /**
     * created dialog for displaying the text-message
     *
     * @param content text which should be shown in the body of the dialog
     * @param buttonText text which should be shown in the button for closing the TextDialog
     * @param windowText text which should be shown as the name for the TextDialog
     * @return Entity that contains the {@link UIComponent}. The entity will already be added to the
     *     game by this method.
     */
    public static Entity generateNewTextDialog(
            String content, String buttonText, String windowText) {
        Entity entity = new Entity();
        show(
                new Supplier<Dialog>() {
                    @Override
                    public Dialog get() {
                        Dialog textDialog =
                                DialogFactory.createTextDialog(
                                        DEFAULT_SKIN,
                                        content,
                                        buttonText,
                                        windowText,
                                        createResultHandler(entity, buttonText));
                        centerActor(textDialog);
                        return textDialog;
                    }
                },
                entity);
        Game.add(entity);
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
                Game.remove(entity);
                return true;
            }
            return false;
        };
    }

    /**
     * centers the actor based on the current windowWidth / windowHeight
     *
     * @param a Actor which position should be updated
     */
    public static void centerActor(Actor a) {
        a.setPosition(
                (Game.windowWidth() - a.getWidth()) / 2, (Game.windowHeight() - a.getHeight()) / 2);
    }

    /**
     * creates line breaks after a word once a certain char count is reached
     *
     * @param string which should be reformatted.
     */
    public static String formatStringForDialogWindow(String string) {
        StringBuilder formattedMsg = new StringBuilder();
        String[] lines = string.split(System.lineSeparator());

        for (String line : lines) {
            String[] words = line.split(" ");
            int sumLength = 0;

            for (String word : words) {
                sumLength += word.length();
                formattedMsg.append(word);
                formattedMsg.append(" ");

                if (sumLength > MAX_ROW_LENGTH) {
                    formattedMsg.append(System.lineSeparator());
                    sumLength = 0;
                }
            }
            formattedMsg.append(System.lineSeparator());
        }
        return formattedMsg.toString().trim();
    }
}
