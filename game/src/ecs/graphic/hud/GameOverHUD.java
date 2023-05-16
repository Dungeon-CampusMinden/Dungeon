package ecs.graphic.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Align;
import controller.ScreenController;
import tools.Constants;
import tools.Point;

public class GameOverHUD<T extends Actor> extends ScreenController<T> {
    /** Creates a new PauseMenu with a new Spritebatch */
    public GameOverHUD(){
        this(new SpriteBatch());
    }
    /**
     * Creates a Screencontroller with a ScalingViewport which stretches the ScreenElements on
     * resize
     *
     * @param batch the batch which should be used to draw with
     */
    public GameOverHUD(SpriteBatch batch) {
        super(batch);
        ScreenText screenText =
            new ScreenText (
                "Game Over",
            new Point(
                0,0),5,
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
    /** shows the Game Over screen */
    public void showMenu() {
        this.forEach((Actor s) -> s.setVisible(true));
    }

    public void hideMenu() {
        this.forEach((Actor s) -> s.setVisible(false));
    }
}
