package graphic.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Align;
import controller.ScreenController;
import ecs.entities.Hero;
import tools.Constants;
import tools.Point;

public class GameOverMenu <T extends Actor> extends ScreenController<T> {

    public GameOverMenu(){this (new SpriteBatch());}

    public boolean isMenuOpen = false;

    public boolean isMenuOpen() {
        return isMenuOpen;
    }

    //specifies how the GameOverMenu looks
    public GameOverMenu(SpriteBatch batch) {
        super(batch);
        ScreenText screenText =
            new ScreenText(
                "   Game Over\nPress K to exit\nPress L to restart",
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
        this.hideEndMenu();
    }

    /**
     * makes the GameOverMenu visible
    **/
    public void showEndMenu(){
        this.forEach((Actor s) -> s.setVisible(true));
        System.out.println("gameOver");
        isMenuOpen = true;
    }

    /**
     * makes the GameOverMenu invisible
     **/
    public void hideEndMenu(){
        this.forEach((Actor s) -> s.setVisible(false));
        isMenuOpen =false;
    }
}
