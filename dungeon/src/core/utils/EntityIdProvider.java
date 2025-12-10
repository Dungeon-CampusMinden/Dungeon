package core.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe central ID provider for Entities. Ensures uniqueness across auto-generated and
 * explicitly assigned IDs.
 *
 * <p>Supports both server entities (non-negative IDs) and local-only entities (negative IDs).
 * Local-only entities are client-side only and should not be synced over the network.
 */
public final class EntityIdProvider {
  private static final AtomicInteger NEXT = new AtomicInteger(0);
  private static final AtomicInteger LOCAL_NEXT = new AtomicInteger(-1);
  private static final ConcurrentHashMap<Integer, Boolean> USED = new ConcurrentHashMap<>();

  private EntityIdProvider() {}

  /**
   * Get the next unique ID.
   *
   * <p>This method generates a unique, non-negative ID that is guaranteed not to collide with any
   * previously registered IDs. It also registers the ID as used to prevent future collisions.
   *
   * <p>Overflow-safe: the internal counter saturates at Integer.MAX_VALUE. If no free IDs remain
   * (e.g., MAX_VALUE is already taken), this method throws an IllegalStateException.
   *
   * @return a unique, non-negative ID
   * @throws IllegalStateException if the ID space is exhausted
   */
  public static int nextId() {
    while (true) {
      // Saturating increment to avoid int overflow into negative values.
      int id = NEXT.getAndUpdate(curr -> curr == Integer.MAX_VALUE ? Integer.MAX_VALUE : curr + 1);

      if (USED.putIfAbsent(id, Boolean.TRUE) == null) {
        return id;
      }

      // If Integer.MAX_VALUE is already taken, no further IDs are available.
      if (id == Integer.MAX_VALUE) {
        throw new IllegalStateException("Entity ID space exhausted");
      }
      // collision with an explicitly registered id, try next
    }
  }

  /**
   * Get the next unique local-only entity ID.
   *
   * <p>Local-only entities are client-side only and should not be synced over the network. This
   * method generates a unique, negative ID that is guaranteed not to collide with any previously
   * generated local IDs. The ID is automatically registered as used.
   *
   * <p>Overflow-safe: the internal counter saturates at Integer.MIN_VALUE. If no free IDs remain
   * (e.g., Integer.MIN_VALUE is already taken), this method throws an IllegalStateException.
   *
   * @return a unique, negative ID for local-only entities
   * @throws IllegalStateException if the local ID space is exhausted
   */
  public static int nextLocalId() {
    while (true) {
      // Saturating decrement to avoid int overflow into positive values.
      int id =
          LOCAL_NEXT.getAndUpdate(curr -> curr == Integer.MIN_VALUE ? Integer.MIN_VALUE : curr - 1);

      if (USED.putIfAbsent(id, Boolean.TRUE) == null) {
        return id;
      }

      // If Integer.MIN_VALUE is already taken, no further IDs are available.
      if (id == Integer.MIN_VALUE) {
        throw new IllegalStateException("Local Entity ID space exhausted");
      }
      // collision with an explicitly registered id, try next
    }
  }

  /**
   * Register a given id. Throws if the id is already used.
   *
   * <p>Supports both non-negative server IDs and negative local-only IDs.
   *
   * @param id the id to register (can be negative for local entities)
   * @throws IllegalArgumentException if id is already in use
   */
  public static void registerOrThrow(int id) {
    if (USED.putIfAbsent(id, Boolean.TRUE) != null) {
      throw new IllegalArgumentException("Entity id already in use: " + id);
    }
  }

  /**
   * Ensure the given ID is registered as used. Does not throw if already present.
   *
   * <p>Supports both non-negative server IDs and negative local-only IDs.
   *
   * @param id the id to ensure registration for
   * @return true if the id was newly registered; false if it was already registered
   */
  public static boolean ensureRegistered(int id) {
    return USED.putIfAbsent(id, Boolean.TRUE) == null;
  }

  /**
   * Unregister a previously used id. Idempotent.
   *
   * @param id the id to release
   */
  public static void unregister(int id) {
    USED.remove(id);
  }
}
