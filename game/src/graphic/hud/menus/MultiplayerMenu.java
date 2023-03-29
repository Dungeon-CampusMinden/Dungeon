package graphic.hud.menus;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Null;
import graphic.hud.widgets.FontBuilder;
import graphic.hud.widgets.ScreenButton;
import graphic.hud.widgets.TextButtonListener;
import graphic.hud.widgets.TextButtonStyleBuilder;
import tools.Constants;
import tools.Point;

public class MultiplayerMenu<T extends Actor> extends Menu<T> {

    private final ScreenButton buttonOpenToLan;
    private final ScreenButton buttonJoinSession;

    public MultiplayerMenu() {
        this(new SpriteBatch(), null);
    }

    /**
     * Creates a Screencontroller with a ScalingViewport which stretches the ScreenElements on
     * resize
     *
     * @param batch the batch which should be used to draw with
     */
    public MultiplayerMenu(SpriteBatch batch, @Null Stage stage) {
        super(batch, stage);
        buttonOpenToLan = new ScreenButton(
            "Open to Lan",
            new Point(0, 0),
            null,
            new TextButtonStyleBuilder(FontBuilder.DEFAULT_FONT)
                .setFontColor(Color.RED)
                .build()
        );
        buttonOpenToLan.setPosition(
            (Constants.WINDOW_WIDTH) / 2f,
            (Constants.WINDOW_HEIGHT) / 1.5f + buttonOpenToLan.getHeight(),
            Align.center | Align.bottom);

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

        add((T) buttonOpenToLan);
        add((T) buttonJoinSession);
        hideMenu();
    }

    public ScreenButton getButtonOpenToLan() {
        return this.buttonOpenToLan;
    }

    public ScreenButton getButtonJoinSession() {
        return this.buttonJoinSession;
    }
}
