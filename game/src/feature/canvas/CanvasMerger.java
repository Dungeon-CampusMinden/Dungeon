package feature.canvas;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Merges the server provided default nodes of a canvas with the local changes of the player.
 *
 * <p>This is the heart of the canvas persistence model. It is what allows the server to change its
 * default node set between two openings of the same canvas while the player keeps everything they
 * built on top of it.
 *
 * <h2>Merge rules</h2>
 *
 * <ol>
 *   <li>Start from the server defaults, in server order.
 *   <li>Drop every default whose id is tombstoned in the overlay: the player deleted it.
 *   <li>For a surviving default with a recorded override, take the override's position, z, size and
 *       props, but keep the type id and flags of the server default. This way the server stays
 *       authoritative about <em>what</em> a node is while the player stays in control of
 *       <em>where</em> it is.
 *   <li>Defaults without an override are used unchanged. This is how newly appearing defaults - the
 *       nodes unlocked by finding an item, for example - show up at their intended position.
 *   <li>Append all locally added nodes.
 *   <li>Prune overlay entries that reference ids the server no longer provides, according to {@link
 *       CanvasOptions#pruneStaleOverrides()} and {@link CanvasOptions#pruneStaleTombstones()}.
 * </ol>
 *
 * <p>Local node ids live in their own namespace (see {@link CanvasArea#newLocalId()}), so they can
 * never collide with server default ids. Should a collision happen anyway, the local node is
 * dropped and the server default wins.
 */
public final class CanvasMerger {

  private CanvasMerger() {}

  /**
   * Merges defaults and overlay into the effective node states of a canvas.
   *
   * <p>The overlay is pruned in place according to the given options.
   *
   * @param defaults the server provided default nodes; must not be null
   * @param overlay the local changes; must not be null
   * @param options the canvas options controlling pruning; must not be null
   * @return the effective node states, defaults first and local additions last
   */
  public static List<NodeState> merge(
      List<NodeState> defaults, CanvasOverlay overlay, CanvasOptions options) {
    Objects.requireNonNull(defaults, "defaults");
    Objects.requireNonNull(overlay, "overlay");
    Objects.requireNonNull(options, "options");

    Set<String> defaultIds = new LinkedHashSet<>();
    defaults.forEach(state -> defaultIds.add(state.id()));
    overlay.pruneStale(defaultIds, options.pruneStaleOverrides(), options.pruneStaleTombstones());

    List<NodeState> merged = new ArrayList<>(defaults.size() + overlay.additions().size());
    for (NodeState serverState : defaults) {
      NodeState base = serverState.withOrigin(NodeOrigin.DEFAULT);
      if (overlay.isTombstoned(base.id())) {
        continue;
      }
      merged.add(applyOverride(base, overlay.override(base.id())));
    }

    for (NodeState local : overlay.additions()) {
      if (defaultIds.contains(local.id())) {
        continue;
      }
      merged.add(local.withOrigin(NodeOrigin.LOCAL));
    }
    return merged;
  }

  /**
   * Merges a snapshot of defaults with an overlay.
   *
   * @param defaults the server provided defaults; must not be null
   * @param overlay the local changes; must not be null
   * @param options the canvas options controlling pruning; must not be null
   * @return the effective node states
   */
  public static List<NodeState> merge(
      CanvasSnapshot defaults, CanvasOverlay overlay, CanvasOptions options) {
    return merge(defaults.nodes(), overlay, options);
  }

  private static NodeState applyOverride(NodeState base, Optional<NodeState> override) {
    if (override.isEmpty()) {
      return base;
    }
    NodeState local = override.get();
    return new NodeState(
        base.typeId(),
        base.id(),
        NodeOrigin.DEFAULT,
        local.x(),
        local.y(),
        local.z(),
        local.width(),
        local.height(),
        base.movable(),
        base.deletable(),
        // sticky travels with the position, because it decides which coordinate space x/y are in
        local.sticky(),
        local.props().isEmpty() ? base.props() : local.props());
  }
}
