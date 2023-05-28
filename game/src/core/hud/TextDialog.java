package core.hud;

import com.badlogic.gdx.scenes.scene2d.ui.*;

/** Contains Constructor, which immediately creates the dialogue including all its elements. */
public final class TextDialog extends Dialog {

    /** button ID (used when control is pressed) */
    private static final String BUTTON_ID = "confirm exit";
    /** Default message when no text is transferred */
    private static final String DEFAULT_MSG = "No message was load.";
    /** Default Button message */
    private static final String DEFAULT_BUTTON_MSG = "OK";

    /**
     * Constructor for Quiz Question
     *
     * @param skin Skin for the dialog (resources that can be used by UI widgets)
     * @param title Title of the dialog
     */
    TextDialog(String title, Skin skin) {
        super(title, skin);
    }

    /**
     * Provides information about the pressed Button
     *
     * @param object Object associated with the button
     */
    @Override
    protected void result(final Object object) {
        if (object.toString().equals(BUTTON_ID)) {
            UITools.deleteDialogue(this);
        }
    }
}
