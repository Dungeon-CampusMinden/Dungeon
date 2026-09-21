package rooms.programming.modules.decisions;

/** The six programs and the persistent values carried through the labyrinth. */
public final class DecisionMaze {
  /** The two executable leaf choices in each rune. */
  public enum Side {
    LEFT,
    RIGHT
  }

  /**
   * Values carried through both successful and failed routes.
   *
   * @param kraft current strength
   * @param energie current energy
   * @param temperatur current temperature
   */
  public record Values(int kraft, int energie, int temperatur) {
    /**
     * @param k strength change
     * @param e energy change
     * @param t temperature change
     * @return updated values without resetting earlier events
     */
    public Values plus(int k, int e, int t) {
      return new Values(kraft + k, energie + e, temperatur + t);
    }

    /**
     * @return all three values with unambiguous labels
     */
    public String label() {
      return "KRAFT " + kraft + "     ENERGIE " + energie + "     TEMPERATUR " + temperatur;
    }
  }

  public static final String[] TITLES = {
    "Das Tor der Energie",
    "Das Tor der Temperatur",
    "Das Tor der drei Runen",
    "Das Tor der zwei Pfade",
    "Das Tor der verbundenen Kräfte",
    "Das Herzfeuer-Siegel"
  };
  public static final String[] RUNES = {
"""
WENN Energie > 50:
    WENN Kraft >= 40:
        RECHTS
    SONST:
        LINKS
SONST:
    LINKS
""",
"""
WENN Energie >= 60:
    WENN Temperatur < 25:
        LINKS
    SONST:
        RECHTS
SONST:
    WENN Kraft >= 40:
        RECHTS
    SONST:
        LINKS
""",
"""
WENN Energie >= 50:
    WENN Temperatur > 20:
        WENN Kraft >= 50:
            RECHTS
        SONST:
            LINKS
    SONST:
        RECHTS
SONST:
    LINKS
""",
"""
WENN Energie >= 50:
    WENN Temperatur > 25:
        WENN Kraft >= 60:
            RECHTS
        SONST:
            LINKS
    SONST:
        WENN Kraft >= 40:
            RECHTS
        SONST:
            LINKS
SONST:
    LINKS
""",
"""
WENN Energie >= 50:
    WENN Temperatur > 25:
        WENN Kraft > 60 UND Energie < 60:
            RECHTS
        SONST:
            LINKS
    SONST:
        RECHTS
SONST:
    LINKS
""",
"""
WENN Energie >= 40:
    WENN Kraft > 60:
        WENN Temperatur > 30 UND Energie < 50:
            WENN Kraft >= 70 ODER Temperatur > 35:
                RECHTS
            SONST:
                LINKS
        SONST:
            LINKS
    SONST:
        LINKS
SONST:
    RECHTS
"""
  };
  public static final String JAVA =
"""
if (energie >= 40) {
    if (kraft > 60) {
        if (temperatur > 30 && energie < 50) {
            if (kraft >= 70 || temperatur > 35) {
                // RECHTS
            } else {
                // LINKS
            }
        } else {
            // LINKS
        }
    } else {
        // LINKS
    }
} else {
    // RECHTS
}
""";

  private DecisionMaze() {}

  /**
   * @param junction zero-based rune index
   * @param v current carried values
   * @return leaf reached by executing the displayed nested program
   */
  public static Side evaluate(int junction, Values v) {
    boolean right =
        switch (junction) {
          case 0 -> v.energie > 50 && v.kraft >= 40;
          case 1 -> v.energie >= 60 ? v.temperatur >= 25 : v.kraft >= 40;
          case 2 -> v.energie >= 50 && (v.temperatur <= 20 || v.kraft >= 50);
          case 3 -> v.energie >= 50 && (v.temperatur > 25 ? v.kraft >= 60 : v.kraft >= 40);
          case 4 -> v.energie >= 50 && (v.temperatur <= 25 || v.kraft > 60 && v.energie < 60);
          case 5 ->
              v.energie < 40
                  || v.kraft > 60
                      && v.temperatur > 30
                      && v.energie < 50
                      && (v.kraft >= 70 || v.temperatur > 35);
          default -> throw new IllegalArgumentException("Unknown junction");
        };
    return right ? Side.RIGHT : Side.LEFT;
  }

  /**
   * @param junction successful junction being left
   * @return fixed changes applied by its onward rune
   */
  public static Values delta(int junction) {
    return switch (junction) {
      case 0 -> new Values(0, -30, 0);
      case 1 -> new Values(0, 30, 0);
      case 2 -> new Values(10, 0, 5);
      case 3 -> new Values(13, -15, 0);
      case 4 -> new Values(0, -13, 4);
      default -> new Values(0, 0, 0);
    };
  }

  /**
   * @param junction successful junction being left
   * @return visible explanation of its value changes
   */
  public static String event(int junction) {
    return switch (junction) {
      case 0 -> "Schwellenrune: ENERGIE -30";
      case 1 -> "Kristallquelle: ENERGIE +30";
      case 2 -> "Kraftquelle: KRAFT +10 · Feuerrune: TEMPERATUR +5";
      case 3 -> "Schmiederune: KRAFT +13 · ENERGIE -15";
      case 4 -> "Herzglut: ENERGIE -13 · TEMPERATUR +4";
      default -> "Das Herzfeuer erwacht.";
    };
  }
}
