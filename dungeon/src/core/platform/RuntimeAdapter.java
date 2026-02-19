package core.platform;

/**
 * Backend-specific runtime access (application lifecycle + graphics context presence).
 *
 * <p>Kept minimal on purpose: only what core.Game currently needs.
 */
public interface RuntimeAdapter {
  /** Request a graceful application exit if supported. */
  void requestExit();

  /** @return true if no graphical context is available (headless). */
  boolean isHeadless();
}
