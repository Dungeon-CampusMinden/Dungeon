package wizard.runner.bootstrap;

/** Bounded failure raised before a Foundation join may create players or signal readiness. */
public final class JoinBootstrapException extends IllegalStateException {
  private static final int MAXIMUM_MESSAGE_LENGTH = 512;

  /**
   * Creates one bounded bootstrap failure without exposing host-side project data.
   *
   * @param message concrete client-facing failure reason
   */
  public JoinBootstrapException(final String message) {
    super(bound(message));
  }

  private static String bound(final String message) {
    if (message == null || message.isBlank()) {
      return "Foundation client bootstrap failed";
    }
    return message.length() <= MAXIMUM_MESSAGE_LENGTH
        ? message
        : message.substring(0, MAXIMUM_MESSAGE_LENGTH);
  }
}
