package starter;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

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

    /** {@link com.badlogic.gdx.Game} class that delegates to the {@link Game}. Just some setup. */
    public LibgdxSetup(Game game) {
        this.game = game;
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        hudBatch = new SpriteBatch();
        game.setSpriteBatch(batch);
        setScreen(game);
    }

    @Override
    public void dispose() {
        batch.dispose();
        hudBatch.dispose();
    }
}
