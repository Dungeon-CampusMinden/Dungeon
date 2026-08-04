package contrib.achivements;

import java.util.Locale;

/** Defines who receives an achievement when it is triggered. */
public enum AchievementUnlockScope {
  /** Unlocks the achievement globally for every player. */
  ALL_PLAYERS,

  /** Unlocks the achievement only for the player entity passed to the trigger. */
  TRIGGERING_PLAYER;

  /**
   * Parses an achievement unlock scope from JSON values.
   *
   * @param unlockForAll optional boolean field
   * @param scope optional string field
   * @return parsed scope, defaulting to {@link #ALL_PLAYERS}
   */
  static AchievementUnlockScope fromJson(Object unlockForAll, Object scope) {
    if (unlockForAll instanceof Boolean value) {
      return value ? ALL_PLAYERS : TRIGGERING_PLAYER;
    }
    if (unlockForAll instanceof String text && !text.isBlank()) {
      return Boolean.parseBoolean(text) ? ALL_PLAYERS : TRIGGERING_PLAYER;
    }
    if (scope instanceof String text && !text.isBlank()) {
      return switch (text.trim().toLowerCase(Locale.ROOT)) {
        case "all", "all_players", "allplayers", "global" -> ALL_PLAYERS;
        case "player", "triggering_player", "triggeringplayer", "local" -> TRIGGERING_PLAYER;
        default -> ALL_PLAYERS;
      };
    }
    return ALL_PLAYERS;
  }

  /**
   * Checks if this scope unlocks for all players.
   *
   * @return true for global achievements
   */
  public boolean unlocksForAll() {
    return this == ALL_PLAYERS;
  }
}
