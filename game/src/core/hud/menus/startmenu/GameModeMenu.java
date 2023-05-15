package core.hud.menus.startmenu;


import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Null;
import core.hud.ScreenButton;
import core.hud.TextButtonListener;
import core.hud.menus.Menu;
import core.utils.Constants;
import core.utils.Point;

public class GameModeMenu<T extends Actor> extends Menu<T> {

    private static final float BUTTON_WIDTH = Constants.WINDOW_WIDTH / 2f;
    private static final float ACTOR_MARGIN = 30;
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
            "Singleplayer",
            new Point(0, 0),
            null
        );
        buttonSinglePlayer.getLabel().setFontScale(buttonTextLabelScale);
        buttonSinglePlayer.setSize(BUTTON_WIDTH,buttonSinglePlayer.getStyle().font.getLineHeight() * 2.5f);
        buttonSinglePlayer.setPosition(
            (Constants.WINDOW_WIDTH) / 2f,
            (Constants.WINDOW_HEIGHT + buttonSinglePlayer.getHeight()) / 2f,
            Align.center | Align.bottom);

        buttonMultiPlayer = new ScreenButton(
            "Multiplayer",
            new Point(0, 0),
            null
        );
        buttonMultiPlayer.getLabel().setFontScale(buttonTextLabelScale);
        buttonMultiPlayer.setSize(BUTTON_WIDTH, buttonMultiPlayer.getStyle().font.getLineHeight() * 2.5f);
        buttonMultiPlayer.setPosition(
            BUTTON_WIDTH,
            buttonSinglePlayer.getY() - buttonMultiPlayer.getHeight() - ACTOR_MARGIN,
            Align.center | Align.bottom
        );

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
