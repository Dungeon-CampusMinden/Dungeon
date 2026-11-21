package rooms.morphco.petrinet;

import java.util.EnumSet;
import java.util.Set;

/**
 * Irreversible progress of the Morph & Co. room.
 *
 * <p>Once the wired fuse box has opened the door to the corridor, the two broken machines can be
 * repaired in any order, whether or not the computer has been read. The inflection machine F-04
 * additionally needs a working affix sorter A-12, because only A-12 delivers the correct affixes.
 * The restart closes both branches.
 */
public enum MorphCoMilestone {
  /** The alarm at the console has been switched off. */
  ALARM_OFF,
  /** The production control system has been read and names the broken machines. */
  CAUSES_FOUND,
  /** The handbook of the inflection machine has been read. */
  HANDBOOK_READ,
  /** Only inflectable words lie in the crates next to the inflection machine. */
  WORD_CLASSES_SORTED,
  /** The maintenance hatch of A-12 has been unlocked. */
  HATCH_UNLOCKED,
  /** All affixes have been cut out and sorted. */
  AFFIXES_SORTED,
  /**
   * The affixes lie in the word-formation and inflection shafts, the assembled circumfixes in the
   * magazine and the lever has restarted A-12; it runs again.
   */
  A12_REPAIRED,
  /** The inflection machine works again. */
  F04_REPAIRED,
  /** The production has been restarted. */
  PRODUCTION_RESTARTED;

  /**
   * Returns the milestones that must be complete before this one can fire.
   *
   * @return prerequisites of this milestone
   */
  public Set<MorphCoMilestone> prerequisites() {
    return switch (this) {
      case ALARM_OFF, HANDBOOK_READ, HATCH_UNLOCKED -> EnumSet.noneOf(MorphCoMilestone.class);
      case CAUSES_FOUND -> EnumSet.of(ALARM_OFF);
      case WORD_CLASSES_SORTED -> EnumSet.of(HANDBOOK_READ);
      case AFFIXES_SORTED -> EnumSet.of(HATCH_UNLOCKED);
      case A12_REPAIRED -> EnumSet.of(AFFIXES_SORTED);
      case F04_REPAIRED -> EnumSet.of(WORD_CLASSES_SORTED, A12_REPAIRED);
      case PRODUCTION_RESTARTED -> EnumSet.of(F04_REPAIRED, A12_REPAIRED);
    };
  }
}
