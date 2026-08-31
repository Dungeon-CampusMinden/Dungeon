package feature.canvas;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Serializable container for a list of {@link NodeState}s.
 *
 * <p>This is the one and only collection type of the canvas framework. The very same class is used
 * for all three roles:
 *
 * <ul>
 *   <li>the <b>defaults</b> the server sends to the client when a canvas dialog opens (see {@link
 *       CanvasSnapshotCodec}),
 *   <li>the <b>local changes</b> a player made, persisted through {@link CanvasStore},
 *   <li>the <b>effective</b> node list a canvas is populated from.
 * </ul>
 *
 * <p>In a snapshot of local changes each entry is interpreted through its {@link
 * NodeState#origin()} and {@link NodeState#deleted()} flags:
 *
 * <ul>
 *   <li>{@link NodeOrigin#LOCAL} - a node the player created,
 *   <li>{@link NodeOrigin#DEFAULT} - modified state of a server default that still exists,
 *   <li>{@link NodeState#deleted()} - a server default the player removed.
 * </ul>
 *
 * <p>{@link #mergeWith(CanvasSnapshot, CanvasOptions)} combines defaults and changes back into the
 * effective node list. Because both sides are the same type, the server can grow or shrink its
 * default set between two openings of a canvas without invalidating the player's work.
 */
public final class CanvasSnapshot implements Serializable {

  private static final long serialVersionUID = 1L;

  private final List<NodeState> nodes;

  /**
   * Creates a snapshot from the given states.
   *
   * @param nodes the node states; must not be null
   */
  public CanvasSnapshot(List<NodeState> nodes) {
    Objects.requireNonNull(nodes, "nodes");
    this.nodes = List.copyOf(nodes);
  }

  /**
   * Creates an empty snapshot.
   *
   * @return a snapshot without any nodes
   */
  public static CanvasSnapshot empty() {
    return new CanvasSnapshot(List.of());
  }

  /**
   * Returns the node states of this snapshot.
   *
   * @return an unmodifiable list of node states
   */
  public List<NodeState> nodes() {
    return Collections.unmodifiableList(nodes);
  }

  /**
   * Returns the number of node states in this snapshot.
   *
   * @return the node count
   */
  public int size() {
    return nodes.size();
  }

  /**
   * Returns the node states of this snapshot indexed by node id.
   *
   * @return a map from node id to state, in snapshot order
   */
  public Map<String, NodeState> byId() {
    Map<String, NodeState> map = new LinkedHashMap<>();
    nodes.forEach(state -> map.put(state.id(), state));
    return map;
  }

  /**
   * Returns the ids of all node states in this snapshot.
   *
   * @return the node ids, in snapshot order
   */
  public Set<String> ids() {
    Set<String> ids = new LinkedHashSet<>();
    nodes.forEach(state -> ids.add(state.id()));
    return ids;
  }

  /**
   * Merges this snapshot of server defaults with a snapshot of local player changes.
   *
   * <p>Merge rules, applied in this order:
   *
   * <ol>
   *   <li>Start from the defaults, in server order.
   *   <li>Drop every default marked {@link NodeState#deleted()} in the changes: the player removed
   *       it.
   *   <li>For a surviving default that has a change entry, take the player's position, z, size,
   *       sticky flag and props, but keep the type id and the movable/deletable flags of the server
   *       default. The server stays authoritative about <em>what</em> a node is, the player about
   *       <em>where</em> it is.
   *   <li>Defaults without a change entry are used unchanged. This is how newly unlocked defaults
   *       appear at their intended position.
   *   <li>Append all locally created nodes, skipping ids that collide with a default.
   * </ol>
   *
   * <p>Change entries referring to ids the server no longer provides do not appear in the result;
   * whether they are <em>kept</em> for future merges is decided when the changes are recorded, see
   * {@link CanvasOptions#pruneStaleOverrides()}.
   *
   * @param changes the local changes; must not be null
   * @param options the canvas options; must not be null
   * @return the effective node states, defaults first and local additions last
   */
  public CanvasSnapshot mergeWith(CanvasSnapshot changes, CanvasOptions options) {
    Objects.requireNonNull(changes, "changes");
    Objects.requireNonNull(options, "options");

    Map<String, NodeState> changesById = changes.byId();
    Set<String> defaultIds = ids();
    List<NodeState> merged = new ArrayList<>(nodes.size() + changes.size());

    for (NodeState serverState : nodes) {
      NodeState base = serverState.withOrigin(NodeOrigin.DEFAULT);
      NodeState change = changesById.get(base.id());
      if (change != null && change.deleted()) {
        continue;
      }
      merged.add(change == null ? base : applyChange(base, change));
    }

    for (NodeState change : changes.nodes()) {
      if (change.deleted()
          || change.origin() != NodeOrigin.LOCAL
          || defaultIds.contains(change.id())) {
        continue;
      }
      merged.add(change.withOrigin(NodeOrigin.LOCAL));
    }
    return new CanvasSnapshot(merged);
  }

  /**
   * Computes the local changes of a canvas by comparing its live nodes against the server defaults.
   *
   * <p>This is the exact inverse of {@link #mergeWith(CanvasSnapshot, CanvasOptions)} and replaces
   * the need to observe every single player edit while the canvas is open: a node that differs from
   * its default becomes a change entry, a default that is no longer present becomes a deletion
   * marker, and everything else is a local addition.
   *
   * <p>Change entries of the previous recording that refer to ids the server no longer provides
   * cannot be derived from the live nodes, so they are carried over unless the corresponding prune
   * option asks for them to be dropped.
   *
   * @param defaults the server provided defaults the canvas was populated from; must not be null
   * @param live the current state of all nodes on the canvas; must not be null
   * @param previousChanges the changes that were loaded when the canvas opened; must not be null
   * @param options the canvas options controlling stale entry pruning; must not be null
   * @return the local changes, ready to be persisted through {@link CanvasStore}
   */
  public static CanvasSnapshot changesOf(
      CanvasSnapshot defaults,
      List<NodeState> live,
      CanvasSnapshot previousChanges,
      CanvasOptions options) {
    Objects.requireNonNull(defaults, "defaults");
    Objects.requireNonNull(live, "live");
    Objects.requireNonNull(previousChanges, "previousChanges");
    Objects.requireNonNull(options, "options");

    Map<String, NodeState> defaultsById = defaults.byId();
    Set<String> liveIds = new LinkedHashSet<>();
    List<NodeState> changes = new ArrayList<>();

    for (NodeState state : live) {
      liveIds.add(state.id());
      NodeState serverState = defaultsById.get(state.id());
      if (serverState == null) {
        changes.add(state.withOrigin(NodeOrigin.LOCAL));
        continue;
      }
      NodeState normalized = state.withOrigin(NodeOrigin.DEFAULT);
      if (!normalized.equals(serverState.withOrigin(NodeOrigin.DEFAULT))) {
        changes.add(normalized);
      }
    }

    for (String defaultId : defaultsById.keySet()) {
      if (!liveIds.contains(defaultId)) {
        changes.add(NodeState.deletion(defaultId));
      }
    }

    for (NodeState previous : previousChanges.nodes()) {
      if (previous.origin() != NodeOrigin.DEFAULT
          || defaultsById.containsKey(previous.id())
          || liveIds.contains(previous.id())) {
        continue;
      }
      boolean prune =
          previous.deleted() ? options.pruneStaleTombstones() : options.pruneStaleOverrides();
      if (!prune) {
        changes.add(previous);
      }
    }
    return new CanvasSnapshot(changes);
  }

  private static NodeState applyChange(NodeState base, NodeState change) {
    return new NodeState(
        base.typeId(),
        base.id(),
        NodeOrigin.DEFAULT,
        change.x(),
        change.y(),
        change.z(),
        change.width(),
        change.height(),
        base.movable(),
        base.deletable(),
        // sticky travels with the position, because it decides which space x/y are expressed in
        change.sticky(),
        false,
        change.props().isEmpty() ? base.props() : change.props());
  }

  /**
   * Returns a copy of this snapshot with an additional node state appended.
   *
   * @param state the state to append
   * @return the extended snapshot
   */
  public CanvasSnapshot with(NodeState state) {
    List<NodeState> copy = new ArrayList<>(nodes);
    copy.add(state);
    return new CanvasSnapshot(copy);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    return obj instanceof CanvasSnapshot other && nodes.equals(other.nodes);
  }

  @Override
  public int hashCode() {
    return nodes.hashCode();
  }

  @Override
  public String toString() {
    return "CanvasSnapshot[" + nodes.size() + " nodes]";
  }
}
