package controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import graphic.DungeonCamera;
import graphic.HUDCamera;
import tools.Constants;

import static com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT;

/** The heart of the framework. From here all strings are pulled. */
public class MainController extends ScreenAdapter {
    protected SpriteBatch batch;
    protected LevelController levelController;
    protected EntityController entityController;
    protected DungeonCamera camera;
    protected HUDController hud;
    protected GraphicController graphicController;

    private boolean doFirstFrame = true;

    // --------------------------- OWN IMPLEMENTATION ---------------------------
    protected void setup() {}

    protected void beginFrame() {}

    protected void endFrame() {}

    protected void onLevelLoad() {}
    // --------------------------- END OWN IMPLEMENTATION ------------------------

    /**
     * Main game loop. Redraws the dungeon and calls the own implementation (beginFrame, endFrame
     * and onLevelLoad).
     *
     * @param delta Time since last loop.
     */
    @Override
    public final void render(float delta) {
        if (doFirstFrame) {
            firstFrame();
        }

        // clears the screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL_COLOR_BUFFER_BIT);
        batch.setProjectionMatrix(camera.combined);

        beginFrame();
        levelController.update();
        entityController.update();
        camera.update();
        hud.update();
        endFrame();
    }

    private void firstFrame() {
        doFirstFrame = false;
        entityController = new EntityController();
        setupCamera();
        graphicController = new GraphicController(camera);
        hud = new HUDController(new SpriteBatch(), graphicController, new HUDCamera());
        levelController = new LevelController(batch, graphicController);
        setup();
    }

    public void setSpriteBatch(SpriteBatch batch) {
        this.batch = batch;
    }

    /** Setting up the camera. */
    private void setupCamera() {
        camera = new DungeonCamera(null, Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT);
        camera.zoom = Constants.DEFAULT_ZOOM_FACTOR;

        // See also:
        // https://stackoverflow.com/questions/52011592/libgdx-set-ortho-camera
    }
}
