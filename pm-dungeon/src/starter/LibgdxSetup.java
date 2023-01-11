package starter;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import controller.Game;
import tools.Constants;

/** {@link com.badlogic.gdx.Game} class that delegates to the {@link Game}. Just some setup. */
public class LibgdxSetup extends com.badlogic.gdx.Game {

    private final Game game;

    /**
     * The batch is necessary to draw ALL the stuff. Every object that uses draw need to know the
     * batch.
     */
    private SpriteBatch batch;
    /** This batch is used to draw the HUD elements on it. */
    private SpriteBatch hudBatch;
    /** The stage is used to draw complex HUD elements */
    private Stage stage;

    /** {@link com.badlogic.gdx.Game} class that delegates to the {@link Game}. Just some setup. */
    public LibgdxSetup(Game game) {
        this.game = game;
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        hudBatch = new SpriteBatch();
        stage =
                new Stage(
                        new ScalingViewport(
                                Scaling.stretch,
                                Constants.WINDOW_WIDTH,
                                Constants.WINDOW_HEIGHT,
                                new OrthographicCamera(
                                        (float) Constants.WINDOW_WIDTH / 2f,
                                        (float) Constants.WINDOW_HEIGHT / 2f)),
                        hudBatch);
        game.setSpriteBatch(batch);
        game.setStage(stage);
        setScreen(game);
    }

    @Override
    public void dispose() {
        batch.dispose();
        hudBatch.dispose();
        stage.dispose();
        game.dispose();
    }
}
