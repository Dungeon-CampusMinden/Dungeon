package controller;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/** ApplicationListener that delegates to the MainGameController. Just some setup. */
public class LibgdxSetup extends Game {

    private final MainController mc;

    /**
     * The batch is necessary to draw ALL the stuff. Every object that uses draw need to know the
     * batch.
     */
    private SpriteBatch batch;

    /**
     * <code>ApplicationListener</code> that delegates to the MainGameController. Just some setup.
     */
    public LibgdxSetup(MainController mc) {
        this.mc = mc;
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        mc.setSpriteBatch(batch);
        setScreen(mc);
    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}
