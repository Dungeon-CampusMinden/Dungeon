package core.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;

public class NotEditableText extends Label {
    /**
     * Display of a non-changeable text content in the dialogue (e.g. quiz question)
     *
     * @param outputMsg Content that is displayed
     * @param skin Skin for the dialogue (resources that can be used by UI widgets)
     */
    public NotEditableText(String outputMsg, Skin skin) {
        super(outputMsg, skin);
        this.setAlignment(Align.center);
        this.setColor(Color.WHITE);
    }
}
