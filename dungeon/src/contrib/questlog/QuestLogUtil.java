package contrib.questlog;

import core.Entity;
import java.util.Optional;

/**
 * Utility methods for creating and writing to the shared quest log.
 *
 * <p>The quest log is stored on a single {@link Entity} with a {@link QuestLogComponent}. Call
 * {@link #initQuestLog()} during setup and add the returned entity to the game if it should be part
 * of the ECS world.
 *
 * <p>The add methods are convenience wrappers around {@link QuestLogComponent#add(String,
 * QuestLogEntry)}. String-based overloads create a {@link QuestLogEntry} internally; the entry sets
 * its timestamp to the current game tick. These methods return {@code false} if the shared quest
 * log has not been initialized.
 */
public final class QuestLogUtil {

  private static final String QUEST_LOG_ENTITY_NAME = "Questlog";

  private static Entity questlog;

  private QuestLogUtil() {}

  /**
   * Creates a new quest log entity and stores it as the shared quest log.
   *
   * <p>The returned entity contains a fresh {@link QuestLogComponent}. Calling this method again
   * replaces the previously stored quest log entity and discards the old shared reference.
   *
   * @return the created quest log entity
   */
  public static Entity initQuestLog() {
    questlog = new Entity(QUEST_LOG_ENTITY_NAME);
    questlog.add(new QuestLogComponent());
    return questlog;
  }

  /**
   * Returns the shared quest log entity created by {@link #initQuestLog()}.
   *
   * @return an {@link Optional} containing the quest log entity, or {@link Optional#empty()} if the
   *     quest log was not initialized
   */
  public static Optional<Entity> getQuestLog() {
    return Optional.ofNullable(questlog);
  }

  /**
   * Stores an existing entity as the shared quest log.
   *
   * <p>This is used by network synchronization when a client receives the server-owned quest log
   * entity. The entity must already contain a {@link QuestLogComponent}; otherwise it cannot be used
   * as the shared quest log.
   *
   * @param entity the synchronized quest log entity
   * @throws IllegalArgumentException if the entity has no {@link QuestLogComponent}
   */
  public static void setQuestLog(Entity entity) {
    if (entity == null || entity.fetch(QuestLogComponent.class).isEmpty()) {
      throw new IllegalArgumentException("Quest log entity must contain a QuestLogComponent.");
    }
    questlog = entity;
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
   * <p>The quest log is considered initialized only after {@link #initQuestLog()} created the
   * backing entity and that entity still contains a {@link QuestLogComponent}. UI code can use this
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
   * restoring saved quest log data or when a specific timestamp is needed.
   *
   * @param tab the tab to add the entry to
   * @param entry the entry to add
   * @return {@code true} if the entry was added, {@code false} if the quest log was not initialized
   */
  public static boolean add(String tab, QuestLogEntry entry) {
    return getQuestLogComponent().map(component -> component.add(tab, entry)).orElse(false);
  }

  /**
   * Adds a system-owned quest log entry to the given tab.
   *
   * <p>The created entry uses {@link QuestLogEntry#DEFAULT_OWNER} as owner and stores the current
   * game tick as its timestamp.
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
   * tick as its timestamp, and is not restricted to the creator.
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
   * <p>The created entry stores the current game tick as its timestamp.
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
}
