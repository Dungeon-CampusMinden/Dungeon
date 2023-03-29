package graphic.hud.menus;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Null;
import graphic.hud.widgets.FontBuilder;
import graphic.hud.widgets.LabelStyleBuilder;
import graphic.hud.widgets.ScreenText;
import tools.Constants;
import tools.Point;

public class PauseMenu<T extends Actor> extends Menu<T> {

    /** Creates a new PauseMenu with a new Spritebatch */
    public PauseMenu() {
        this(new SpriteBatch(), null);
    }

    /** Creates a new PauseMenu with a given Spritebatch */
    public PauseMenu(SpriteBatch batch, @Null Stage stage) {
        super(batch, stage);
        ScreenText screenText =
                new ScreenText(
                        "Paused",
                        new Point(0, 0),
                        3,
                        new LabelStyleBuilder(FontBuilder.DEFAULT_FONT)
                                .setFontcolor(Color.RED)
                                .build());
        screenText.setFontScale(3);
        screenText.setPosition(
                (Constants.WINDOW_WIDTH) / 2f - screenText.getWidth(),
                (Constants.WINDOW_HEIGHT) / 1.5f + screenText.getHeight(),
                Align.center | Align.bottom);
        add((T) screenText);
        hideMenu();
    }
}
