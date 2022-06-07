package level;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import graphic.Painter;
import level.elements.ILevel;
import level.elements.Tile;
import level.generator.IGenerator;
import level.tools.DesignLabel;
import level.tools.LevelElement;
import tools.Point;

/** Manages the level. */
public class LevelAPI {
    private final SpriteBatch batch;
    private final Painter painter;
    private final IOnLevelLoader onLevelLoader;
    private IGenerator gen;
    private ILevel currentLevel;

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

    /** Load a new level. */
    public void loadLevel() {
        currentLevel = gen.getLevel();
        onLevelLoader.onLevelLoad();
    }

    /**
     * Load a new level
     *
     * @param designLabel The design that the level should have
     */
    public void loadLevel(DesignLabel designLabel) {
        currentLevel = gen.getLevel(designLabel);
        onLevelLoader.onLevelLoad();
    }

    /** Draw level */
    public void update() {
        drawLevel();
    }

    /**
     * @return The currently loaded level.
     */
    public ILevel getCurrentLevel() {
        return currentLevel;
    }

    protected void drawLevel() {
        Tile[][] layout = currentLevel.getLayout();
        for (int y = 0; y < layout.length; y++) {
            for (int x = 0; x < layout[0].length; x++) {
                Tile t = layout[y][x];
                if (t.getLevelElement() != LevelElement.SKIP) {
                    painter.draw(
                            t.getTexturePath(),
                            new Point(t.getCoordinate().x, t.getCoordinate().y),
                            batch);
                }
            }
        }
    }

    /**
     * @return The currently used Level-Generator
     */
    public IGenerator getGenerator() {
        return gen;
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
    public void setLevel(ILevel level) {
        currentLevel = level;
        onLevelLoader.onLevelLoad();
    }
}
