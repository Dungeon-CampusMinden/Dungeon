package graphic.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import tools.Constants;

/** Contains Constructor, which immediately creates the dialogue including all its elements. */
public final class TextDialog extends Dialog {

    /** button ID (used when control is pressed) */
    private static final String btnID = "confirm exit";

    private static final String defaultMsg = "No message was load.";
    private static final String defaultBtnMsg = "OK";
    private static final int differenceMeasure = 200;

    /**
     * @param skin Skin for the dialogue (resources that can be used by UI widgets)
     * @param outputMsg Content displayed in the scrollable label
     * @param buttonMsg text for the button
     * @param title Title of the dialogue
     */
    public TextDialog(Skin skin, String outputMsg, String buttonMsg, String title) {
        super(title, skin);

        if (outputMsg.trim().isEmpty()) outputMsg = defaultMsg;

        Label labelContent = new Label(outputMsg, skin);
        labelContent.setAlignment(Align.left);
        labelContent.setColor(Color.WHITE);

        Table scrollTable = new Table();
        scrollTable.add(labelContent);
        scrollTable.row();

        ScrollPane scroller = new ScrollPane(scrollTable, skin);
        scroller.setFadeScrollBars(false);
        scroller.setScrollbarsVisible(true);

        Table table = new Table();
        table.setFillParent(true);
        table.add(scroller)
                .size(
                        Constants.WINDOW_WIDTH - differenceMeasure,
                        Constants.WINDOW_HEIGHT - differenceMeasure);
        this.addActor(table);

        if (buttonMsg.trim().isEmpty()) buttonMsg = defaultBtnMsg;

        button(buttonMsg, btnID);
    }

    /**
     * Provides information about the pressed Button
     *
     * @param object Object associated with the button
     */
    @Override
    protected void result(final Object object) {
        if (object.toString().equals(btnID)) {
            UITools.deleteDialogue(this);
        }
    }
}
