package starter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector3;
import controller.Game;
import tools.Point;

/**
 * The entry class to create your own implementation.
 *
 * <p>This class is directly derived form {@link Game} and acts as the {@link
 * com.badlogic.gdx.Game}.
 */
public class MyGame extends Game {
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
        processPressedKeys();
    }

    private void processPressedKeys() {
        checkZoomingKeys();
        checkMovingKeys();
    }

    private void checkZoomingKeys() {
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

    private void checkMovingKeys() {
        if (Gdx.input.isKeyPressed(Input.Keys.U)) {
            Vector3 position = camera.position;
            camera.setFocusPoint(new Point(position.x, position.y + 1));
        }
        if (Gdx.input.isKeyPressed(Input.Keys.J)) {
            Vector3 position = camera.position;
            camera.setFocusPoint(new Point(position.x, position.y - 1));
        }
        if (Gdx.input.isKeyPressed(Input.Keys.H)) {
            Vector3 position = camera.position;
            camera.setFocusPoint(new Point(position.x - 1, position.y));
        }
        if (Gdx.input.isKeyPressed(Input.Keys.K)) {
            Vector3 position = camera.position;
            camera.setFocusPoint(new Point(position.x + 1, position.y));
        }
    }

    @Override
    public void onLevelLoad() {
        camera.setFocusPoint(levelAPI.getCurrentLevel().getStartTile().getCoordinate().toPoint());
    }

    /**
     * The program entry point to start the dungeon.
     *
     * @param args command line arguments, but not needed.
     */
    public static void main(String[] args) {
        // start the game
        DesktopLauncher.run(new MyGame());
    }
}
