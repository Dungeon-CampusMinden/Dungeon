package controller;

import static com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT;

import basiselements.DungeonElement;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import graphic.DungeonCamera;
import graphic.Painter;
import hud.LevelEditorGui;
import java.util.ArrayList;
import java.util.List;
import level.IOnLevelLoader;
import level.LevelAPI;
import level.LevelEditor;
import level.generator.IGenerator;
import level.generator.randomwalk.RandomWalkGenerator;
import level.tools.LevelElement;
import tools.Constants;

/** The heart of the framework. From here all strings are pulled. */
public abstract class Game extends ScreenAdapter implements IOnLevelLoader {
    /**
     * The batch is necessary to draw ALL the stuff. Every object that uses draw need to know the
     * batch.
     */
    protected SpriteBatch batch;

    /** The stage is used to draw elements on the UI */
    protected Stage stage;

    protected InputMultiplexer inputMultiplexer;

    /** Contais all Controller of the Dungeon */
    protected List<AbstractController<?>> controller;

    protected EntityController entityController;
    protected DungeonCamera camera;
    /** Draws objects */
    protected Painter painter;

    protected LevelAPI levelAPI;
    /** Generates the level */
    protected IGenerator generator;
    /** Editor used to modify the level during runtime */
    protected LevelEditor levelEditor;
    /** Gui used to display the tools of the level editor */
    protected LevelEditorGui levelEditorGui;

    private boolean doFirstFrame = true;

    // --------------------------- OWN IMPLEMENTATION ---------------------------

    /** Called once at the beginning of the game. */
    protected abstract void setup();

    /** Called at the beginning of each frame. Before the controllers call <code>update</code>. */
    protected abstract void frame();

    // --------------------------- END OWN IMPLEMENTATION ------------------------

    /** Create a new game instance */
    public Game() {
        inputMultiplexer = new InputMultiplexer();
    }

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
            frame();
            if (runLoop()) {
                clearScreen();
                levelAPI.update();
                if (runLoop()) {
                    controller.forEach(AbstractController::update);
                    if (runLoop()) {
                        camera.update();
                    }
                }
                stage.draw();
                stage.act(delta);
                stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            }
        }
    }

    private void clearScreen() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL_COLOR_BUFFER_BIT);
    }

    private void firstFrame() {
        doFirstFrame = false;
        controller = new ArrayList<>();
        setupCameras();
        painter = new Painter(batch, camera);
        entityController = new EntityController(painter);
        controller.add(entityController);
        generator = new RandomWalkGenerator();
        levelAPI = new LevelAPI(batch, painter, generator, this);
        setup();
        if (Constants.ENABLE_LEVEL_EDITOR) {
            LevelEditor.addSpawnableLevelElement(LevelElement.WALL, "Wall");
            LevelEditor.addSpawnableLevelElement(LevelElement.FLOOR, "Floor");
            LevelEditor.addSpawnableLevelElement(LevelElement.HOLE, "Hole");
            LevelEditor.addSpawnableLevelElement(LevelElement.SKIP, "Skip");
            LevelEditor.addSpawnableLevelElement(LevelElement.DOOR, "Door");
            levelEditor = new LevelEditor(levelAPI, this, camera, this::addDungeonElement);
            inputMultiplexer.addProcessor(levelEditor);
            levelEditorGui = new LevelEditorGui(levelEditor, stage);
        }
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    protected boolean addDungeonElement(DungeonElement object) {
        return entityController.add(object);
    }

    public void setSpriteBatch(SpriteBatch batch) {
        this.batch = batch;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        inputMultiplexer.addProcessor(stage);
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

    /** Called when the game is closed */
    @Override
    public void dispose() {
        super.dispose();
        if (entityController != null) {
            entityController.stop();
        }
    }

    /** Called when the game is paused */
    @Override
    public void pause() {
        super.pause();
        if (entityController != null) {
            entityController.pause();
        }
    }

    /** Called when the paused game is resumed */
    @Override
    public void resume() {
        super.resume();
        if (entityController != null) {
            entityController.resume();
        }
    }
}
