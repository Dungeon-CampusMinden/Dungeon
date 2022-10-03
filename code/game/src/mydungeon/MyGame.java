package mydungeon;

import com.badlogic.gdx.Gdx;
import controller.MainController;
import level.generator.LevelLoader.LevelLoader;
import level.generator.dungeong.graphg.NoSolutionException;

public class MyGame extends Game {
    @Override
    protected void setup() {
        levelAPI.setGenerator(new LevelLoader());
        // load the first level
        try {
            levelAPI.loadLevel();
        } catch (NoSolutionException e) {
            System.out.println(
                    "Es konnte kein Level geladen werden, bitte den \"assets\" Ordner überprüfen.");
            Gdx.app.exit();
        }
    }

    @Override
    protected void beginFrame() {}

    @Override
    protected void endFrame() {}

    @Override
    public void onLevelLoad() {}

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
