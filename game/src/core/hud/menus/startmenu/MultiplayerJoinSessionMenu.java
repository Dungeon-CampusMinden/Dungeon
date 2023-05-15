package core.hud.menus.startmenu;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Null;
import core.hud.*;
import core.hud.menus.Menu;
import core.utils.Constants;
import core.utils.Point;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class MultiplayerJoinSessionMenu<T extends Actor> extends Menu<T> {

    private static final float BUTTON_WIDTH = Constants.WINDOW_WIDTH / 2f;
    private static final float ACTOR_MARGIN = 30;
    private final ScreenInput inputHostIpPort;
    private final ScreenButton buttonJoin;
    private final ScreenText textInvalidAddress;

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

        textInvalidAddress = new ScreenText(
            "Invalid host address. Check ip and port.",
            new Point(0, 0),
            6f,
            new LabelStyleBuilder(FontBuilder.DEFAULT_FONT).setFontcolor(Color.RED).build()
        );
        textInvalidAddress.setPosition(
            inputHostIpPort.getX(),
            inputHostIpPort.getY() - textInvalidAddress.getHeight()
        );

        add((T) inputHostIpPort);
        add((T) buttonJoin);
        add((T) textInvalidAddress);

        hideMenu();
    }

    @Override
    public void showMenu() {
        this.forEach((Actor s) -> s.setVisible(true));
        textInvalidAddress.setVisible(false);
        isVisible = true;
    }

    public ScreenInput getInputHostIpPort() {
        return inputHostIpPort;
    }

    public ScreenButton getButtonJoin() {
        return buttonJoin;
    }

    public ScreenText getTextInvalidAddress() { return textInvalidAddress; }
}
