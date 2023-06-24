package graphic.hud.menus;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import graphic.hud.FontBuilder;
import graphic.hud.ScreenButton;
import graphic.hud.TextButtonListener;
import graphic.hud.TextButtonStyleBuilder;
import tools.Point;

/** Class for building menu buttons. */
public class MenuButton extends ScreenButton implements IMenuItem {

    private static final TextButtonStyle MENU_BUTTON_STYLE;

    static {
        MENU_BUTTON_STYLE =
                new TextButtonStyleBuilder(FontBuilder.DEFAULT_FONT)
                        .setFontColor(Color.BLUE)
                        .setDownFontColor(Color.YELLOW)
                        .setUpImage("hud/buttonBackground.png")
                        .setDownImage("hud/buttonBackground.png")
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
    public MenuButton(
            String text, Point position, TextButtonListener listener, TextButtonStyle style) {
        super(text, position, listener, style);
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
    public MenuButton(String text, Point position, TextButtonListener listener) {
        super(text, position, listener, MENU_BUTTON_STYLE);
    }

    /**
     * Clears all current listeners on the button and adds a new one with a given functionality
     * which can be assigned by passing a new Listener object and implementing the clicked()-method.
     *
     * @param listener the Listener object to be assigned to the button
     */
    @Override
    public void executeAction(ClickListener listener) {
        this.clearListeners();
        this.addListener(listener);
    }
}
