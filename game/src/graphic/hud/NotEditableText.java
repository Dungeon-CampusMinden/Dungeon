package graphic.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;

public class NotEditableText extends Label {

    public NotEditableText(String outputMsg, Skin skin )
    {
        super(outputMsg, skin);
        this.setAlignment(Align.center);
        this.setColor(Color.WHITE);
    }
}
