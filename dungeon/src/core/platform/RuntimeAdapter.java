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

  /**
   * @return true if the current host provides a usable libGDX rendering context
   *     (Gdx.graphics/Gdx.gl/Gdx.files available). This is intentionally NOT the same
   *     as "not headless" because other hosts (e.g. LITIENGINE) can have a window
   *     but still no libGDX context.
   */
  default boolean supportsGdxRendering() {
    return false;
  }
}
