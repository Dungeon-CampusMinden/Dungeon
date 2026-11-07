package rooms.morphco.util;

import engine.Entity;
import engine.Game;
import feature.achievements.AchievementManager;
import java.util.Locale;

/** Achievements of the Morph & Co. room; definitions live in {@code achievements/morphco.json}. */
public enum MorphCoAchievements {
  /** The alarm has been switched off. */
  ALARM_OFF,
  /** All inflectable words lie in the right crates. */
  WORD_CLASSES,
  /** The affix sorter has been repaired. */
  MARIE_KONDO,
  /** All three circumfixes have been assembled. */
  COMBINATORIAL,
  /** The inflection machine has been repaired. */
  FLEXION,
  /** The production has been restarted. */
  RESTART,
  /** The affixes were cut and sorted correctly at the first attempt. */
  FIRST_TRY,
  /** Every potted plant has been inspected. */
  PLANTS,
  /** The stuck lever has been pulled five times. */
  LEVER,
  /** Both phone numbers have been called. */
  NOBODY_HOME,
  /** Every poster has been read. */
  POSTERS,
  /** Every book on the shelves has been opened. */
  BOOKWORM;

  /** Definition file inside the assets. */
  public static final String DEFINITION_PATH = "achievements/morphco.json";

  /** Runtime file with the locally recorded unlocks. */
  public static final String STATUS_PATH = "morph-co-achievement-unlock.json";

  /** Registers the definitions for the menu, the server and the clients. */
  public static void register() {
    AchievementManager.registerAchievements(DEFINITION_PATH, STATUS_PATH);
  }

  /** Unlocks the achievement for all players from authoritative room logic. */
  public void unlock() {
    if (!Game.isMultiplayerClient()) AchievementManager.instance().pop(id());
  }

  /**
   * Unlocks the achievement for an action of a player; the definition decides the recipients.
   *
   * @param player acting player
   */
  public void unlock(Entity player) {
    if (!Game.isMultiplayerClient()) AchievementManager.instance().popFor(player, id());
  }

  /**
   * Returns the definition and localization key.
   *
   * @return namespaced achievement ID
   */
  public String id() {
    return "morphco_" + name().toLowerCase(Locale.ROOT);
  }
}
