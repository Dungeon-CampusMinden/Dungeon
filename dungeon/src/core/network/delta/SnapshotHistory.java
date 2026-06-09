package core.network.delta;

import core.network.messages.s2c.SnapshotMessage;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/** Fixed-size history of full snapshots indexed by server tick. */
public final class SnapshotHistory {
  private final int capacity;
  private final LinkedHashMap<Integer, SnapshotMessage> snapshots = new LinkedHashMap<>();

  /**
   * Creates a snapshot history with the given capacity.
   *
   * @param capacity maximum number of snapshots to retain
   */
  public SnapshotHistory(int capacity) {
    if (capacity <= 0) {
      throw new IllegalArgumentException("capacity must be positive");
    }
    this.capacity = capacity;
  }

  /**
   * Stores a full snapshot and evicts the oldest entry when capacity is exceeded.
   *
   * @param snapshot snapshot to store
   */
  public synchronized void add(SnapshotMessage snapshot) {
    add(snapshot, Set.of());
  }

  /**
   * Stores a full snapshot while preserving protected ticks outside the rolling capacity.
   *
   * <p>Protected ticks are intended for client-specific acknowledged or in-flight baselines. They
   * remain available even when the normal rolling window advances, so stable play cannot evict the
   * only baseline a client can currently acknowledge.
   *
   * @param snapshot snapshot to store
   * @param protectedTicks snapshot ticks that must not be evicted by this insertion
   */
  public synchronized void add(SnapshotMessage snapshot, Collection<Integer> protectedTicks) {
    Objects.requireNonNull(snapshot, "snapshot");
    Set<Integer> protectedTickSet = new HashSet<>(Objects.requireNonNull(protectedTicks));
    protectedTickSet.removeIf(Objects::isNull);
    snapshots.remove(snapshot.serverTick());
    snapshots.put(snapshot.serverTick(), copyOf(snapshot));
    evictOverflow(protectedTickSet);
  }

  /**
   * Finds a snapshot by server tick.
   *
   * @param serverTick server tick to look up
   * @return matching snapshot, if retained
   */
  public synchronized Optional<SnapshotMessage> snapshot(int serverTick) {
    return Optional.ofNullable(snapshots.get(serverTick)).map(SnapshotHistory::copyOf);
  }

  /**
   * Returns the newest retained snapshot.
   *
   * @return newest snapshot, if any
   */
  public synchronized Optional<SnapshotMessage> newest() {
    return snapshots.isEmpty()
        ? Optional.empty()
        : Optional.of(copyOf(snapshots.lastEntry().getValue()));
  }

  /**
   * Returns whether a snapshot tick is retained.
   *
   * @param serverTick server tick to test
   * @return true when the tick is present
   */
  public synchronized boolean contains(int serverTick) {
    return snapshots.containsKey(serverTick);
  }

  /** Clears all retained snapshots. */
  public synchronized void clear() {
    snapshots.clear();
  }

  /**
   * Returns the number of retained snapshots.
   *
   * @return retained snapshot count
   */
  public synchronized int size() {
    return snapshots.size();
  }

  /**
   * Returns the maximum number of snapshots retained by this history.
   *
   * @return snapshot capacity
   */
  public int capacity() {
    return capacity;
  }

  private void evictOverflow(Set<Integer> protectedTicks) {
    while (unprotectedSize(protectedTicks) > capacity) {
      boolean removed = false;
      for (var iterator = snapshots.keySet().iterator(); iterator.hasNext(); ) {
        Integer tick = iterator.next();
        if (!protectedTicks.contains(tick)) {
          iterator.remove();
          removed = true;
          break;
        }
      }
      if (!removed) {
        return;
      }
    }
  }

  private int unprotectedSize(Set<Integer> protectedTicks) {
    int unprotected = 0;
    for (Integer tick : snapshots.keySet()) {
      if (!protectedTicks.contains(tick)) {
        unprotected++;
      }
    }
    return unprotected;
  }

  private static SnapshotMessage copyOf(SnapshotMessage snapshot) {
    return new SnapshotMessage(
        snapshot.serverTick(), List.copyOf(snapshot.entities()), snapshot.levelState());
  }
}
