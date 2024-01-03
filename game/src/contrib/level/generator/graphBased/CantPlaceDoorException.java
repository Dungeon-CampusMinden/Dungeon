package contrib.level.generator.graphBased;

import contrib.level.generator.graphBased.levelGraph.Direction;
import core.level.utils.LevelElement;
import core.level.utils.LevelSize;

/**
 * Exception to throw if a door cannot be place in a room.
 *
 * @see RoomGenerator
 */
public final class CantPlaceDoorException extends RuntimeException {

  /**
   * Create a new CantPlaceDoorException.
   *
   * @param layout Layout of the room.
   * @param direction where should the door be placed.
   * @param size size of the level.
   */
  public CantPlaceDoorException(
      final LevelElement[][] layout, final Direction direction, final LevelSize size) {
    super(
        "Cant place door at "
            + direction
            + " in room with the layout "
            + RoomGenerator.layoutToString(layout, size));
  }
}
