package hamster;

import basiselements.DungeonElement;
import controller.Game;
import hamster.elements.Hamster;
import hamster.elements.Loot;
import java.util.ArrayList;
import java.util.List;
import level.LevelEditor;
import level.generator.hamster.HamsterGenerator;
import level.generator.postGeneration.WallGenerator;
import starter.DesktopLauncher;
import tools.Constants;
import tools.Point;

/**
 * The entry class for the Hamster Simulator
 *
 * <p>This class is directly derived form {@link Game} and acts as the {@link
 * com.badlogic.gdx.Game}.
 *
 * @author Maxim Fruendt
 */
public class HamsterSimulator extends Game {

    /** Lock used for synchronized access to the entity list */
    private final Object entityLock = new Object();

    /** Set up this game */
    @Override
    protected void setup() {
        // set the default generator
        levelAPI.setGenerator(new WallGenerator(new HamsterGenerator()));
        // load the first level
        levelAPI.loadLevel();

        if (Constants.ENABLE_LEVEL_EDITOR) {
            LevelEditor.addSpawnableObject(Loot.class, "Loot");
            LevelEditor.addSpawnableObject(Hamster.class, "Hamster");
        }
    }

    /** Callback that will be invoked every frame */
    @Override
    protected void frame() {}

    /** Callback that will be invoked when a new level is loaded */
    @Override
    public void onLevelLoad() {
        camera.setFocusPoint(levelAPI.getCurrentLevel().getStartTile().getCoordinate().toPoint());
    }

    /**
     * Get if the position in the level is accessible
     *
     * @param checkedPos Position which will be evaluated
     * @return True if position is accessible, else false
     */
    public boolean isLevelPosAccessible(Point checkedPos) {
        return levelAPI != null
                && levelAPI.getCurrentLevel().getTileAt(checkedPos.toCoordinate()).isAccessible();
    }

    /**
     * Check if the position is accessible for the given element
     *
     * @param element Element which will be evaluated
     * @param checkedPos New position for the element
     * @param collisionRange Range in which the element will collide with other elements
     * @return True if position is accessible, else false
     */
    public boolean isPosAccessibleForEntity(
            DungeonElement element, Point checkedPos, float collisionRange) {
        if (!isLevelPosAccessible(checkedPos)) {
            return false;
        }
        List<DungeonElement> collidingEntities =
                getCollidingEntitiesForEntity(element, checkedPos, collisionRange);
        return collidingEntities != null && collidingEntities.size() <= 0;
    }

    /**
     * Get all elements that collide with the input element
     *
     * @param element Element which will be evaluated
     * @param checkedPos New position for the element
     * @param collisionRange Range in which the element will collide with other elements
     * @return List of colliding entities
     */
    public List<DungeonElement> getCollidingEntitiesForEntity(
            DungeonElement element, Point checkedPos, float collisionRange) {
        synchronized (entityLock) {
            List<DungeonElement> collidingEntities = new ArrayList<>();
            for (DungeonElement otherElement : entityController) {
                if (element != otherElement) {
                    if (doPositionsCollide(
                            checkedPos, otherElement.getPosition(), collisionRange)) {
                        collidingEntities.add(otherElement);
                    }
                }
            }
            return collidingEntities;
        }
    }

    /**
     * Remove an entity from the game
     *
     * @param element Entity to be removed
     * @return True if remove was successful, else false
     */
    public boolean removeEntity(DungeonElement element) {
        synchronized (entityLock) {
            return entityController.remove(element);
        }
    }

    /**
     * Check if two positions collide
     *
     * @param pos1 Anchor position
     * @param pos2 Position to be checked
     * @param range Range of a possible collision
     * @return True if positions collide, else false
     */
    private boolean doPositionsCollide(Point pos1, Point pos2, float range) {
        return Math.abs(pos1.x - pos2.x) < range && Math.abs(pos1.y - pos2.y) < range;
    }

    /**
     * The program entry point to start the dungeon.
     *
     * @param args command line arguments, but not needed.
     */
    public static void main(String[] args) {
        // start the game
        DesktopLauncher.run(new HamsterSimulator());
    }
}
