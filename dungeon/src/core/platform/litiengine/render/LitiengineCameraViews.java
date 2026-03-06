package core.platform.litiengine.render;

public final class LitiengineCameraViews {
  private LitiengineCameraViews() {}

  public record View(double offsetX, double offsetY, int levelHeight, int tilePx) {}

  private static volatile View CURRENT = new View(0, 0, 0, 32);

  public static View get() {
    return CURRENT;
  }

  public static void set(double offsetX, double offsetY, int levelHeight, int tilePx) {
    CURRENT = new View(offsetX, offsetY, levelHeight, tilePx);
  }
}
