package feature.canvas;

import engine.utils.logging.DungeonLogger;
import feature.canvas.nodes.ActionNode;
import feature.canvas.nodes.LabelNode;
import feature.canvas.nodes.SocketNode;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Registry that maps a stable node type id to a factory able to rebuild a {@link CanvasNode} from a
 * {@link NodeState}.
 *
 * <p>Every node type that should survive being sent over the network or being stored as a local
 * change must be registered here exactly once, ideally from a static initializer of the node class
 * itself or from the feature that introduces it:
 *
 * <pre>{@code
 * CanvasNodeType.register("myPuzzle.slot", SlotNode::new);
 * }</pre>
 *
 * <p>Unknown type ids are not fatal: {@link #create(NodeState)} falls back to a plain {@link
 * CanvasNode} that renders as a bordered box with the node id and logs a warning, so a client that
 * receives node types from a newer server degrades gracefully instead of crashing.
 */
public final class CanvasNodeType {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(CanvasNodeType.class);

  private static final Map<String, Function<NodeState, CanvasNode>> FACTORIES =
      new ConcurrentHashMap<>();

  static {
    register(CanvasNode.TYPE_ID, state -> new CanvasNode(state.id()));
    LabelNode.registerType();
    ActionNode.registerType();
    SocketNode.registerType();
  }

  private CanvasNodeType() {}

  /**
   * Registers a node type.
   *
   * <p>Re-registering the same type id with the same factory instance is a no-op; registering a
   * different factory for an already known type id is an error, because it would make snapshots
   * ambiguous.
   *
   * @param typeId the stable type identifier; must not be null or blank
   * @param factory creates a node from a state; must not be null
   * @throws IllegalStateException if a different factory is already registered for the type id
   */
  public static void register(String typeId, Function<NodeState, CanvasNode> factory) {
    Objects.requireNonNull(typeId, "typeId");
    Objects.requireNonNull(factory, "factory");
    if (typeId.isBlank()) {
      throw new IllegalArgumentException("typeId must not be blank");
    }
    Function<NodeState, CanvasNode> existing = FACTORIES.putIfAbsent(typeId, factory);
    if (existing != null && existing != factory) {
      throw new IllegalStateException("Duplicate CanvasNodeType registration for '" + typeId + "'");
    }
  }

  /**
   * Checks whether a type id is known.
   *
   * @param typeId the type identifier to look up
   * @return true if a factory is registered for the type id
   */
  public static boolean isRegistered(String typeId) {
    return typeId != null && FACTORIES.containsKey(typeId);
  }

  /**
   * Looks up the factory for a type id.
   *
   * @param typeId the type identifier to look up
   * @return the factory, if one is registered
   */
  public static Optional<Function<NodeState, CanvasNode>> factory(String typeId) {
    return typeId == null ? Optional.empty() : Optional.ofNullable(FACTORIES.get(typeId));
  }

  /**
   * Rebuilds a node from its state.
   *
   * <p>The returned node always has the common state of {@code state} applied, even when the type
   * id is unknown and the generic fallback node is used.
   *
   * @param state the state to materialize; must not be null
   * @return the created node
   */
  public static CanvasNode create(NodeState state) {
    Objects.requireNonNull(state, "state");
    Function<NodeState, CanvasNode> factory = FACTORIES.get(state.typeId());
    if (factory == null) {
      LOGGER.warn(
          "Unknown canvas node type '{}' for node '{}', falling back to a generic node",
          state.typeId(),
          state.id());
      return new FallbackNode(state);
    }
    CanvasNode node = factory.apply(state);
    node.applyState(state);
    return node;
  }

  /**
   * Generic node used when a {@link NodeState} references an unregistered type id.
   *
   * <p>It keeps the original type id and all props so that re-serializing the node does not lose
   * information the client could not interpret.
   */
  private static final class FallbackNode extends CanvasNode {
    private final String originalTypeId;
    private final Map<String, String> originalProps;

    private FallbackNode(NodeState state) {
      super(state.id());
      this.originalTypeId = state.typeId();
      this.originalProps = state.props();
      applyState(state);
    }

    @Override
    public String typeId() {
      return originalTypeId;
    }

    @Override
    protected void writeProps(NodeState.Props props) {
      originalProps.forEach(props::put);
    }
  }
}
