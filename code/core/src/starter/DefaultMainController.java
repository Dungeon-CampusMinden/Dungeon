package starter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import controller.MainController;

public class DefaultMainController extends MainController {
    private int zoomLevel = 10;

    @Override
    protected void setup() {
        // set the default generator
        // levelAPI.setGenerator(new RandomWalkGenerator());
        // load the first level
        levelAPI.loadLevel();
    }

    @Override
    protected void frame() {
        if (Gdx.input.isKeyPressed(Input.Keys.I)) {
            zoomLevel++;
            camera.zoom = 0.05f * zoomLevel;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.O)) {
            zoomLevel--;
            if (zoomLevel <= 0) {
                zoomLevel = 1;
            }
            camera.zoom = 0.05f * zoomLevel;
        }
    }

    @Override
    public void onLevelLoad() {}

    /**
     * The program entry point to start the dungeon.
     *
     * @param args command line arguments, but not needed.
     */
    public static void main(String[] args) {
        // start the game
        DesktopLauncher.run(new DefaultMainController());
    }
}
