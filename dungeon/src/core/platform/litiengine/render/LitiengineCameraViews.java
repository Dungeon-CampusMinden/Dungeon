package core.platform.litiengine.render;

/** Shared camera view data for the LITIENGINE backend. */
public final class LitiengineCameraViews {
  private static final View DEFAULT_VIEW = new View(0, 0, 0, 32);

  private LitiengineCameraViews() {}

  public record View(double offsetX, double offsetY, int levelHeight, int tilePx) {}

  private static volatile View CURRENT = DEFAULT_VIEW;

  public static View get() {
    return CURRENT;
  }

  public static void set(double offsetX, double offsetY, int levelHeight, int tilePx) {
    CURRENT = new View(offsetX, offsetY, levelHeight, tilePx);
  }

  /** Resets the shared camera view to its default state. */
  public static void reset() {
    CURRENT = DEFAULT_VIEW;
  }
}
