package engine.level.utils;

/**
 * Represents the different available visual design themes for a dungeon environment. These labels
 * can be used to define the aesthetic style of the dungeon.
 */
public enum DesignLabel {
  /**
   * The default dungeon design: brown brick walls and stone floors with random cracks, plus wall
   * decoration in levels whose atmosphere asks for it.
   */
  DEFAULT,

  /** The former default design with thin grey stone walls and a purple tiled floor. */
  OLD_DEFAULT,

  /** A fiery theme featuring lava, heat, and volcanic visuals. */
  FIRE,

  /** A lush forest theme with greenery, trees, and natural elements. */
  FOREST,

  /** An icy theme with snow, frost, and cold, wintry visuals. */
  ICE,

  /** An ancient temple theme with mystical or sacred architectural elements. */
  TEMPLE,

  /** A dark, ominous theme with shadows and eerie atmosphere. */
  DARK,

  /** A very colorful, whimsical theme featuring a rainbow palette. */
  RAINBOW,

  /** A damaged AI research facility with cool metal surfaces and restrained cyan accents. */
  SYSTEM_RECOVERY;
}
