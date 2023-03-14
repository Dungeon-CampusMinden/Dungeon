package graphic.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import tools.Point;

/** Simple one line Text input field. */
public class ScreenInput extends TextField {
    private static final TextFieldStyle DEFAULT_TEXT_FIELD_STYLE;

    static {
        DEFAULT_TEXT_FIELD_STYLE =
                new TextFieldStyleBuilder(FontBuilder.DEFAULT_FONT)
                        .setFontColor(Color.BLACK)
                        .build();
    }

    /**
     * Creates a one line text input field with default style.
     *
     * @param text a text which is added to the input by creation
     * @param position the position where the ScreenInput should be drawn
     */
    public ScreenInput(String text, Point position) {
        super(text, DEFAULT_TEXT_FIELD_STYLE);
        setPosition(position.x, position.y);
    }

    /**
     * creates a one line text input field with given style.
     *
     * @param text a text which is added to the input by creation
     * @param position the position where the ScreenInput should be drawn
     * @param style the custom style
     */
    public ScreenInput(String text, Point position, TextFieldStyle style) {
        super(text, style);
        setPosition(position.x, position.y);
    }
}
