package basiselements;

import controller.Game;
import tools.Point;

/**
 * An object that can be spawned with the <code>LevelEditor</code>.
 *
 * @author Maxim Fruendt
 */
public interface ISpawnable {
    /**
     * Set the exact position in the dungeon of this instance
     *
     * @param position New position of this object
     */
    void setPosition(Point position);

    /**
     * Set the game that this belongs in
     *
     * @param game New game of this object
     */
    void setGame(Game game);
}
