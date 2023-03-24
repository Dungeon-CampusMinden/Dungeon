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

public class MultiplayerJoinSessionMenu<T extends Actor> extends Menu<T> {

    private final ScreenInput inputSessionInfo;
    private final ScreenButton buttonJoin;

    public MultiplayerJoinSessionMenu() {
        this(new SpriteBatch(), null);
    }

    /**
     * Creates a Screencontroller with a ScalingViewport which stretches the ScreenElements on
     * resize
     *
     * @param batch the batch which should be used to draw with
     */
    public MultiplayerJoinSessionMenu(SpriteBatch batch, @Null Stage stage) {
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

        buttonJoin = new ScreenButton(
            "Connect",
            new Point(0, 0),
            null,
            new TextButtonStyleBuilder(FontBuilder.DEFAULT_FONT)
                .setFontColor(Color.GREEN)
                .build()
        );
        buttonJoin.setPosition(
            (Constants.WINDOW_WIDTH) / 2f,
            (Constants.WINDOW_HEIGHT) / 3.5f + buttonJoin.getHeight(),
            Align.center | Align.bottom);

        add((T) inputSessionInfo);
        add((T) buttonJoin);
        hideMenu();
    }

    public ScreenInput getInputSessionInfo() {
        return inputSessionInfo;
    }

    public ScreenButton getButtonJoin() {
        return buttonJoin;
    }
}
