package core.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe central ID provider for Entities. Ensures uniqueness across auto-generated and
 * explicitly assigned IDs.
 */
public final class EntityIdProvider {
  private static final AtomicInteger NEXT = new AtomicInteger(0);
  private static final ConcurrentHashMap<Integer, Boolean> USED = new ConcurrentHashMap<>();

  private EntityIdProvider() {}

  /**
   * Get the next unique ID.
   *
   * @return a unique, non-negative ID
   */
  public static int nextId() {
    while (true) {
      int id = NEXT.getAndIncrement();
      if (USED.putIfAbsent(id, Boolean.TRUE) == null) {
        return id;
      }
      // collision with an explicitly registered id, try next
    }
  }

  /**
   * Register a given id. Throws if the id is already used.
   *
   * @param id the id to register
   * @throws IllegalArgumentException if id is already in use or negative
   */
  public static void registerOrThrow(int id) {
    if (id < 0) {
      throw new IllegalArgumentException("Entity id must be non-negative");
    }
    if (USED.putIfAbsent(id, Boolean.TRUE) != null) {
      throw new IllegalArgumentException("Entity id already in use: " + id);
    }
    // Bump NEXT forward to avoid long collision loops when many explicit ids are high
    bumpNextIfNeeded(id + 1);
  }

  /**
   * Ensure the given ID is registered as used, without throwing if already present. Useful when
   * entities are constructed externally and then added to the game.
   *
   * @param id the id to ensure registration for
   * @throws IllegalArgumentException if id is negative
   */
  public static void ensureRegistered(int id) {
    if (id < 0) {
      throw new IllegalArgumentException("Entity id must be non-negative");
    }
    USED.putIfAbsent(id, Boolean.TRUE);
    bumpNextIfNeeded(id + 1);
  }

  /**
   * Unregister a previously used id. Idempotent.
   *
   * @param id the id to release
   */
  public static void unregister(int id) {
    USED.remove(id);
  }

  private static void bumpNextIfNeeded(int minNext) {
    int prev;
    do {
      prev = NEXT.get();
      if (prev >= minNext) return;
    } while (!NEXT.compareAndSet(prev, minNext));
  }
}
