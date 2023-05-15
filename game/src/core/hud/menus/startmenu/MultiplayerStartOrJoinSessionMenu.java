package core.hud.menus.startmenu;


import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Null;
import core.hud.ScreenButton;
import core.hud.menus.Menu;
import core.utils.Constants;
import core.utils.Point;

public class MultiplayerStartOrJoinSessionMenu<T extends Actor> extends Menu<T> {

    private static final float BUTTON_WIDTH = Constants.WINDOW_WIDTH / 2f;
    private static final float BUTTON_MARGIN = 30;
    private final ScreenButton buttonStartSession;
    private final ScreenButton buttonJoinSession;

    public MultiplayerStartOrJoinSessionMenu() {
        this(new SpriteBatch(), null);
    }

    /**
     * Creates a Screencontroller with a ScalingViewport which stretches the ScreenElements on
     * resize
     *
     * @param batch the batch which should be used to draw with
     */
    public MultiplayerStartOrJoinSessionMenu(SpriteBatch batch, @Null Stage stage) {
        super(batch, stage);
        buttonStartSession = new ScreenButton(
            "Start a session",
            new Point(0, 0),
            null
        );
        buttonStartSession.getLabel().setFontScale(buttonTextLabelScale);
        buttonStartSession.setSize(BUTTON_WIDTH,buttonStartSession.getStyle().font.getLineHeight() * 2.5f);
        buttonStartSession.setPosition(
            (Constants.WINDOW_WIDTH) / 2f,
            (Constants.WINDOW_HEIGHT + buttonStartSession.getHeight()) / 2f,
            Align.center | Align.bottom
        );

        buttonJoinSession = new ScreenButton(
            "Join a Session",
            new Point(0, 0),
            null
        );
        buttonJoinSession.getLabel().setFontScale(buttonTextLabelScale);
        buttonJoinSession.setSize(BUTTON_WIDTH, buttonJoinSession.getStyle().font.getLineHeight() * 2.5f);
        buttonJoinSession.setPosition(
            BUTTON_WIDTH,
            buttonStartSession.getY() - buttonJoinSession.getHeight() - BUTTON_MARGIN,
            Align.center | Align.bottom
        );

        add((T) buttonStartSession);
        add((T) buttonJoinSession);
        hideMenu();
    }

    public ScreenButton getButtonStartSession() {
        return this.buttonStartSession;
    }

    public ScreenButton getButtonJoinSession() {
        return this.buttonJoinSession;
    }
}
