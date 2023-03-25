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

public class MultiplayerHostOrJoinMenu<T extends Actor> extends Menu<T> {

    private static final int buttonTextLabelScale = 2;
    private final ScreenButton buttonOpenToLan;
    private final ScreenButton buttonJoinSession;

    public MultiplayerHostOrJoinMenu() {
        this(new SpriteBatch(), null);
    }

    /**
     * Creates a Screencontroller with a ScalingViewport which stretches the ScreenElements on
     * resize
     *
     * @param batch the batch which should be used to draw with
     */
    public MultiplayerHostOrJoinMenu(SpriteBatch batch, @Null Stage stage) {
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
        buttonOpenToLan.getLabel().setFontScale(buttonTextLabelScale);

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
