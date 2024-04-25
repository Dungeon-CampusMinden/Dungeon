package core.level.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Specifies which textures and layouts should be used for the room. */
public enum DesignLabel {
  /** WTF? . */
  DEFAULT(50), // 50% chance
  /** WTF? . */
  FIRE(0), // 0% chance //we have no closed doors texture
  /** WTF? . */
  FOREST(9), // 9% chance
  /** WTF? . */
  ICE(10), // 10% chance
  /** WTF? . */
  TEMPLE(30), // 30% chance
  /** WTF? . */
  DARK(0), // 0% chance //we have no closed doors texture
  /** WTF? . */
  RAINBOW(1); // 1% chance

  private static final Random RANDOM = new Random();
  private static final List<DesignLabel> VALUES = new ArrayList<>();

  static {
    for (DesignLabel l : values()) for (int i = 0; i < l.chance; i++) VALUES.add(l);
  }

  private final int chance;

  /**
   * Create a new label.
   *
   * @param chance chance in % that this label will be returned by the {@link #randomDesign()}
   *     function.
   */
  DesignLabel(int chance) {
    this.chance = chance;
  }

  /**
   * WTF? .
   *
   * @return A random enum-value based on chances
   */
  public static DesignLabel randomDesign() {
    return VALUES.get(RANDOM.nextInt(0, VALUES.size()));
  }
}
