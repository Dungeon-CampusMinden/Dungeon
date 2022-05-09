package controller;

import static com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import graphic.DungeonCamera;
import graphic.HUDPainter;
import graphic.Painter;
import level.IOnLevelLoader;
import level.LevelAPI;
import level.generator.IGenerator;
import level.generator.dungeong.levelg.LevelG;
import tools.Constants;

/** The heart of the framework. From here all strings are pulled. */
public abstract class MainController extends ScreenAdapter implements IOnLevelLoader {
    /**
     * The batch is necessary to draw ALL the stuff. Every object that uses draw need to know the
     * batch.
     */
    protected SpriteBatch batch;

    protected EntityController entityController;
    protected DungeonCamera camera;
    /** Draws objects */
    protected Painter painter;
    /** This batch is used to draw the HUD elements on it. */
    protected SpriteBatch hudBatch;

    protected HUDController hudController;
    /** Draws hud */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings protected HUDPainter hudPainter;

    protected LevelAPI levelAPI;
    /** Generates the level */
    protected IGenerator generator;

    private boolean doFirstFrame = true;

    // --------------------------- OWN IMPLEMENTATION ---------------------------
    protected abstract void setup();

    protected abstract void beginFrame();

    protected abstract void endFrame();

    // --------------------------- END OWN IMPLEMENTATION ------------------------

    /**
     * Main game loop. Redraws the dungeon and calls the own implementation (beginFrame, endFrame
     * and onLevelLoad).
     *
     * @param delta Time since last loop.
     */
    @Override
    public void render(float delta) {
        if (doFirstFrame) {
            firstFrame();
        }
        batch.setProjectionMatrix(camera.combined);
        if (runLoop()) {
            beginFrame();
            if (runLoop()) {
                clearScreen();
                levelAPI.update();
                if (runLoop()) {
                    entityController.update();
                    if (runLoop()) {
                        camera.update();
                        if (runLoop()) {
                            hudController.update();
                            if (runLoop()) {
                                endFrame();
                            }
                        }
                    }
                }
            }
        }
    }

    private void clearScreen() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL_COLOR_BUFFER_BIT);
    }

    private void firstFrame() {
        doFirstFrame = false;
        entityController = new EntityController();
        setupCameras();
        painter = new Painter(camera);
        hudPainter = new HUDPainter();
        hudController = new HUDController(hudBatch);
        generator =
                new LevelG(
                        Constants.getPathToRoomTemplates(),
                        Constants.getPathToReplacements(),
                        Constants.getPathToGraph()); // DungeonG
        levelAPI = new LevelAPI(batch, painter, generator, this);
        setup();
    }

    public void setSpriteBatch(SpriteBatch batch) {
        this.batch = batch;
    }

    public void setHudBatch(SpriteBatch batch) {
        this.hudBatch = batch;
    }

    protected boolean runLoop() {
        return true;
    }

    private void setupCameras() {
        camera = new DungeonCamera(null, Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT);
        camera.zoom = Constants.DEFAULT_ZOOM_FACTOR;

        // See also:
        // https://stackoverflow.com/questions/52011592/libgdx-set-ortho-camera
    }
}
