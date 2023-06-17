package graphic.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Align;
import controller.ScreenController;
import minigame.Minigame;
import tools.Constants;
import tools.Point;

public class MinigameScreen<T extends Actor> extends ScreenController<T> {

    private ScreenImage lockImage;
    private ScreenImage[] circles;

    /** Creates a new PauseMenu with a new Spritebatch */
    public MinigameScreen() {
        this(new SpriteBatch());
    }

    /** Creates a new PauseMenu with a given Spritebatch */
    public MinigameScreen(SpriteBatch batch) {
        super(batch);
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

    public void update(Minigame game) {
        if (lockImage != null) {
            remove((T) lockImage);
        }
        if (circles != null) {
            for (int i = 0; i < circles.length; i++) {
                if (circles[i] != null) {
                    remove((T) circles[i]);
                }
            }
        }
        lockImage = new ScreenImage(game.getAnimationFrame(), new Point(0, 0));
        circles = new ScreenImage[game.finishAmount()];
        for (int i = 0; i < game.finishAmount(); i++) {
            if (i < game.getPickedCount())
                circles[i] = new ScreenImage("greenCircle", new Point(0, 0));
            else
                circles[i] = new ScreenImage(determineCircleState(game.currentState()), new Point(0, 0));
        }
    }

    private String determineCircleState(byte state) {
        switch (state) {
            case 0:
                return "emptyCircle";
            case 1:
                return "grayCircle";
            case 2:
                return "whiteCircle";
            default:
                return "emptyCircle";
        }
    }
}
