package graphic.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import controller.ScreenController;
import tools.Constants;

/** Predefined key event calls up dialogue with corresponding formatting and functionality */
public class EventResponsiveDialogue<T extends Actor> extends ScreenController<T> {

    /** Possible key events for dialogue call */
    public enum AuthorisedAccessKeys {
        INTERACT_WORLD(Input.Keys.E),
        PAUSE_GAME(Input.Keys.P),
        TEST_QUEST(Input.Keys.Z);
        private final int value;

        AuthorisedAccessKeys(int value) {
            this.value = value;
        }
    }
    /** Dialogue with info field and button to cancel play pause */
    private static TextDialog dialog;

    /** Determined event which dialogue is to be called up */
    private static int eventDescription;

    /** Description of all components of the dialogue and message for the dialogue */
    String[] formatIdentifier;

    /**
     * Creates a new EventResponsiveDialogue with a new Spritebatch
     *
     * @param msg Content displayed in the dialogue
     * @param skin Resources that can be used by UI widgets
     * @param msgColor colour of the text
     */
    public EventResponsiveDialogue(String msg, Skin skin, Color msgColor) {
        this(new SpriteBatch(), msg, skin, msgColor);
    }

    /**
     * Creates a new EventResponsiveDialogue with a given Spritebatch
     *
     * @param batch to display the textures
     * @param msg Content displayed in the dialogue
     * @param skin Resources that can be used by UI widgets
     * @param msgColor colour of the text
     */
    public EventResponsiveDialogue(SpriteBatch batch, String msg, Skin skin, Color msgColor) {
        super(batch);

        createDialogOnEvent(eventDescription, skin, msg);
        formatDialog(msgColor);

        dialog.setEnable(true);
        add((T) dialog);
    }
    /**
     * Function formats dialogue according to the selection of the key event. P' Call dialogue with
     * pause message. E' Call dialogue with text content.
     *
     * @param eventDescription Determined event which dialogue is to be called up
     * @param msg Content displayed in the dialogue
     * @param skin Resources that can be used by UI widgets
     */
    private void createDialogOnEvent(int eventDescription, Skin skin, String msg) {
        switch (eventDescription) {
            case Input.Keys.P -> {
                formatIdentifier = new String[3];
                formatIdentifier[0] = "pause";
                formatIdentifier[1] = "OK";
                formatIdentifier[2] = "pause";
                UITools.showInfoText(formatIdentifier);
                dialog =
                        new TextDialog(
                                skin,
                                formatIdentifier[0],
                                formatIdentifier[1],
                                formatIdentifier[2]);
            }
            case Input.Keys.E -> {
                formatIdentifier = new String[3];
                formatIdentifier[0] = msg;
                formatIdentifier[1] = "Weiter";
                formatIdentifier[2] = "NPC Dialog";
                UITools.showInfoText(formatIdentifier);
                dialog =
                        new TextDialog(
                                skin,
                                formatIdentifier[0],
                                formatIdentifier[1],
                                formatIdentifier[2]);
            }
            default -> {
                UITools.showInfoText();
                dialog = new TextDialog(skin);
            }
        }
    }
    /** Dialog formatting */
    private void formatDialog(Color msgColor) {
        dialog.setColor(msgColor);
        dialog.setWidth(Constants.WINDOW_WIDTH - 100);
        dialog.setHeight(Constants.WINDOW_HEIGHT - 100);
        dialog.setPosition(
                (Constants.WINDOW_WIDTH) / 2f,
                (Constants.WINDOW_HEIGHT) / 2f,
                Align.center | Align.top / 2);
    }

    /** Flag that the dialogue has been deleted */
    public boolean isEnable() {
        return dialog.getEnable();
    }
    /** Find out whether the key event for dialogue generation has occurred */
    public static boolean isDialogCalledByKeyboardEvent() {
        for (AuthorisedAccessKeys keyOption : AuthorisedAccessKeys.values()) {
            if (Gdx.input.isKeyJustPressed(keyOption.value)) {
                eventDescription = keyOption.value;
                return true;
            }
        }
        return false;
    }
}
