package core.platform;

/**
 * Minimal window API that the core can depend on without referencing a concrete engine.
 *
 * <p>This is intentionally tiny for the first step: only what core.Game currently needs.
 */
public interface WindowAdapter {
  int width();
  int height();

  /** Update window title if supported by the active backend. */
  void setTitle(String title);
}
