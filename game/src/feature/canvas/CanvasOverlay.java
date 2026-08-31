package feature.canvas;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * The purely local changes a player made to a canvas.
 *
 * <p>An overlay never contains the server provided default nodes themselves. It only records what
 * the player changed on top of them:
 *
 * <ul>
 *   <li><b>additions</b> - nodes the player created locally, stored in full
 *   <li><b>tombstones</b> - ids of default nodes the player deleted
 *   <li><b>overrides</b> - modified state (position, z, size, props) of default nodes that still
 *       exist
 * </ul>
 *
 * <p>Because of this split, the server can grow or shrink its default node set between two openings
 * of the same canvas without invalidating the player's work: new defaults simply appear, deleted
 * ones stay deleted and moved ones keep their position.
 *
 * @see CanvasMerger
 * @see CanvasStore
 */
public final class CanvasOverlay {

  private final Map<String, NodeState> additions = new LinkedHashMap<>();
  private final Map<String, NodeState> overrides = new LinkedHashMap<>();
  private final Set<String> tombstones = new LinkedHashSet<>();

  /** Creates an empty overlay. */
  public CanvasOverlay() {}

  /**
   * Records a node as locally added or updates an already recorded local node.
   *
   * @param state the state of the local node; its origin must be {@link NodeOrigin#LOCAL}
   */
  public void putAddition(NodeState state) {
    Objects.requireNonNull(state, "state");
    additions.put(state.id(), state.withOrigin(NodeOrigin.LOCAL));
    tombstones.remove(state.id());
  }

  /**
   * Records modified state for a server provided default node.
   *
   * @param state the current state of the default node
   */
  public void putOverride(NodeState state) {
    Objects.requireNonNull(state, "state");
    overrides.put(state.id(), state.withOrigin(NodeOrigin.DEFAULT));
  }

  /**
   * Records the current state of a node, choosing the right bucket based on its origin.
   *
   * @param state the state to record
   */
  public void put(NodeState state) {
    Objects.requireNonNull(state, "state");
    if (state.origin() == NodeOrigin.LOCAL) {
      putAddition(state);
    } else {
      putOverride(state);
    }
  }

  /**
   * Records that a node was removed.
   *
   * <p>Locally added nodes are simply forgotten; default nodes get a tombstone so they stay deleted
   * the next time the canvas is opened.
   *
   * @param state the state of the removed node
   */
  public void remove(NodeState state) {
    Objects.requireNonNull(state, "state");
    if (state.origin() == NodeOrigin.LOCAL) {
      additions.remove(state.id());
      return;
    }
    overrides.remove(state.id());
    tombstones.add(state.id());
  }

  /**
   * Returns the locally added nodes.
   *
   * @return an unmodifiable list of local node states
   */
  public List<NodeState> additions() {
    return List.copyOf(additions.values());
  }

  /**
   * Returns the recorded overrides for default nodes.
   *
   * @return an unmodifiable map from node id to overridden state
   */
  public Map<String, NodeState> overrides() {
    return Map.copyOf(overrides);
  }

  /**
   * Returns the ids of deleted default nodes.
   *
   * @return an unmodifiable set of tombstoned node ids
   */
  public Set<String> tombstones() {
    return Set.copyOf(tombstones);
  }

  /**
   * Returns the override recorded for the given default node.
   *
   * @param nodeId the node id to look up
   * @return the override, if one was recorded
   */
  public Optional<NodeState> override(String nodeId) {
    return Optional.ofNullable(overrides.get(nodeId));
  }

  /**
   * Checks whether a default node was deleted by the player.
   *
   * @param nodeId the node id to check
   * @return true if the node is tombstoned
   */
  public boolean isTombstoned(String nodeId) {
    return tombstones.contains(nodeId);
  }

  /**
   * Returns whether this overlay holds no local changes at all.
   *
   * @return true if there are no additions, overrides or tombstones
   */
  public boolean isEmpty() {
    return additions.isEmpty() && overrides.isEmpty() && tombstones.isEmpty();
  }

  /** Discards all recorded local changes. */
  public void clear() {
    additions.clear();
    overrides.clear();
    tombstones.clear();
  }

  /**
   * Drops overrides and tombstones for node ids that are not part of the given default set.
   *
   * @param knownDefaultIds the ids currently provided by the server
   * @param pruneOverrides true to drop stale overrides
   * @param pruneTombstones true to drop stale tombstones
   */
  public void pruneStale(
      Collection<String> knownDefaultIds, boolean pruneOverrides, boolean pruneTombstones) {
    if (pruneOverrides) {
      overrides.keySet().retainAll(new LinkedHashSet<>(knownDefaultIds));
    }
    if (pruneTombstones) {
      tombstones.retainAll(new LinkedHashSet<>(knownDefaultIds));
    }
  }

  /**
   * Serializes this overlay.
   *
   * <p>Tombstones are encoded as marker states so the whole overlay fits into a single {@link
   * CanvasSnapshot}.
   *
   * @return a snapshot describing this overlay
   */
  public CanvasSnapshot toSnapshot() {
    List<NodeState> states = new ArrayList<>(additions.values());
    states.addAll(overrides.values());
    for (String id : tombstones) {
      states.add(
          new NodeState(
              TOMBSTONE_TYPE, id, NodeOrigin.DEFAULT, 0f, 0f, 0, 0f, 0f, false, false, Map.of()));
    }
    return new CanvasSnapshot(states);
  }

  /**
   * Restores an overlay from a snapshot created by {@link #toSnapshot()}.
   *
   * @param snapshot the snapshot to read; must not be null
   * @return the restored overlay
   */
  public static CanvasOverlay fromSnapshot(CanvasSnapshot snapshot) {
    Objects.requireNonNull(snapshot, "snapshot");
    CanvasOverlay overlay = new CanvasOverlay();
    for (NodeState state : snapshot.nodes()) {
      if (TOMBSTONE_TYPE.equals(state.typeId())) {
        overlay.tombstones.add(state.id());
      } else {
        overlay.put(state);
      }
    }
    return overlay;
  }

  /** Pseudo type id used to encode tombstones inside a snapshot. */
  private static final String TOMBSTONE_TYPE = "canvas.tombstone";

  @Override
  public String toString() {
    return "CanvasOverlay[additions="
        + additions.size()
        + ", overrides="
        + overrides.size()
        + ", tombstones="
        + tombstones.size()
        + ']';
  }
}
