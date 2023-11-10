package core;

import com.badlogic.gdx.ai.pfa.GraphPath;

import contrib.configuration.KeyboardConfig;

import core.components.PositionComponent;
import core.game.ECSManagment;
import core.game.PreRunConfiguration;
import core.level.Tile;
import core.level.elements.ILevel;
import core.level.utils.Coordinate;
import core.level.utils.LevelElement;
import core.level.utils.LevelSize;
import core.systems.*;
import core.utils.IVoidFunction;
import core.utils.Point;
import core.utils.components.MissingComponentException;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Stream;

/** The heart of the framework. From here all strings are pulled. */
public final class Game {

    private static final Logger LOGGER = Logger.getLogger("Game");

    // ====================PreRunConfiguration====================

    public static int windowWidth() {
        return PreRunConfiguration.windowWidth();
    }

    public static void windowWidth(int windowWidth) {
        PreRunConfiguration.windowWidth(windowWidth);
    }

    public static int windowHeight() {
        return PreRunConfiguration.windowHeight();
    }

    public static void windowHeight(int windowHeight) {
        PreRunConfiguration.windowHeight(windowHeight);
    }

    public static int frameRate() {
        return PreRunConfiguration.frameRate();
    }

    public static void frameRate(int frameRate) {
        PreRunConfiguration.frameRate(frameRate);
    }

    public static boolean fullScreen() {
        return PreRunConfiguration.fullScreen();
    }

    public static void fullScreen(boolean fullscreen) {
        PreRunConfiguration.fullScreen(fullscreen);
    }

    public static String windowTitle() {
        return PreRunConfiguration.windowTitle();
    }

    public static void windowTitle(String windowTitle) {
        PreRunConfiguration.windowTitle(windowTitle);
    }

    public static String logoPath() {
        return PreRunConfiguration.logoPath();
    }

    public static void logoPath(String logoPath) {
        PreRunConfiguration.logoPath(logoPath);
    }

    public static boolean disableAudio() {
        return PreRunConfiguration.disableAudio();
    }

    public static void disableAudio(boolean disableAudio) {
        PreRunConfiguration.disableAudio(disableAudio);
    }

    public static IVoidFunction userOnFrame() {
        return PreRunConfiguration.userOnFrame();
    }

    public static void userOnFrame(IVoidFunction userOnFrame) {
        PreRunConfiguration.userOnFrame(userOnFrame);
    }

    public static IVoidFunction userOnSetup() {
        return PreRunConfiguration.userOnSetup();
    }

    public static void userOnSetup(IVoidFunction userOnSetup) {
        PreRunConfiguration.userOnSetup(userOnSetup);
    }

    public static Consumer<Boolean> userOnLevelLoad() {
        return PreRunConfiguration.userOnLevelLoad();
    }

    public static void userOnLevelLoad(Consumer<Boolean> userOnLevelLoad) {
        PreRunConfiguration.userOnLevelLoad(userOnLevelLoad);
    }

    public static void initBaseLogger() {
        PreRunConfiguration.initBaseLogger();
    }

    public static void loadConfig(
            String s,
            Class<KeyboardConfig> keyboardConfigClass,
            Class<core.configuration.KeyboardConfig> keyboardConfigClass1)
            throws IOException {
        PreRunConfiguration.loadConfig(s, keyboardConfigClass, keyboardConfigClass1);
    }

    /**
     * @return the currently loaded level
     */
    public static ILevel currentLevel() {
        return LevelSystem.level();
    }

    /**
     * Get the tile at the given point in the level
     *
     * <p>{@link Point#toCoordinate} will be used, to convert the point into a coordinate.
     *
     * @param p Point from where to get the tile
     * @return the tile at the given point.
     */
    public static Tile tileAT(Point p) {
        return currentLevel().tileAt(p);
    }

    /**
     * Get the tile at the given coordinate in the level
     *
     * @param c Coordinate from where to get the tile
     * @return the tile at the given coordinate.
     */
    public static Tile tileAT(Coordinate c) {
        return currentLevel().tileAt(c);
    }

    /**
     * @return a random Tile in the Level
     */
    public static Tile randomTile() {
        return currentLevel().randomTile();
    }

    /**
     * Get the end tile.
     *
     * @return The end tile.
     */
    public static Tile endTile() {
        return currentLevel().endTile();
    }

    /**
     * Get the start tile.
     *
     * @return The start tile.
     */
    public static Tile startTile() {
        return currentLevel().startTile();
    }

    /**
     * Returns the tile the given entity is standing on.
     *
     * @param entity entity to check for.
     * @return tile at the coordinate of the entity
     */
    public static Tile tileAtEntity(Entity entity) {
        return currentLevel().tileAtEntity(entity);
    }

    /**
     * Returns the entities on the given tile.
     *
     * @param t Tile to check for.
     * @return Stream of all entities on the given tile
     */
    public static Stream<Entity> entityAtTile(Tile t) {
        Tile tile = Game.tileAT(t.position());

        return ECSManagment.entityStream(Set.of(PositionComponent.class))
                .filter(
                        e ->
                                tileAT(
                                                e.fetch(PositionComponent.class)
                                                        .orElseThrow(
                                                                () ->
                                                                        MissingComponentException
                                                                                .build(
                                                                                        e,
                                                                                        PositionComponent
                                                                                                .class))
                                                        .position())
                                        .equals(tile));
    }

    /**
     * Get a random Tile
     *
     * @param elementType Type of the Tile
     * @return A random Tile of the given Type
     */
    public static Tile randomTile(LevelElement elementType) {
        return currentLevel().randomTile(elementType);
    }

    /**
     * Get the position of a random Tile as Point
     *
     * @return Position of the Tile as Point
     */
    public static Point randomTilePoint() {
        return currentLevel().randomTilePoint();
    }

    /**
     * Get the position of a random Tile as Point
     *
     * @param elementTyp Type of the Tile
     * @return Position of the Tile as Point
     */
    public static Point randomTilePoint(LevelElement elementTyp) {
        return currentLevel().randomTilePoint(elementTyp);
    }

    /**
     * Starts the indexed A* pathfinding algorithm a returns a path
     *
     * <p>Throws an IllegalArgumentException if start or end is non-accessible.
     *
     * @param start Start tile
     * @param end End tile
     * @return Generated path
     */
    public static GraphPath<Tile> findPath(Tile start, Tile end) {
        return currentLevel().findPath(start, end);
    }

    /**
     * Get the Position of the given entity in the level.
     *
     * @param entity Entity to get the current position from (needs a {@link PositionComponent}
     * @return Position of the given entity.
     */
    public static Point positionOf(Entity entity) {
        return currentLevel().positionOf(entity);
    }

    /**
     * Set the current level.
     *
     * <p>This method is for testing and debugging purposes.
     *
     * @param level New level
     */
    public static void currentLevel(ILevel level) {
        LevelSystem levelSystem = (LevelSystem) ECSManagment.systems().get(LevelSystem.class);
        if (levelSystem != null) levelSystem.loadLevel(level);
        else LOGGER.warning("Can not set Level because levelSystem is null.");
    }

    /**
     * The currently set level-Size.
     *
     * <p>This value is used for the generation of the next level.
     *
     * <p>The currently active level can have a different size.
     *
     * @return currently set level-Size.
     */
    public static LevelSize levelSize() {
        return LevelSystem.levelSize();
    }

    /**
     * Set the {@link LevelSize} of the next level.
     *
     * @param levelSize Size of the next level.
     */
    public static void levelSize(LevelSize levelSize) {
        LevelSystem.levelSize(levelSize);
    }
}
