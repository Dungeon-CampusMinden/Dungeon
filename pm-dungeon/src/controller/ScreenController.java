package controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import tools.Constants;

/** A class to manage <code>Actor</code> and <code>Removable<code>s.
 * <p>Provides methods for add, remove, update and reorder elements.</p>
 *
 * @param <T> A libGDX <code>Actor</code> that has also the removable property.
 */
public class ScreenController<T extends Actor> extends AbstractController<T> {
    protected Stage stage;

    /**
     * Creates a Screencontroller with a ScalingViewport which stretches the ScreenElements on
     * resize
     *
     * @param batch the batch which should be used to draw with
     */
    public ScreenController(SpriteBatch batch) {
        super();
        stage =
                new Stage(
                        new ScalingViewport(
                                Scaling.stretch, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT),
                        batch);

        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void update() {
        super.update();

        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    @Override
    public void process(T e) {}

    @Override
    public boolean add(T screenElement, ControllerLayer layer) {
        boolean add = super.add(screenElement, layer);
        if (add) {
            stage.addActor(screenElement);
            reorder();
        }
        return add;
    }

    /**
     * Synchronizes the z-index of each ScreenElement with this collection.
     *
     * <p>That is, the order in which the ScreenElements are to be drawn.
     */
    private void reorder() {
        int index = 0;
        for (T se : this) {
            se.setZIndex(index++);
        }
    }

    @Override
    public boolean remove(T e) {
        e.remove();
        return super.remove(e);
    }
}
