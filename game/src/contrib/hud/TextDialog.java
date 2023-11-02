package contrib.hud;

import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import java.util.function.BiFunction;

/** A TextDialog which allows the resulthandler to be defined per functional interface */
public final class TextDialog extends Dialog {

    /** Handler for Button presses */
    private final BiFunction<TextDialog, String, Boolean> resultHandler;

    /**
     * creates a Textdialog with the given title and skin and stores the functional interface for
     * Button events.
     *
     * @param skin Skin for the dialog (resources that can be used by UI widgets)
     * @param title Title of the dialog
     * @param resultHandler controls the button presses
     */
    public TextDialog(
            String title, Skin skin, BiFunction<TextDialog, String, Boolean> resultHandler) {
        super(title, skin);
        this.resultHandler = resultHandler;
    }

    /**
     * creates a Textdialog with the given title and skin and stores the functional interface for
     * Button events.
     *
     * @param title Title of the dialog
     * @param skin Skin for the dialog (resources that can be used by UI widgets)
     * @param windowStyleName the name of the style which should be used
     * @param resultHandler controls the button presses
     */
    public TextDialog(
            String title,
            Skin skin,
            String windowStyleName,
            BiFunction<TextDialog, String, Boolean> resultHandler) {
        super(title, skin, windowStyleName);
        this.resultHandler = resultHandler;
    }
    /**
     * when a Button event happened calls the stored resultHandler and when the resultHandler
     * returns a false stops the default hide on button press.
     *
     * @param object Object associated with the button
     */
    @Override
    protected void result(final Object object) {
        if (!resultHandler.apply(this, object.toString())) cancel();
    }
}
