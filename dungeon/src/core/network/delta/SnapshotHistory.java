package core.network.delta;

import core.network.messages.s2c.SnapshotMessage;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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
    Objects.requireNonNull(snapshot, "snapshot");
    snapshots.remove(snapshot.serverTick());
    snapshots.put(snapshot.serverTick(), snapshot);
    while (snapshots.size() > capacity) {
      Integer oldestTick = snapshots.keySet().iterator().next();
      snapshots.remove(oldestTick);
    }
  }

  /**
   * Finds a snapshot by server tick.
   *
   * @param serverTick server tick to look up
   * @return matching snapshot, if retained
   */
  public synchronized Optional<SnapshotMessage> snapshot(int serverTick) {
    return Optional.ofNullable(snapshots.get(serverTick));
  }

  /**
   * Returns the newest retained snapshot.
   *
   * @return newest snapshot, if any
   */
  public synchronized Optional<SnapshotMessage> newest() {
    SnapshotMessage newest = null;
    for (Map.Entry<Integer, SnapshotMessage> entry : snapshots.entrySet()) {
      newest = entry.getValue();
    }
    return Optional.ofNullable(newest);
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
}
