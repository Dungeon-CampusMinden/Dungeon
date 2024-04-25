package core.level.utils;

import java.util.List;
import java.util.Random;

/** Specifies how large a level should be. Exact definition is interpreted by the generator. */
public enum LevelSize {
  /** WTF? . */
  SMALL,
  /** WTF? . */
  MEDIUM,
  /** WTF? . */
  LARGE;

  private static final List<LevelSize> VALUES = List.of(values());
  private static final int SIZE = VALUES.size();
  private static final Random RANDOM = new Random();

  /**
   * Get a random level size.
   *
   * @return A random level size.
   */
  public static LevelSize randomSize() {
    return VALUES.get(RANDOM.nextInt(SIZE));
  }
}
