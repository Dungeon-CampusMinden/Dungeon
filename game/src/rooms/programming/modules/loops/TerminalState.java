package rooms.programming.modules.loops;

import java.util.List;

/**
 * Immutable room state carried in existing snapshot metadata.
 *
 * @param collectedRunes available canonical rune IDs
 * @param checkpoint zero-based current checkpoint, or five after completion
 * @param golemId entity observed by the monitor
 * @param cellX current grid column
 * @param cellY current grid row
 * @param busy whether an attempt or return is running
 * @param observationReady whether the golem has entered the remote maze
 * @param activeRune inserted rune ID, retained after execution until removed or replaced
 * @param status current runtime feedback
 */
public record TerminalState(
    List<String> collectedRunes,
    int checkpoint,
    int golemId,
    int cellX,
    int cellY,
    boolean busy,
    boolean observationReady,
    String activeRune,
    String status) {
  /** Copies the inventory so snapshots cannot modify shared state. */
  public TerminalState {
    collectedRunes = List.copyOf(collectedRunes);
  }

  /**
   * @return whether every checkpoint has been completed
   */
  public boolean finished() {
    return checkpoint >= LoopMaze.checkpoints().size();
  }
}
