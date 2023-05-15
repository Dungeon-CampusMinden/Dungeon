package core.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.*;

public class EditableText extends TextArea {
    /**
     * Area for entering texts
     *
     * @param skin Skin for the dialogue (resources that can be used by UI widgets)
     */
    public EditableText(Skin skin) {
        super("", skin);
        this.setMessageText("Click here...");
        this.setColor(Color.YELLOW);
    }
}
