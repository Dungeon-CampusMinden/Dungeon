package core.network.messages.s2c;

import java.util.Objects;
import java.util.Set;

/**
 * Snapshot state for the current level.
 *
 * @param doorStates all door tiles with their open/closed state
 */
public record LevelState(Set<DoorTileState> doorStates) {

  /**
   * Creates a new immutable level state snapshot.
   *
   * @param doorStates all door tiles with their open/closed state
   */
  public LevelState {
    doorStates = Set.copyOf(Objects.requireNonNull(doorStates, "doorStates"));
  }
}
