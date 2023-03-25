package graphic.hud.menus.startmenu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Null;
import graphic.hud.menus.Menu;
import graphic.hud.widgets.*;
import tools.Constants;
import tools.Point;

public class MultiplayerOpenToLanMenu<T extends Actor> extends Menu<T> {

    private static final int buttonTextLabelScale = 2;
    private final ScreenInput inputSessionInfo;
    private final ScreenButton buttonOpen;

    public MultiplayerOpenToLanMenu() { this(new SpriteBatch(), null); }

    /**
     * Creates a Screencontroller with a ScalingViewport which stretches the ScreenElements on
     * resize
     *
     * @param batch the batch which should be used to draw with
     */
    public MultiplayerOpenToLanMenu(SpriteBatch batch, @Null Stage stage) {
        super(batch, stage);
        inputSessionInfo = new ScreenInput(
            "Lets come together",
            new Point(0, 0),
            new TextFieldStyleBuilder(FontBuilder.DEFAULT_FONT)
                .setFontColor(Color.RED)
                .build()
        );
        inputSessionInfo.setPosition(
            (Constants.WINDOW_WIDTH) / 2f,
            (Constants.WINDOW_HEIGHT) / 1.5f + inputSessionInfo.getHeight(),
            Align.center | Align.bottom);

        buttonOpen = new ScreenButton(
            "Open",
            new Point(0, 0),
            null,
            new TextButtonStyleBuilder(FontBuilder.DEFAULT_FONT)
                .setFontColor(Color.GREEN)
                .build()
        );
        buttonOpen.setPosition(
            (Constants.WINDOW_WIDTH) / 2f,
            (Constants.WINDOW_HEIGHT) / 3.5f + buttonOpen.getHeight(),
            Align.center | Align.bottom);
        buttonOpen.getLabel().setFontScale(buttonTextLabelScale);

        add((T) inputSessionInfo);
        add((T) buttonOpen);
        hideMenu();
    }

    public ScreenInput getInputSessionInfo() {
        return inputSessionInfo;
    }

    public ScreenButton getButtonOpen() {
        return buttonOpen;
    }
}
