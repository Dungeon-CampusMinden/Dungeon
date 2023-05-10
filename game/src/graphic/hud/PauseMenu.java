package graphic.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Align;
import controller.ScreenController;
import starter.Game;
import tools.Constants;
import tools.Point;

public class PauseMenu extends Group{


    /** Creates a new PauseMenu */
    public PauseMenu() {
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
        addActor(screenText);
        hideMenu();
    }

    /** shows the Menu */
    public void showMenu() {
        getChildren().forEach((Actor s) -> s.setVisible(true));
    }

    /** hides the Menu */
    public void hideMenu() {
        getChildren().forEach((Actor s) -> s.setVisible(false));
    }
}
