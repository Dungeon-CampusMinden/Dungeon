package wizard.runner.room;

/** Signals that no seeded attempt arranged every room of the project without collisions. */
public final class UnplaceableLevelException extends RuntimeException {
  /** Creates the exception. */
  UnplaceableLevelException() {
    super(
        "no collision-free level found within " + LevelGenerator.MAX_ATTEMPTS + " seeded attempts");
  }
}
