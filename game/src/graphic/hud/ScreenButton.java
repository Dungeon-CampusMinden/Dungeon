package graphic.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import tools.Constants;
import tools.Point;

/** This class is intended for the configuration of the button to be displayed. */
public class ScreenButton extends TextButton {
    private static final TextButtonStyle DEFAULT_BUTTON_STYLE;

    static {
        DEFAULT_BUTTON_STYLE =
                new TextButtonStyleBuilder(FontBuilder.DEFAULT_FONT)
                        .setFontColor(Color.BLUE)
                        .setDownFontColor(Color.YELLOW)
                        .build();
    }

    /**
     * Creates a ScreenButton which can be used with the ScreenController.
     *
     * @param text the text for the ScreenButton
     * @param position the Position where the ScreenButton should be placed 0,0 is bottom left
     * @param listener the TextButtonListener which handles the button press
     * @param style the TextButtonStyle to use
     */
    public ScreenButton(
            String text, Point position, TextButtonListener listener, TextButtonStyle style) {
        super(text, style);
        this.setPosition(position.x, position.y);
        this.addListener(listener);
        this.setScale(1 / Constants.DEFAULT_ZOOM_FACTOR);
    }

    /**
     * Creates a ScreenButton which can be used with the ScreenController.
     *
     * <p>Uses the DEFAULT_BUTTON_STYLE
     *
     * @param text the text for the ScreenButton
     * @param position the Position where the ScreenButton should be placed 0,0 is bottom left
     * @param listener the TextButtonListener which handles the button press
     */
    public ScreenButton(String text, Point position, TextButtonListener listener) {
        this(text, position, listener, DEFAULT_BUTTON_STYLE);
    }
}
