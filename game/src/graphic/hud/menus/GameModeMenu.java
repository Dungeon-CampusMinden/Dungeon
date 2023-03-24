package graphic.hud.menus;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Null;
import graphic.hud.widgets.*;
import tools.Constants;
import tools.Point;

public class GameModeMenu<T extends Actor> extends Menu<T> {

    private final ScreenButton buttonSinglePlayer;
    private final ScreenButton buttonMultiPlayer;

    public GameModeMenu() {
        this(new SpriteBatch(), null);
    }

    /**
     * Creates a Screencontroller with a ScalingViewport which stretches the ScreenElements on
     * resize
     *
     * @param batch the batch which should be used to draw with
     */
    public GameModeMenu(SpriteBatch batch, @Null Stage stage) {
        super(batch, stage);
        buttonSinglePlayer = new ScreenButton(
            "Single-Player",
            new Point(0, 0),
            null,
            new TextButtonStyleBuilder(FontBuilder.DEFAULT_FONT)
                .setFontColor(Color.RED)
                .build()
        );
        buttonSinglePlayer.setPosition(
            (Constants.WINDOW_WIDTH) / 2f,
            (Constants.WINDOW_HEIGHT) / 1.5f + buttonSinglePlayer.getHeight(),
            Align.center | Align.bottom);
        buttonMultiPlayer = new ScreenButton(
            "Multi-Player",
            new Point(0, 0),
            null,
            new TextButtonStyleBuilder(FontBuilder.DEFAULT_FONT)
                .setFontColor(Color.GREEN)
                .build()
        );
        buttonMultiPlayer.setPosition(
            (Constants.WINDOW_WIDTH) / 2f,
            (Constants.WINDOW_HEIGHT) / 3.5f + buttonMultiPlayer.getHeight(),
            Align.center | Align.bottom);

        add((T) buttonSinglePlayer);
        add((T) buttonMultiPlayer);

        hideMenu();
    }

    public ScreenButton getButtonSinglePlayer() {
        return buttonSinglePlayer;
    }

    public ScreenButton getButtonMultiPlayer() {
        return buttonMultiPlayer;
    }
}
