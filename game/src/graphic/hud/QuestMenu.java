package graphic.hud;

import java.util.logging.Logger;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Align;
import controller.ScreenController;
import starter.Game;
import tools.Constants;
import tools.Point;

public class QuestMenu<T extends Actor> extends ScreenController<T> {

    private ScreenText screenText;

    private static final Logger LOGGER = Logger.getLogger(QuestMenu.class.getSimpleName());

    /** Creates a new QuestMenu with a new Spritebatch */
    public QuestMenu() {
        this(new SpriteBatch());
    }

    /** Creates a new QuestMenu with a given Spritebatch */
    public QuestMenu(SpriteBatch batch) {
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

    /**
     * Displays the quest on the screen
     * 
     * @param text Text to display
     */
    public void display(String text, float timeSeconds) {
        if (screenText != null)
            remove((T) screenText);
        screenText = new ScreenText(
                text,
                new Point(0, 0),
                2,
                new LabelStyleBuilder(FontBuilder.DEFAULT_FONT)
                        .setFontcolor(Color.GREEN)
                        .build());
        screenText.setFontScale(2);
        screenText.setPosition(
                ((Constants.WINDOW_WIDTH) / 2f - screenText.getWidth()),
                (Constants.WINDOW_HEIGHT) / 1.5f + screenText.getHeight(),
                Align.center | Align.bottom);
        add((T) screenText);
        LOGGER.info("New Quest displayed: " + text);
        showMenu();
        Game.questDisplayTime = 3 * Constants.FRAME_RATE;
    }
}
