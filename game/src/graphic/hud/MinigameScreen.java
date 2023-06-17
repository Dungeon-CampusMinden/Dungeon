package graphic.hud;

import java.util.logging.Logger;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Align;
import controller.ScreenController;
import logging.CustomLogLevel;
import minigame.Minigame;
import tools.Constants;
import tools.Point;

public class MinigameScreen<T extends Actor> extends ScreenController<T> {

    private static final Logger minigameScreenLogger = Logger.getLogger(MinigameScreen.class.getName());

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
        minigameScreenLogger.log(CustomLogLevel.DEBUG, "New MinigameScreen created");
    }

    /** shows the Menu */
    public void showMenu() {
        this.forEach((Actor s) -> s.setVisible(true));
        minigameScreenLogger.info("MinigameScreen is now visible");
    }

    /** hides the Menu */
    public void hideMenu() {
        this.forEach((Actor s) -> s.setVisible(false));
        minigameScreenLogger.info("MinigameScreen is now hidden");
    }

    /** Updates all elements */
    public void updateScreen(Minigame game) {
        forEach((T s) -> remove(s));
        minigameScreenLogger.log(CustomLogLevel.DEBUG, "All elements deleted");
        lockImage = new ScreenImage(game.getAnimationFrame(),
                new Point(Constants.WINDOW_WIDTH / 2, Constants.WINDOW_HEIGHT / 2));
        add((T) lockImage);
        minigameScreenLogger.info("Lock image updated");
        circles = new ScreenImage[game.finishAmount()];
        for (int i = 0; i < game.finishAmount(); i++) {
            Point p = new Point((Constants.WINDOW_WIDTH / (circles.length + 1)) * (i + 1), Constants.WINDOW_HEIGHT / 4);
            if (i == game.getPickedCount())
                circles[i] = new ScreenImage(determineCircleState(game.currentState()), p);
            else if (i < game.getPickedCount())
                circles[i] = new ScreenImage("minigame/greenCircle.png", p);
            else
                circles[i] = new ScreenImage("minigame/emptyCircle.png", p);
            add((T) circles[i]);
        }
        minigameScreenLogger.info("Circles updated");
        showMenu();
        minigameScreenLogger.info("Minigames updated and set visible");
    }

    private String determineCircleState(byte state) {
        switch (state) {
            case 0:
                return "minigame/emptyCircle.png";
            case 1:
                return "minigame/grayCircle.png";
            case 2:
                return "minigame/whiteCircle.png";
            default:
                return "minigame/emptyCircle.png";
        }
    }
}
