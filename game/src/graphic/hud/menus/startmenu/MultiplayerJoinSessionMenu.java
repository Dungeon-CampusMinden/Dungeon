package graphic.hud.menus.startmenu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Null;
import graphic.hud.menus.Menu;
import graphic.hud.widgets.*;
import tools.Constants;
import tools.Point;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class MultiplayerJoinSessionMenu<T extends Actor> extends Menu<T> {

    private static final float BUTTON_WIDTH = Constants.WINDOW_WIDTH / 2f;
    private static final float ACTOR_MARGIN = 30;
    private final ScreenInput inputHostIpPort;
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
        String deviceIpAddress;
        try {
            deviceIpAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            deviceIpAddress = "Error";
        }
        BitmapFont inputFont = new BitmapFont();
        inputFont.getData().setScale(2f);
        inputHostIpPort = new ScreenInput(
            String.format("%s:%d", deviceIpAddress, 25444),
            new Point(0, 0),
            new TextFieldStyleBuilder(inputFont)
                .setFontColor(Color.GRAY)
                .setBackground(Color.WHITE)
                .build()
        );
        inputHostIpPort.setWidth(Constants.WINDOW_WIDTH / 2f);
        inputHostIpPort.setPosition(
            (Constants.WINDOW_WIDTH) / 2f,
            (Constants.WINDOW_HEIGHT + inputHostIpPort.getHeight()) / 2f,
            Align.center | Align.bottom);

        buttonJoin = new ScreenButton(
            "Connect",
            new Point(0, 0),
            null
        );
        buttonJoin.getLabel().setFontScale(buttonTextLabelScale);
        buttonJoin.setSize(BUTTON_WIDTH,buttonJoin.getStyle().font.getLineHeight() * 2.5f);
        buttonJoin.setPosition(
            BUTTON_WIDTH,
            inputHostIpPort.getY() - buttonJoin.getHeight() - ACTOR_MARGIN,
            Align.center | Align.bottom
        );

        add((T) inputHostIpPort);
        add((T) buttonJoin);
        hideMenu();
    }

    public ScreenInput getInputHostIpPort() {
        return inputHostIpPort;
    }

    public ScreenButton getButtonJoin() {
        return buttonJoin;
    }
}
