package feature.questlog;

import engine.Entity;
import engine.Game;
import engine.components.PlayerComponent;
import engine.network.messages.s2c.QuestLogStateMessage;
import engine.sound.CoreSounds;
import engine.sound.Sounds;
import java.util.Optional;

/**
 * Utility methods for creating, registering, and writing to the shared quest log.
 *
 * <p>The quest log is stored on a single {@link Entity} with a {@link QuestLogComponent}. There is
 * one shared reference in this utility so gameplay code and UI code do not need to search the ECS
 * world for the quest log entity every time they want to read or write entries.
 *
 * <h2>Server setup</h2>
 *
 * <p>Create the quest log once during server-side level setup and add the returned entity to the
 * game. This entity is the authoritative quest log; {@link QuestLogSyncSystem} synchronizes it to
 * clients.
 *
 * <pre>{@code
 * Game.add(QuestLogUtil.initServerQuestLog());
 * }</pre>
 *
 * <p>After setup, server-side gameplay code can add entries whenever something relevant happens.
 * Use the simple overloads for system-owned entries:
 *
 * <pre>{@code
 * QuestLogUtil.add("Main Quest", "The storage door is open.");
 * QuestLogUtil.add("Secrets", "A hidden keypad was found.", true);
 * }</pre>
 *
 * <p>Use the owner overload when the entry should be attributed to a specific creator:
 *
 * <pre>{@code
 * QuestLogUtil.add("Notes", "Found the office code.", playerName, false);
 * }</pre>
 *
 * <h2>Client synchronization</h2>
 *
 * <p>Clients never create a quest log themselves. In multiplayer, {@link QuestLogSyncSystem} sends
 * every client the quest log it may see whenever the server-side quest log changes, and the client
 * applies it through {@link #applyClientState(QuestLogStateMessage)}. Games need no own
 * synchronization code; client UI code reads the result through {@link #getQuestLogComponent()}.
 *
 * <h2>Client-created notes</h2>
 *
 * <p>In multiplayer games, the server-owned quest log is authoritative. Calling {@link #add(String,
 * String)} directly on a network client only changes the client's local synchronized copy; it does
 * not update the server or other clients and can be overwritten by the next server synchronization.
 *
 * <p>When a client should create an entry, route the action through the server via {@link
 * #addPlayerNote(Entity, String, String, boolean)}:
 *
 * <pre>{@code
 * QuestLogUtil.addPlayerNote(player, "Notes", submittedText, false);
 * }</pre>
 */
public final class QuestLogUtil {

  private static final String QUEST_LOG_ENTITY_NAME = "Questlog";
  private static final String USER_NOTE_TAB = "Notes";

  private static Entity questlog;

  private QuestLogUtil() {}

  /**
   * Creates a new quest log entity and stores it as the shared quest log.
   *
   * <p>This is the normal server-side initialization path. The returned entity contains a fresh
   * {@link QuestLogComponent} and should be added to the game so it can be synchronized to clients.
   * Calling this method again replaces the previously stored quest log entity and discards the old
   * shared reference.
   *
   * <p>Use this during level setup:
   *
   * <pre>{@code
   * Game.add(QuestLogUtil.initServerQuestLog());
   * }</pre>
   *
   * @return the created quest log entity
   */
  public static Entity initServerQuestLog() {
    questlog = new Entity(QUEST_LOG_ENTITY_NAME);
    questlog.add(new QuestLogComponent());
    return questlog;
  }

  /**
   * Returns the current shared quest log entity.
   *
   * <p>On the server this is the entity created by {@link #initServerQuestLog()}. On a network
   * client it is a local entity holding the state applied by {@link
   * #applyClientState(QuestLogStateMessage)}.
   *
   * @return an {@link Optional} containing the quest log entity, or {@link Optional#empty()} if the
   *     quest log was not initialized
   */
  public static Optional<Entity> getQuestLog() {
    return Optional.ofNullable(questlog);
  }

  /**
   * Replaces the client-side quest log with state received from the server.
   *
   * <p>Called by the network layer for every {@link QuestLogStateMessage}. An unavailable state
   * removes the client-side quest log.
   *
   * @param state quest log state visible to this client
   */
  public static void applyClientState(QuestLogStateMessage state) {
    if (!state.available()) {
      questlog = null;
      return;
    }

    QuestLogComponent component = new QuestLogComponent();
    for (QuestLogStateMessage.Entry entry : state.entries()) {
      component.add(
          entry.tab(),
          new QuestLogEntry(
              entry.text(),
              entry.timestamp(),
              entry.userCreated(),
              entry.owner(),
              entry.onlyForCreator(),
              entry.title()));
    }
    if (questlog == null) {
      questlog = Entity.createLocalEntity(QUEST_LOG_ENTITY_NAME);
    }
    questlog.add(component);
  }

  /**
   * Returns the component that stores the quest log entries on the shared quest log entity.
   *
   * @return an {@link Optional} containing the quest log component, or {@link Optional#empty()} if
   *     the quest log was not initialized
   */
  public static Optional<QuestLogComponent> getQuestLogComponent() {
    return getQuestLog().flatMap(entity -> entity.fetch(QuestLogComponent.class));
  }

