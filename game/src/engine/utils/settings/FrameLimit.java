package engine.utils.settings;

/**
 * The frame rates a player can limit rendering to. The limit only changes how often the game draws;
 * the world ticks at its own fixed rate.
 */
public enum FrameLimit {
  /** 30 frames per second. */
  FPS_30(30),
  /** 60 frames per second. */
  FPS_60(60),
  /** 120 frames per second. */
  FPS_120(120),
  /** 144 frames per second. */
  FPS_144(144),
  /** As many frames as the device and its display allow. */
  UNLIMITED(0);

  /** The limit every game starts with. */
  public static final FrameLimit DEFAULT = FPS_60;

  private final int fps;

  FrameLimit(int fps) {
    this.fps = fps;
  }

  /**
   * Gets the limit as libGDX expects it.
   *
   * @return the frames per second, or 0 for no limit
   */
  public int fps() {
    return fps;
  }
}
