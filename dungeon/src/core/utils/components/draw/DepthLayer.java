package core.utils.components.draw;

import core.components.DrawComponent;

/**
 * Defines the rendering order of drawable components by assigning a depth value to each layer.
 *
 * <p>The lower the {@code depth} value, the earlier it is rendered (further in the background).
 * Higher values are rendered later (in the foreground or UI).
 *
 * <p>This class only serves as a place to store the default depth values. In the {@link
 * DrawComponent}, the depth can be assigned any integer value.
 */
public enum DepthLayer {
  /** Background layer behind the level. If we ever want to add a background, parallax, etc. */
  Background(-9999),

  /** Base level geometry (e.g. floors, walls, doors). */
  Level(-1000),

  /** Decorative elements drawn behind the main play area. */
  BackgroundDeco(-100),

  /** For entities that other entities can stand on (e.g. pressure plates) */
  Ground(-10),

  /** Default layer for most objects. */
  Normal(0),

  /** Decorative elements drawn in front of normal gameplay. */
  ForegroundDeco(50),

  /** Player characters are rendered on this layer. */
  Player(100),

  /**
   * Entities that should always appear above the player (e.g. overhead effects, flying enemies).
   */
  AbovePlayer(1000),

  /** User interface elements, always drawn on top. */
  UI(9999);

  private final int depth;

  /**
   * Creates a new {@link DepthLayer} with a given depth value.
   *
   * @param depth numeric depth value; lower values are rendered behind higher ones
   */
  DepthLayer(int depth) {
    this.depth = depth;
  }

  /**
   * Returns the numeric depth value of this layer.
   *
   * @return depth value used for rendering order
   */
  public int depth() {
    return depth;
  }
}
