package rooms.programming.state;

import java.util.Optional;

/** Ordered phases of the Programming 1 escape room. */
public enum ProgrammingPhase {
  VARIABLES,
  LOOPS,
  METHODS,
  CONDITIONS,
  FINALE,
  COMPLETE;

  private static final ProgrammingPhase[] ORDER = values();

  /** Returns the next phase, or an empty value after the room is complete. */
  public Optional<ProgrammingPhase> next() {
    int nextIndex = ordinal() + 1;
    return nextIndex < ORDER.length ? Optional.of(ORDER[nextIndex]) : Optional.empty();
  }

  /** Reports whether the given phase has already been completed. */
  public boolean completed(ProgrammingPhase phase) {
    return ordinal() > phase.ordinal();
  }
}
