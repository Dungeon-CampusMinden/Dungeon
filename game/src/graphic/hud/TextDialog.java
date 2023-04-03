package graphic.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import tools.Constants;

/** Displays dialogue with scrollable label and a button */
public final class TextDialog extends Dialog {

    /** button ID (used when control is pressed) */
    private static final String btnID = "confirm exit";

    private static final String defaulMsg = "No message was load.";
    private static final String defaultBtnMsg = "OK";
    private boolean enable;

    /**
     * @param skin Skin for the dialogue (resources that can be used by UI widgets)
     */
    public TextDialog(Skin skin) {
        super("", skin);
        enable = false;

        Label labelContent = new Label(defaulMsg, skin);
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
        table.add(scroller).size(Constants.WINDOW_WIDTH - 200, Constants.WINDOW_HEIGHT - 200);
        this.addActor(table);

        button(defaultBtnMsg, btnID);
    }

    /**
     * @param title Title of the dialogue
     * @param buttonMsg text for the button
     * @param skin Skin for the dialogue (resources that can be used by UI widgets)
     * @param outputMsg Content displayed in the scrollable label
     */
    public TextDialog(Skin skin, String outputMsg, String buttonMsg, String title) {
        super(title, skin);
        enable = false;

        if (outputMsg.trim().isEmpty()) outputMsg = defaulMsg;

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
        table.add(scroller).size(Constants.WINDOW_WIDTH - 200, Constants.WINDOW_HEIGHT - 200);
        this.addActor(table);

        if (buttonMsg.trim().isEmpty()) buttonMsg = defaultBtnMsg;

        button(buttonMsg, btnID);
    }

    /** Indicates whether the dialogue must be deleted */
    public boolean getEnable() {
        return enable;
    }
    /** */
    public void setEnable(final boolean isEnable) {
        enable = isEnable;
    }

    /**
     * @param object Object associated with the button
     */
    @Override
    protected void result(final Object object) {
        if (object.toString() == btnID) enable = false;
    }
}
