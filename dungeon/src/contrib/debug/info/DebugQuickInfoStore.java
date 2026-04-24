package contrib.debug.info;

import core.Entity;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

/**
 * Stores optional debug info text for entities.
 *
 * <p>The store uses weak references for keys, so entries disappear once the associated entities are
 * no longer strongly referenced elsewhere.
 */
public final class DebugQuickInfoStore {

  /** Creates an empty debug quick-info store. */
  public DebugQuickInfoStore() {}

  private final Map<Entity, String> quickInfoByEntity =
    Collections.synchronizedMap(new WeakHashMap<>());

  /**
   * Stores or removes additional debug info for an entity.
   *
   * <p>Passing {@code null} or blank text removes the current entry.
   *
   * @param entity entity whose info should be updated
   * @param quickInfo additional debug info text, or {@code null} to remove it
   */
  public void set(Entity entity, String quickInfo) {
    if (quickInfo == null || quickInfo.isBlank()) {
      quickInfoByEntity.remove(entity);
      return;
    }

    quickInfoByEntity.put(entity, quickInfo.strip());
  }

  /**
   * Looks up additional debug info for an entity.
   *
   * @param entity entity whose info should be queried
   * @return stored debug info if present
   */
  public Optional<String> get(Entity entity) {
    return Optional.ofNullable(quickInfoByEntity.get(entity));
  }

  /** Removes all stored entity debug info. */
  public void clear() {
    quickInfoByEntity.clear();
  }
}
