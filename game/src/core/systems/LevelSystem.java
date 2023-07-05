package core.systems;

import core.Entity;
import core.Game;
import core.System;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.elements.ILevel;
import core.level.generator.IGenerator;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.level.utils.LevelSize;
import core.utils.IVoidFunction;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.Painter;
import core.utils.components.draw.PainterConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/** Manages the level. */
public class LevelSystem extends System {
    private final Painter painter;
    private final IVoidFunction onLevelLoader;
    private IGenerator gen;
    /** Currently used level-size configuration for generating new level */
    private static LevelSize LEVELSIZE = LevelSize.SMALL;
    /**
     * The currently loaded level of the game.
     *
     * @see ILevel
     */
    private static ILevel currentLevel;

    private final Logger levelAPI_logger = Logger.getLogger(this.getClass().getName());

    /**
     * @param painter Who draws?
     * @param generator Level generator
     * @param onLevelLoader Object that implements the onLevelLoad method.
     */
    public LevelSystem(Painter painter, IGenerator generator, IVoidFunction onLevelLoader) {
        super(PlayerComponent.class, PositionComponent.class);
        this.gen = generator;
        this.painter = painter;
        this.onLevelLoader = onLevelLoader;
    }

    /**
     * Load a new Level
     *
     * @param size The size that the level should have
     * @param label The design that the level should have
     */
    public void loadLevel(LevelSize size, DesignLabel label) {
        currentLevel = gen.level(label, size);
        onLevelLoader.execute();
        levelAPI_logger.info("A new level was loaded.");
    }

    /**
     * Load a new level with random size and the given design
     *
     * @param designLabel The design that the level should have
     */
    public void loadLevel(DesignLabel designLabel) {
        loadLevel(LevelSize.randomSize(), designLabel);
    }

    /**
     * Load a new level with the given size and a random design
     *
     * @param size wanted size of the level
     */
    public void loadLevel(LevelSize size) {
        loadLevel(size, DesignLabel.randomDesign());
    }

    /** Load a new level with random size and random design. */
    public void loadLevel() {
        loadLevel(LevelSize.randomSize(), DesignLabel.randomDesign());
    }

    /**
     * @return The currently loaded level.
     */
    public static ILevel currentLevel() {
        return currentLevel;
    }

    public static void currentLevel(ILevel level) {
        currentLevel = level;
    }

    protected void drawLevel() {
        Map<String, PainterConfig> mapping = new HashMap<>();

        Tile[][] layout = currentLevel.layout();
        for (Tile[] tiles : layout) {
            for (int x = 0; x < layout[0].length; x++) {
                Tile t = tiles[x];
                if (t.levelElement() != LevelElement.SKIP) {
                    String texturePath = t.texturePath();
                    if (!mapping.containsKey(texturePath)) {
                        mapping.put(texturePath, new PainterConfig(texturePath));
                    }
                    painter.draw(t.position(), texturePath, mapping.get(texturePath));
                }
            }
        }
    }

    /**
     * @return The currently used Level-Generator
     */
    public IGenerator generator() {
        return gen;
    }

    /**
     * Set the level generator
     *
     * @param generator new level generator
     */
    public void generator(IGenerator generator) {
        gen = generator;
    }

    /**
     * Sets the current level to the given level and calls onLevelLoad().
     *
     * @param level The level to be set.
     */
    public void level(ILevel level) {
        currentLevel = level;
        onLevelLoader.execute();
    }

    /**
     * Check if the given en entity is on the end-tile
     *
     * @param entity entity to check for
     * @return true if the entity is on the end-tile, false if not
     */
    private boolean isOnEndTile(Entity entity) {
        PositionComponent pc =
                entity.fetch(PositionComponent.class)
                        .orElseThrow(
                                () ->
                                        MissingComponentException.build(
                                                entity, PositionComponent.class));
        Tile currentTile = Game.tileAT(pc.position());
        return currentTile.equals(Game.endTile());
    }

    public static LevelSize levelSize() {
        return LEVELSIZE;
    }

    public static void levelSize(LevelSize levelSize) {
        LEVELSIZE = levelSize;
    }

    @Override
    public void execute() {
        java.lang.System.out.println("LEVEL!!!!!!!!!");
        drawLevel();
        if (entityStream().anyMatch(this::isOnEndTile)) loadLevel(levelSize());
    }
}
