package rooms.morphco.level;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import rooms.morphco.petrinet.MorphCoMilestone;
import rooms.morphco.petrinet.MorphCoProgressNet;
import rooms.morphco.puzzles.CircumfixPuzzle;

/**
 * Server-side room state that is not a milestone of the progress net.
 *
 * <p>Flags record observations (an inspected machine, a read poster, ...). They drive the quest log
 * and the hidden achievements and are stored in the savegame together with the milestones.
 */
public final class MorphCoState {

  /** The arrival conversation has been shown. */
  public static final String ARRIVAL = "arrival";

  /** The card has opened the door of the control room. */
  public static final String CARD_USED = "card-used";

  /** The control room has been entered. */
  public static final String CONTROL_ENTERED = "control-entered";

  /** The hall F-04 has been entered. */
  public static final String F04_ENTERED = "f04-entered";

  /** The inflection machine has been inspected after reading the handbook. */
  public static final String MACHINE_INSPECTED = "machine-inspected";

  /** The faulty word forms on the conveyor have been examined. */
  public static final String CONVEYOR_SEEN = "conveyor-seen";

  /** The defect report in the waste-paper basket has been read. */
  public static final String COMPLAINT_SEEN = "complaint-seen";

  /** The first part of "Morphemtypen und wo sie zu finden sind" has been read. */
  public static final String MORPH1_READ = "morph1-read";

  /** The second part of "Morphemtypen und wo sie zu finden sind" has been read. */
  public static final String MORPH2_READ = "morph2-read";

  /** The display of the affix sorter has been read. */
  public static final String A12_DISPLAY_SEEN = "a12-display-seen";

  /** The question at the hatch lock has been shown. */
  public static final String LOCK_SEEN = "lock-seen";

  /** The maintenance hatch has been opened and the sorter inspected. */
  public static final String HATCH_OPENED = "hatch-opened";

  /** The broken circumfix parts have been taken from the pile. */
  public static final String PILE_TAKEN = "pile-taken";

  /** The phone message has been played. */
  public static final String MESSAGE_HEARD = "message-heard";

  /** The security service has been called. */
  public static final String CALLED_SECURITY = "called-security";

  /** Rosi has been called. */
  public static final String CALLED_ROSI = "called-rosi";

  /** Prefix of flags that record an inspected plant. */
  public static final String PLANT_PREFIX = "plant:";

  /** Prefix of flags that record a read poster. */
  public static final String POSTER_PREFIX = "poster:";

  /** Prefix of flags that record an opened book. */
  public static final String BOOK_PREFIX = "book:";

  /** Potted plants in the factory. */
  public static final int PLANTS = 3;

  /** Posters in the factory. */
  public static final int POSTERS = 7;

  /** Books on the shelves of the factory. */
  public static final int BOOKS = 8;

  private final Set<String> flags = new HashSet<>();
  private final EnumSet<CircumfixPuzzle.Circumfix> assembled =
      EnumSet.noneOf(CircumfixPuzzle.Circumfix.class);
  private int leverPulls;
  private int affixAttempts;

  /**
   * Checks a milestone in the progress net.
   *
   * @param milestone milestone to check
   * @return whether the milestone is complete
   */
  public boolean done(MorphCoMilestone milestone) {
    return MorphCoProgressNet.isComplete(milestone);
  }

  /**
   * Checks a flag.
   *
   * @param flag flag name
   * @return whether the flag is set
   */
  public boolean has(String flag) {
    return flags.contains(flag);
  }

  /**
   * Sets a flag.
   *
   * @param flag flag name
   * @return whether the flag was newly set
   */
  public boolean set(String flag) {
    return flags.add(flag);
  }

  /**
   * Counts the flags with a prefix.
   *
   * @param prefix flag prefix such as {@link #PLANT_PREFIX}
   * @return number of matching flags
   */
  public long count(String prefix) {
    return flags.stream().filter(flag -> flag.startsWith(prefix)).count();
  }

  /**
   * Returns a copy of all flags.
   *
   * @return set flags
   */
  public Set<String> flags() {
    return Set.copyOf(flags);
  }

  /**
   * Returns the assembled circumfixes.
   *
   * @return copy of the assembled circumfixes
   */
  public Set<CircumfixPuzzle.Circumfix> assembled() {
    return assembled.isEmpty()
        ? EnumSet.noneOf(CircumfixPuzzle.Circumfix.class)
        : EnumSet.copyOf(assembled);
  }

  /**
   * Records an assembled circumfix.
   *
   * @param circumfix assembled circumfix
   * @return whether it was assembled for the first time
   */
  public boolean assemble(CircumfixPuzzle.Circumfix circumfix) {
    return assembled.add(circumfix);
  }

  /**
   * Counts a pull on the stuck lever.
   *
   * @return pulls in this run
   */
  public int pullLever() {
    return ++leverPulls;
  }

  /**
   * Returns the pulls on the stuck lever.
   *
   * @return pulls in this run
   */
  public int leverPulls() {
    return leverPulls;
  }

  /**
   * Counts a checked answer of the affix step.
   *
   * @return attempts in this run
   */
  public int affixAttempt() {
    return ++affixAttempts;
  }

  /**
   * Returns the checked answers of the affix step.
   *
   * @return attempts in this run
   */
  public int affixAttempts() {
    return affixAttempts;
  }

  /**
   * Replaces the state with saved values.
   *
   * @param savedFlags saved flags
   * @param savedAssembled saved circumfixes
   * @param savedLeverPulls saved lever pulls
   * @param savedAffixAttempts saved affix attempts
   */
  public void restore(
      Set<String> savedFlags,
      Set<CircumfixPuzzle.Circumfix> savedAssembled,
      int savedLeverPulls,
      int savedAffixAttempts) {
    flags.clear();
    flags.addAll(savedFlags);
    assembled.clear();
    assembled.addAll(savedAssembled);
    leverPulls = Math.max(0, savedLeverPulls);
    affixAttempts = Math.max(0, savedAffixAttempts);
  }
}
