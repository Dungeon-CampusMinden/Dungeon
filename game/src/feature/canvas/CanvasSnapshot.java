package feature.canvas;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Serializable container for a list of {@link NodeState}s.
 *
 * <p>Used both as the network payload for the server provided default nodes of a canvas (see {@link
 * CanvasSnapshotCodec}) and as the persistence format of a {@link CanvasOverlay}.
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
