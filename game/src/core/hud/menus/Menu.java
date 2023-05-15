package core.hud.menus;


import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Null;
import core.utils.controller.ScreenController;

public abstract class Menu<T extends Actor> extends ScreenController<T> {

    protected boolean isVisible;
    // TODO: outsource
    protected static final int buttonTextLabelScale = 2;

    /**
     * Creates a Screencontroller with a ScalingViewport which stretches the ScreenElements on
     * resize
     *
     * @param batch the batch which should be used to draw with
     */
    public Menu(@Null SpriteBatch batch, @Null Stage stage) {
        super(batch, stage);
    }

    /** shows the Menu */
    public void showMenu() {
        this.forEach((Actor s) -> s.setVisible(true));
        isVisible = true;
    }

    /** hides the Menu */
    public void hideMenu() {
        this.forEach((Actor s) -> s.setVisible(false));
        isVisible = false;
    }

    public boolean isVisible() {
        return isVisible;
    }
}