  /**
   * Returns whether the shared quest log is ready to be used.
   *
   * <p>The quest log is considered initialized when a shared entity exists and still contains a
   * {@link QuestLogComponent}. This can happen through {@link #initServerQuestLog()} on the server
   * or through {@link #applyClientState(QuestLogStateMessage)} on a client. UI code can use this
   * check to hide quest log controls until the current game or level actually provides a quest log.
   *
   * @return {@code true} if quest log entries can be read and written, {@code false} otherwise
   */
  public static boolean isInitialized() {
    return getQuestLogComponent().isPresent();
  }

  /**
   * Adds an existing quest log entry to the given tab.
   *
   * <p>Use this overload when the caller already created a {@link QuestLogEntry}, for example when
   * restoring saved quest log data, applying synchronized state, or when a specific timestamp is
   * needed.
   *
   * <p>In multiplayer games, call this on the server for authoritative quest log changes. A network
   * client can call it only for temporary local UI state; such changes are not sent back to the
   * server.
   *
   * @param tab the tab to add the entry to
   * @param entry the entry to add
   * @return {@code true} if the entry was added, {@code false} if the quest log was not initialized
   */
  public static boolean add(String tab, QuestLogEntry entry) {
    boolean added =
        getQuestLogComponent().map(component -> component.add(tab, entry)).orElse(false);
    if (added) {
      Sounds.play(CoreSounds.INTERFACE_QUESTLOG_ENTRY_CREATED);
    }
    return added;
  }

  /**
   * Adds a system-owned quest log entry to the given tab.
   *
   * <p>The created entry uses {@link QuestLogEntry#DEFAULT_OWNER} as owner and stores the current
   * game tick as its timestamp. Use this for entries created by game logic that should not be
   * attributed to a player.
   *
   * @param tab the tab to add the entry to
   * @param text the text shown for the quest log entry
   * @param onlyForCreator true if the entry should only be visible to its creator, false otherwise
   * @return {@code true} if the entry was added, {@code false} if the quest log was not initialized
   */
  public static boolean add(String tab, String text, boolean onlyForCreator) {
    return add(tab, new QuestLogEntry(text, onlyForCreator));
  }

  /**
   * Adds a public system-owned quest log entry to the given tab.
   *
   * <p>The created entry uses {@link QuestLogEntry#DEFAULT_OWNER} as owner, stores the current game
   * tick as its timestamp, and is not restricted to the creator. This is the shortest overload for
   * normal server-side quest progress entries.
   *
   * @param tab the tab to add the entry to
   * @param text the text shown for the quest log entry
   * @return {@code true} if the entry was added, {@code false} if the quest log was not initialized
   */
  public static boolean add(String tab, String text) {
    return add(tab, text, false);
  }

  /**
   * Adds an owner-specific quest log entry to the given tab.
   *
   * <p>The created entry stores the current game tick as its timestamp. Use this when the creator
   * is already known as a string, for example when gameplay code stores a player name separately.
   *
   * @param tab the tab to add the entry to
   * @param text the text shown for the quest log entry
   * @param owner the identifier of the owner or creator of this entry
   * @param onlyForCreator true if the entry should only be visible to its creator, false otherwise
   * @return {@code true} if the entry was added, {@code false} if the quest log was not initialized
   */
  public static boolean add(String tab, String text, String owner, boolean onlyForCreator) {
    return add(tab, new QuestLogEntry(text, owner, onlyForCreator));
  }

  /**
   * Adds a player-authored note to the shared quest log.
   *
   * <p>This method is useful for server-handled client input. The UI or interaction handler passes
   * the requesting player entity and the submitted note text; this method trims blank input,
   * resolves the player name from {@link PlayerComponent}, and stores the note in the requested
   * tab.
   *
   * <pre>{@code
   * QuestLogUtil.addPlayerNote(player, "Notes", submittedText, false);
   * QuestLogUtil.addPlayerNote(player, "Private Notes", submittedText, true);
   * }</pre>
   *
   * @param player the player who created the note
   * @param tab the tab to add the note to
   * @param text the submitted note text
   * @param onlyForCreator true if the note should only be visible to its creator, false otherwise
   * @return {@code true} if the note was added, {@code false} if the note was blank or the quest
   *     log was not initialized
   */
  public static boolean addPlayerNote(
      Entity player, String tab, String text, boolean onlyForCreator) {
    if (text == null) {
      return false;
    }

    String trimmedText = text.trim();
    if (trimmedText.isEmpty()) {
      return false;
    }

    String creator =
        Optional.ofNullable(player)
            .flatMap(entity -> entity.fetch(PlayerComponent.class))
            .map(PlayerComponent::playerName)
            .orElse(QuestLogEntry.DEFAULT_OWNER);

    boolean added =
        add(tab, new QuestLogEntry(trimmedText, Game.currentTick(), true, creator, onlyForCreator));
    if (added && player != null && !Game.isMultiplayerClient()) {
      engine.tracking.Tracking.participantForEntity(player.id())
          .ifPresent(
              id ->
                  engine.tracking.Tracking.interaction(
                      "quest-log", onlyForCreator ? "private-note-added" : "note-added", id));
    }
    return added;
  }

  /**
   * Adds a public player-authored note to the default user note tab.
   *
   * <p>This is a convenience overload for the common UI note case. It behaves like {@code
   * addPlayerNote(player, "Notes", text, false)}.
   *
   * @param player the player who created the note
   * @param text the submitted note text
   * @return {@code true} if the note was added, {@code false} if the note was blank or the quest
   *     log was not initialized
   */
  public static boolean addPlayerNote(Entity player, String text) {
    return addPlayerNote(player, USER_NOTE_TAB, text, false);
  }
}
