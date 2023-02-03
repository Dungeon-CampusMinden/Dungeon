package hamster.elements;

import basiselements.DungeonElement;
import basiselements.ThreadedDungeonElement;
import controller.Game;
import hamster.HamsterSimulator;
import java.util.List;
import tools.Point;

/**
 * Example implementation of a threaded character, used in the Hamster Simulator
 *
 * @author Maxim Fruendt
 */
public class Hamster extends ThreadedDungeonElement {

    /** Fixed movement speed of the character */
    private static final float MOVEMENT_SPEED = 0.2f;
    /** Range for checking collisions with other characters */
    private static final float COLLISION_RANGE = 1f;
    /** Range for picking up objects */
    private static final float PICKUP_RANGE = 1.5f;

    /** List of directions in which the character can walk */
    private enum WalkingDirections {
        LEFT,
        RIGHT,
        UP,
        DOWN
    }

    /** Reference to the game used for collision detection */
    private HamsterSimulator game;
    /** The direction in which the character walked last, used for movement calculation */
    protected WalkingDirections lastWalkedDirection = WalkingDirections.RIGHT;

    /** Update the logic of this character */
    @Override
    protected void update() {
        WalkingDirections direction = lastWalkedDirection;
        for (int i = 0; i < WalkingDirections.values().length; i++) {
            if (i != 0) {
                direction = rotateClockwise(direction);
            }
            if (move(direction)) {
                lastWalkedDirection = direction;
                break;
            }
        }
        if (game != null) {
            List<DungeonElement> collidingEntities =
                    game.getCollidingEntitiesForEntity(
                            getFakeElement(), getPosition(), PICKUP_RANGE);
            for (DungeonElement entity : collidingEntities) {
                if (entity instanceof Loot) {
                    game.removeEntity(entity);
                }
            }
        }
    }

    /**
     * Get the path to the current texture
     *
     * @return Texture path
     */
    @Override
    public String getTexturePath() {
        return "character/knight/knight_m_idle_anim_f0.png";
    }

    /**
     * Set the game to which tis element belongs to
     *
     * @param game New game of this object
     */
    @Override
    public void setGame(Game game) {
        if (game instanceof HamsterSimulator) {
            this.game = (HamsterSimulator) game;
        }
    }

    /**
     * Check if the desired direction is reachable and move the character if true
     *
     * @param direction (Absolute) Direction that should be moved to
     * @return True if moving was successful, else false
     */
    protected boolean move(WalkingDirections direction) {
        if (game == null) {
            return false;
        }

        Point newPosition = new Point(position);

        // Get the new position
        switch (direction) {
            case LEFT -> newPosition.x -= MOVEMENT_SPEED;
            case RIGHT -> newPosition.x += MOVEMENT_SPEED;
            case DOWN -> newPosition.y -= MOVEMENT_SPEED;
            case UP -> newPosition.y += MOVEMENT_SPEED;
        }

        if (game.isPosAccessibleForEntity(getFakeElement(), newPosition, COLLISION_RANGE)) {
            position = newPosition;
            return true;
        }
        return false;
    }

    /**
     * Turn the current direction one step clockwise
     *
     * @param direction Current direction
     * @return New direction
     */
    private WalkingDirections rotateClockwise(WalkingDirections direction) {
        switch (direction) {
            case DOWN -> {
                return WalkingDirections.LEFT;
            }
            case UP -> {
                return WalkingDirections.RIGHT;
            }
            case RIGHT -> {
                return WalkingDirections.DOWN;
            }
            case LEFT -> {
                return WalkingDirections.UP;
            }
            default -> {
                return lastWalkedDirection;
            }
        }
    }
}
