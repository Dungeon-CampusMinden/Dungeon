package graphic.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Align;

import configuration.ConfigMap;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import controller.ScreenController;
import ecs.tools.Flags.Flag;
import graphic.hud.ScreenButton;
import graphic.hud.TextButtonListener;
import graphic.hud.TextButtonStyleBuilder;
import starter.Game;
import tools.Constants;
import tools.Point;
import ecs.tools.Flags.Flag;

public class GameOverMenu<T extends Actor> extends ScreenController<T> {
    private Game game;

    /** Creates a new GameOverMenu with a new Spritebatch */
    public GameOverMenu(Game game) {
        this(new SpriteBatch());
        this.game = game;
    }

    /** Creates a new GameOverMenu with a given Spritebatch */
    public GameOverMenu(SpriteBatch batch) {
        super(batch);
        ScreenText screenText = new ScreenText(
                "Game Over",
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

        ScreenButton screenButtonQuit = new ScreenButton(
                "Quit",
                new Point(0, 50),
                new TextButtonListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        throw new Flag();
                    }
                },
                new TextButtonStyleBuilder(FontBuilder.DEFAULT_FONT)
                        .setFontColor(Color.RED)
                        .setDownFontColor(Color.BLUE)
                        .setOverFontColor(Color.YELLOW)
                        .build());
        screenButtonQuit.setPosition(
                (Constants.WINDOW_WIDTH) / 3f - screenText.getWidth(),
                (Constants.WINDOW_HEIGHT) / 2.5f + screenText.getHeight(),
                Align.center | Align.bottom);
        add((T) screenButtonQuit);

        ScreenButton screenButtonRestart = new ScreenButton(
                "Restart",
                new Point(0, 50),
                new TextButtonListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        game.restart();
                    }
                },
                new TextButtonStyleBuilder(FontBuilder.DEFAULT_FONT)
                        .setFontColor(Color.RED)
                        .setDownFontColor(Color.BLUE)
                        .setOverFontColor(Color.YELLOW)
                        .build());
        screenButtonRestart.setPosition(
                2 * (Constants.WINDOW_WIDTH) / 3f - screenText.getWidth(),
                (Constants.WINDOW_HEIGHT) / 2.5f + screenText.getHeight(),
                Align.center | Align.bottom);
        add((T) screenButtonRestart);

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
