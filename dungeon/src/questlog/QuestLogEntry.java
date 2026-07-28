package questlog;

import core.Game;

/**
 * Represents a single entry in the quest log.
 *
 * <p>An entry stores the displayed quest text together with metadata about the game tick at which
 * it was created, who created it, and whether its visibility is restricted to the creator.
 *
 * <p>Normal game code should use {@link #QuestLogEntry(String, boolean)} or {@link
 * #QuestLogEntry(String, String, boolean)}. These constructors set {@link #timestamp()} to {@link
 * Game#currentTick()} automatically, so callers do not need to provide the timestamp manually. The
 * full record constructor still exists because Java records always expose a canonical constructor
 * for all record components.
 *
 * @param text the text shown for this quest log entry
 * @param timestamp the game tick at which this entry was created or recorded; normally assigned by
 *     the timestamp-free constructors
 * @param owner the identifier of the owner or creator of this entry
 * @param onlyForCreator true if this entry should only be visible to its creator, false if it may
 *     be visible to others
 */
public record QuestLogEntry(String text, int timestamp, String owner, boolean onlyForCreator) {

  /** Default owner used for quest log entries created by the system. */
  public static final String DEFAULT_OWNER = "System";

  /**
   * Creates a quest log entry with the default system owner and the current game tick.
   *
   * @param text the text shown for this quest log entry
   * @param onlyForCreator true if this entry should only be visible to its creator, false if it may
   *     be visible to others
   */
  public QuestLogEntry(String text, boolean onlyForCreator) {
    this(text, DEFAULT_OWNER, onlyForCreator);
  }

  /**
   * Creates a quest log entry with a specific owner and the current game tick.
   *
   * @param text the text shown for this quest log entry
   * @param owner the identifier of the owner or creator of this entry
   * @param onlyForCreator true if this entry should only be visible to its creator, false if it may
   *     be visible to others
   */
  public QuestLogEntry(String text, String owner, boolean onlyForCreator) {
    this(text, Game.currentTick(), owner, onlyForCreator);
  }
}
