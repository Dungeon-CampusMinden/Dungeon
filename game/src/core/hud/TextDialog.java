package core.hud;

import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import java.util.function.BiPredicate;

/** Contains Constructor, which immediately creates the dialogue including all its elements. */
public final class TextDialog extends Dialog {

    /**
     * Handler for Button presses
     */
    private final BiPredicate<TextDialog, String> resultHandler;

    /**
     * Constructor for Quiz Question
     *
     * @param skin Skin for the dialog (resources that can be used by UI widgets)
     * @param title Title of the dialog
     * @param resultHandler controls the button presses
     */
    public TextDialog(String title, Skin skin, BiPredicate<TextDialog,String> resultHandler) {
        super(title, skin);
        this.resultHandler = resultHandler;
    }

    /**
     * Provides information about the pressed Button
     *
     * @param object Object associated with the button
     */
    @Override
    protected void result(final Object object) {
        if (!resultHandler.test(this, object.toString())) cancel();
    }
}
