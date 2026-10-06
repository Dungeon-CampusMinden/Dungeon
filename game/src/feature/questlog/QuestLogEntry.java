package feature.questlog;

import engine.Game;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a single entry in the quest log.
 *
 * <p>An entry stores the displayed quest text together with metadata about the game tick at which
 * it was created, who created it, and whether its visibility is restricted to the creator. An
 * explicit title makes the entry collapsible, with its text initially hidden. Plain text retains
 * its paragraph formatting without changing how the entry behaves.
 *
 * <p>Normal game code should use {@link #QuestLogEntry(String, boolean)} or {@link
 * #QuestLogEntry(String, String, boolean)}. These constructors set {@link #timestamp()} to {@link
 * Game#currentTick()} automatically, so callers do not need to provide the timestamp manually.
 *
 * @param text the text shown for this quest log entry
 * @param timestamp the game tick at which this entry was created or recorded; normally assigned by
 *     the timestamp-free constructors
 * @param userCreated true if this entry was created by a player's direct note action, false if it
 *     was created by game logic
 * @param owner the identifier of the owner or creator of this entry
 * @param onlyForCreator true if this entry should only be visible to its creator, false if it may
 *     be visible to others
 * @param title optional title of a collapsible entry
 */
public record QuestLogEntry(
    String text,
    int timestamp,
    boolean userCreated,
    String owner,
    boolean onlyForCreator,
    Optional<String> title) {

  /** Validates an explicitly supplied title. */
  public QuestLogEntry {
    Objects.requireNonNull(title, "title");
    title = title.map(String::strip);
    if (title.filter(value -> value.isBlank() || value.lines().count() != 1).isPresent()) {
      throw new IllegalArgumentException("A collapsible entry needs a nonblank single-line title");
    }
  }

  /** Default owner used for quest log entries created by the system. */
  public static final String DEFAULT_OWNER = "System";

  /**
   * Creates a plain entry, including entries restored from stored state.
   *
   * @param text entry text
   * @param timestamp creation tick
   * @param userCreated whether a player wrote the entry
   * @param owner creator of the entry
   * @param onlyForCreator whether the entry is private
   */
  public QuestLogEntry(
      String text, int timestamp, boolean userCreated, String owner, boolean onlyForCreator) {
    this(text, timestamp, userCreated, owner, onlyForCreator, Optional.empty());
  }

  /**
   * Creates a titled system entry whose details start collapsed.
   *
   * @param title title visible while collapsed
   * @param text details shown when expanded
   * @return a public system entry at the current game tick
   */
  public static QuestLogEntry collapsible(String title, String text) {
    return new QuestLogEntry(
        text, Game.currentTick(), false, DEFAULT_OWNER, false, Optional.of(title));
  }

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
    this(text, Game.currentTick(), false, owner, onlyForCreator);
  }
}
