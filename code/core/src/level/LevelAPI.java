package level;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import graphic.Painter;
import level.elements.Level;
import level.elements.room.Room;
import level.elements.room.Tile;
import level.generator.IGenerator;
import level.generator.dungeong.graphg.NoSolutionException;
import level.tools.DesignLabel;
import level.tools.LevelElement;
import tools.Point;

/** Manages the level. */
public class LevelAPI {
    private final SpriteBatch batch;
    private final Painter painter;
    private final IOnLevelLoader onLevelLoader;
    private IGenerator gen;
    private Level currentLevel;

    /**
     * @param batch Batch on which to draw.
     * @param painter Who draws?
     * @param generator Level generator
     * @param onLevelLoader Object that implements the onLevelLoad method.
     */
    public LevelAPI(
            SpriteBatch batch,
            Painter painter,
            IGenerator generator,
            IOnLevelLoader onLevelLoader) {
        this.gen = generator;
        this.batch = batch;
        this.painter = painter;
        this.onLevelLoader = onLevelLoader;
    }

    /**
     * Load a new level.
     *
     * @throws NoSolutionException if no level can be loaded.
     */
    public void loadLevel() throws NoSolutionException {
        currentLevel = gen.getLevel();
        onLevelLoader.onLevelLoad();
    }

    /**
     * Load a new level with the given configuration.
     *
     * @param nodes Number of rooms in the level
     * @param edges Number of loops in the level
     * @param designLabel design of the level
     * @throws NoSolutionException if no level can be loaded.
     */
    public void loadLevel(int nodes, int edges, DesignLabel designLabel)
            throws NoSolutionException {
        currentLevel = gen.getLevel(nodes, edges, designLabel);
        onLevelLoader.onLevelLoad();
    }

    /** Draw level */
    public void update() {
        drawLevel();
    }

    public Level getCurrentLevel() {
        return currentLevel;
    }

    private void drawLevel() {
        for (Room r : getCurrentLevel().getRooms())
            for (int y = 0; y < r.getLayout().length; y++)
                for (int x = 0; x < r.getLayout()[0].length; x++) {
                    Tile t = r.getLayout()[y][x];
                    if (t.getLevelElement() != LevelElement.SKIP)
                        painter.draw(
                                t.getTexturePath(),
                                new Point(t.getCoordinate().x, t.getCoordinate().y),
                                batch);
                }
    }

    /**
     * Set the level generator
     *
     * @param generator new level generator
     */
    public void setGenerator(IGenerator generator) {
        gen = generator;
    }

    /**
     * Sets the current level to the given level and calls onLevelLoad().
     *
     * @param level The level to be set.
     */
    public void setLevel(Level level) {
        currentLevel = level;
        onLevelLoader.onLevelLoad();
    }
}
