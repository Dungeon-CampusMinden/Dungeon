package graphic.hud.menus.startmenu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Null;
import graphic.hud.menus.Menu;
import graphic.hud.widgets.*;
import tools.Constants;
import tools.Point;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class MultiplayerJoinSessionMenu<T extends Actor> extends Menu<T> {

    private final ScreenInput inputHostPort;
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
        inputFont.getData().setScale(2f, 2f);
        inputHostPort = new ScreenInput(
            String.format("%s:%d", deviceIpAddress, 25444),
            new Point(0, 0),
            new TextFieldStyleBuilder(inputFont)
                .setFontColor(Color.RED)
                .build()
        );
        GlyphLayout glyphLayout = new GlyphLayout();
        glyphLayout.setText(inputHostPort.getStyle().font, inputHostPort.getText());
        float prefWidth = glyphLayout.width;
        inputHostPort.setWidth(prefWidth);
        inputHostPort.setPosition(
            (Constants.WINDOW_WIDTH) / 2f,
            (Constants.WINDOW_HEIGHT) / 1.5f + inputHostPort.getHeight(),
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
        buttonJoin.getLabel().setFontScale(buttonTextLabelScale);

        add((T) inputHostPort);
        add((T) buttonJoin);
        hideMenu();
    }

    public ScreenInput getInputHostPort() {
        return inputHostPort;
    }

    public ScreenButton getButtonJoin() {
        return buttonJoin;
    }
}
