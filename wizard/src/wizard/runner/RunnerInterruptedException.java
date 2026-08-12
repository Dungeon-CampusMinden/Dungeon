package wizard.runner;

/** Signals that a blocking Runner runtime was interrupted after its resources were cleaned up. */
public final class RunnerInterruptedException extends RuntimeException {
  /**
   * Creates the stable interruption signal while retaining the original interruption.
   *
   * @param cause interrupted blocking operation
   */
  public RunnerInterruptedException(final InterruptedException cause) {
    super(cause);
  }
}
