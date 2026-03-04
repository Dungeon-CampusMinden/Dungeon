package core.network.messages.s2c;

import core.level.utils.Coordinate;
import java.util.Objects;

/**
 * Snapshot state for one door tile.
 *
 * @param coordinate tile coordinate of the door
 * @param open true if the door is currently open
 */
public record DoorTileState(Coordinate coordinate, boolean open) {

  /**
   * Creates a new immutable door tile snapshot state.
   *
   * @param coordinate tile coordinate of the door
   * @param open true if the door is currently open
   */
  public DoorTileState {
    coordinate = Objects.requireNonNull(coordinate, "coordinate");
  }
}
