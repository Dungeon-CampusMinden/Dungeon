package hud;

import basiselements.Removable;
import basiselements.hud.ScreenText;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import controller.ScreenController;
import tools.Constants;
import tools.Point;

public class PauseMenu<T extends Actor & Removable> extends ScreenController<T> {

    /** Creates a new PauseMenu with a new Spritebatch */
    public PauseMenu() {
        this(new SpriteBatch());
    }

    /** Creates a new PauseMenu with a given Spritebatch */
    public PauseMenu(SpriteBatch batch) {
        super(batch);
        add(
                (T)
                        new ScreenText(
                                "Paused",
                                new Point(Constants.WINDOW_WIDTH / 2, Constants.WINDOW_HEIGHT / 2),
                                1));
        hideMenu();
    }

    /** shows the Menu */
    public void showMenu() {
        this.forEach((Actor s) -> s.setVisible(true));
    }

    /** hides the Menu */
    public void hideMenu() {
        this.forEach((Actor s) -> s.setVisible(false));
    }
}
