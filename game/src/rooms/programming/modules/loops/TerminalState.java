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
 * @param facing cardinal heading name
 * @param busy whether an attempt or return is running
 * @param observationReady whether the golem has entered the remote maze
 * @param activeRune executing rune ID, or empty when idle
 * @param status current runtime feedback
 * @param completed number of completed checkpoints
 * @param finished whether the final checkpoint is complete
 */
public record TerminalState(
    List<String> collectedRunes,
    int checkpoint,
    int golemId,
    int cellX,
    int cellY,
    String facing,
    boolean busy,
    boolean observationReady,
    String activeRune,
    String status,
    int completed,
    boolean finished) {
  /** Copies the inventory so snapshots cannot modify shared state. */
  public TerminalState {
    collectedRunes = List.copyOf(collectedRunes);
  }
}
