package starter;

import controller.Game;
import level.generator.postGeneration.WallGenerator;
import level.generator.randomwalk.RandomWalkGenerator;

/**
 * The entry class to create your own implementation.
 *
 * <p>This class is directly derived form {@link Game} and acts as the {@link
 * com.badlogic.gdx.Game}.
 */
public class MyGame extends Game {

    @Override
    protected void setup() {
        // set the default generator
        levelAPI.setGenerator(new WallGenerator(new RandomWalkGenerator()));
        // load the first level
        levelAPI.loadLevel();
    }

    @Override
    protected void frame() {}

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
