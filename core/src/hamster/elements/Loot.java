package hamster.elements;

import basiselements.DungeonElement;
import controller.Game;
import tools.Point;

/**
 * Loot dungeon element for use with the Hamster Simulator
 *
 * @author Maxim Fruendt
 */
public class Loot extends DungeonElement {

    /** Position of this dungeon object */
    private Point position;

    /** Update this dungeon element */
    @Override
    public void update() {}

    /**
     * Get the current position of this dungeon element
     *
     * @return Current position
     */
    @Override
    public Point getPosition() {
        return position;
    }

    /**
     * Set the position of this dungeon element
     *
     * @param position New position of this object
     */
    @Override
    public void setPosition(Point position) {
        this.position = position;
    }

    /**
     * Set the game to which this dungeon element belongs
     *
     * @param game New game of this object
     */
    @Override
    public void setGame(Game game) {}

    /**
     * Get the path of the current texture of this dungeon element
     *
     * @return Path to the currently active texture
     */
    @Override
    public String getTexturePath() {
        return "objects/crate.png";
    }
}
