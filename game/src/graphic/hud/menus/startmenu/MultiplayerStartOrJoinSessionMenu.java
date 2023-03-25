package graphic.hud.menus.startmenu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Null;
import graphic.hud.menus.Menu;
import graphic.hud.widgets.FontBuilder;
import graphic.hud.widgets.ScreenButton;
import graphic.hud.widgets.TextButtonStyleBuilder;
import tools.Constants;
import tools.Point;

public class MultiplayerStartOrJoinSessionMenu<T extends Actor> extends Menu<T> {

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
            null,
            new TextButtonStyleBuilder(FontBuilder.DEFAULT_FONT)
                .setFontColor(Color.RED)
                .build()
        );
        buttonStartSession.setPosition(
            (Constants.WINDOW_WIDTH) / 2f,
            (Constants.WINDOW_HEIGHT) / 1.5f + buttonStartSession.getHeight(),
            Align.center | Align.bottom);
        buttonStartSession.getLabel().setFontScale(buttonTextLabelScale);

        buttonJoinSession = new ScreenButton(
            "Join a Session",
            new Point(0, 0),
            null,
            new TextButtonStyleBuilder(FontBuilder.DEFAULT_FONT)
                .setFontColor(Color.GREEN)
                .build()
        );
        buttonJoinSession.setPosition(
            (Constants.WINDOW_WIDTH) / 2f,
            (Constants.WINDOW_HEIGHT) / 3.5f + buttonJoinSession.getHeight(),
            Align.center | Align.bottom);
        buttonJoinSession.getLabel().setFontScale(buttonTextLabelScale);

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
