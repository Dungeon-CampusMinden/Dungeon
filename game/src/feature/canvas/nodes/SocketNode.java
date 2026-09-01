package feature.canvas.nodes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import engine.utils.TriConsumer;
import engine.utils.logging.DungeonLogger;
import feature.canvas.CanvasArea;
import feature.canvas.CanvasDragContext;
import feature.canvas.CanvasGraphics;
import feature.canvas.CanvasNode;
import feature.canvas.CanvasNodeType;
import feature.canvas.CanvasSnapshot;
import feature.canvas.CanvasSnapshotCodec;
import feature.canvas.NodeState;
import feature.hud.dialogs.DialogDesign;
import feature.hud.elements.RichLabel;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * A labeled node with a fixed number of slots that can adopt other canvas nodes.
 *
 * <p>Empty sockets are rendered as placeholder label-node boxes to the right of the socket node's
 * own content. Accepted nodes are removed from the canvas and rendered in those positions as owned
 * children. Dragging an occupied node releases it back to the canvas at the same visual position.
 *
 * <p>Filters that need to survive state reconstruction should be registered under a stable id with
 * {@link #registerFilter(String, Predicate)} and selected through {@link #filter(String)}.
 */
public class SocketNode extends CanvasNode {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(SocketNode.class);

  /** Stable type id of this node type. */
  public static final String TYPE = "canvas.socket";

  /** Prop key holding the displayed content. */
  public static final String PROP_CONTENT = "content";

  /** Prop key holding the number of sockets. */
  public static final String PROP_SOCKET_COUNT = "socketCount";

  /** Prop key holding the registered filter id. */
  public static final String PROP_FILTER_ID = "filterId";

  /** Prop key holding the registered node-added callback id. */
  public static final String PROP_NODE_ADDED_CALLBACK_ID = "nodeAddedCallbackId";

  private static final String PROP_SOCKET_PREFIX = "socket.";
  private static final String PROP_LOCKED_PREFIX = "locked.";
  private static final Map<String, Predicate<CanvasNode>> FILTERS = new ConcurrentHashMap<>();
  private static final Map<String, TriConsumer<SocketNode, Integer, CanvasNode>>
      NODE_ADDED_CALLBACKS = new ConcurrentHashMap<>();
  private static final Predicate<CanvasNode> ACCEPT_ALL = node -> true;
  private static final Predicate<CanvasNode> REJECT_ALL = node -> false;
  private static final TriConsumer<SocketNode, Integer, CanvasNode> NO_NODE_ADDED_CALLBACK =
      (socket, index, node) -> {};
  private static final CanvasSnapshotCodec SNAPSHOT_CODEC = new CanvasSnapshotCodec();

  private static final float BOX_WIDTH = 160f;
  private static final float BOX_HEIGHT = 64f;
  private static final float BOX_SPACING = 12f;
  private static final float GROUP_LINE_Y = 3f;
  private static final float GROUP_LINE_TOP = 10f;
  private static final float GROUP_LINE_THICKNESS = 2f;

  private static final Color BOX_COLOR = new Color(0.20f, 0.28f, 0.42f, 0.95f);
  private static final Color EMPTY_COLOR = new Color(0.20f, 0.28f, 0.42f, 0.35f);
  private static final Color BORDER = new Color(0.85f, 0.88f, 0.95f, 1f);
  private static final Color HIGHLIGHT = new Color(0.25f, 0.82f, 0.38f, 1f);

  private String content;
  private int socketCount;
  private String filterId;
  private Predicate<CanvasNode> filter = ACCEPT_ALL;
  private String nodeAddedCallbackId;
  private TriConsumer<SocketNode, Integer, CanvasNode> nodeAddedCallback = NO_NODE_ADDED_CALLBACK;
  private SocketEntry[] sockets;
  private boolean[] lockedSockets;
  private RichLabel label;
  private int highlightedSocket = -1;

  /**
   * Creates a socket node.
   *
   * @param id unique node id within a canvas
   * @param content the rich-label content displayed by this node
   * @param socketCount the number of sockets; must not be negative
   */
  public SocketNode(String id, String content, int socketCount) {
    super(id, preferredWidth(socketCount), BOX_HEIGHT + GROUP_LINE_TOP);
    if (socketCount < 0) {
      throw new IllegalArgumentException("socketCount must not be negative");
    }
    this.content = content == null ? id : content;
    this.socketCount = socketCount;
    this.sockets = new SocketEntry[socketCount];
    this.lockedSockets = new boolean[socketCount];
  }

  /**
   * Creates a socket node from serialized state.
   *
   * @param state the state to rebuild from
   */
  public SocketNode(NodeState state) {
    this(
        state.id(),
        state.prop(PROP_CONTENT, state.id()),
        Math.max(0, state.intProp(PROP_SOCKET_COUNT, 0)));
  }

  /** Registers this node type with the {@link CanvasNodeType} registry. */
  public static void registerType() {
    CanvasNodeType.register(TYPE, SocketNode::new);
  }

  /**
   * Registers or replaces a named socket filter.
   *
   * @param id stable filter id; must not be null or blank
   * @param filter filter to invoke for dragged nodes; must not be null
   */
  public static void registerFilter(String id, Predicate<CanvasNode> filter) {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(filter, "filter");
    if (id.isBlank()) {
      throw new IllegalArgumentException("filter id must not be blank");
    }
    FILTERS.put(id, filter);
  }

  /**
   * Registers and selects a named filter.
   *
   * @param id stable filter id
   * @param value filter to invoke for dragged nodes
   * @return this node for chaining
   */
  public SocketNode filter(String id, Predicate<CanvasNode> value) {
    registerFilter(id, value);
    return filter(id);
  }

  /**
   * Uses a previously registered filter.
   *
   * @param id the registered filter id
   * @return this node for chaining
   * @throws IllegalArgumentException if no filter is registered under the id
   */
  public SocketNode filter(String id) {
    Predicate<CanvasNode> registered = FILTERS.get(id);
    if (registered == null) {
      throw new IllegalArgumentException("No SocketNode filter registered for '" + id + "'");
    }
    this.filterId = id;
    this.filter = registered;
    notifyStateChanged();
    return this;
  }

  /**
   * Uses a runtime-only filter.
   *
   * <p>The predicate itself cannot be serialized. Use {@link #registerFilter(String, Predicate)}
   * and {@link #filter(String)} when this node is reconstructed from a canvas definition or saved
   * state.
   *
   * @param value the filter to use
   * @return this node for chaining
   */
  public SocketNode filter(Predicate<CanvasNode> value) {
    this.filterId = null;
    this.filter = Objects.requireNonNull(value, "value");
    notifyStateChanged();
    return this;
  }

  /**
   * Registers or replaces a named node-added callback.
   *
   * @param id stable callback id; must not be null or blank
   * @param callback callback invoked after a node enters a socket; must not be null
   */
  public static void registerNodeAddedCallback(
      String id, TriConsumer<SocketNode, Integer, CanvasNode> callback) {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(callback, "callback");
    if (id.isBlank()) {
      throw new IllegalArgumentException("callback id must not be blank");
    }
    NODE_ADDED_CALLBACKS.put(id, callback);
  }

  /**
   * Registers and selects a named node-added callback.
   *
   * @param id stable callback id
   * @param callback callback invoked after a node enters a socket
   * @return this node for chaining
   */
  public SocketNode onNodeAdded(String id, TriConsumer<SocketNode, Integer, CanvasNode> callback) {
    registerNodeAddedCallback(id, callback);
    return onNodeAdded(id);
  }

  /**
   * Uses a previously registered node-added callback.
   *
   * @param id the registered callback id
   * @return this node for chaining
   * @throws IllegalArgumentException if no callback is registered under the id
   */
  public SocketNode onNodeAdded(String id) {
    TriConsumer<SocketNode, Integer, CanvasNode> registered = NODE_ADDED_CALLBACKS.get(id);
    if (registered == null) {
      throw new IllegalArgumentException(
          "No SocketNode node-added callback registered for '" + id + "'");
    }
    nodeAddedCallbackId = id;
    nodeAddedCallback = registered;
    notifyStateChanged();
    return this;
  }

  /**
   * Uses a runtime-only node-added callback.
   *
   * <p>Use a named callback when this node must retain the callback after reconstruction.
   *
   * @param callback callback invoked after a node enters a socket
   * @return this node for chaining
   */
  public SocketNode onNodeAdded(TriConsumer<SocketNode, Integer, CanvasNode> callback) {
    nodeAddedCallbackId = null;
    nodeAddedCallback = Objects.requireNonNull(callback, "callback");
    notifyStateChanged();
    return this;
  }

  /**
   * Returns the displayed content.
   *
   * @return the rich-label content
   */
  public String content() {
    return content;
  }

  /**
   * Sets the displayed content.
   *
   * @param value the new content
   * @return this node for chaining
   */
  public SocketNode content(String value) {
    this.content = value == null ? id() : value;
    if (label != null) {
      label.setText(this.content);
    }
    notifyStateChanged();
    return this;
  }

  /**
   * Returns the number of available sockets.
   *
   * @return the socket count
   */
  public int socketCount() {
    return socketCount;
  }

  /**
   * Returns the node occupying a socket.
   *
   * @param index zero-based socket index
   * @return the occupying node, if the socket is filled
   */
  public Optional<CanvasNode> socket(int index) {
    checkSocketIndex(index);
    SocketEntry entry = sockets[index];
    return entry == null ? Optional.empty() : Optional.of(entry.node());
  }

  /**
   * Returns all currently socketed nodes in socket order.
   *
   * @return the socketed nodes
   */
  public List<CanvasNode> socketedNodes() {
    return Arrays.stream(sockets).filter(Objects::nonNull).map(SocketEntry::node).toList();
  }

  /**
   * Returns whether a socket is locked against removal.
   *
   * @param index zero-based socket index
   * @return true if an occupying node cannot be dragged out
   */
  public boolean socketLocked(int index) {
    checkSocketIndex(index);
    return lockedSockets[index];
  }

  /**
   * Locks or unlocks a socket against removal.
   *
   * <p>Locking an empty socket does not prevent it from being filled.
   *
   * @param index zero-based socket index
   * @param locked whether the socket should be locked
   * @return this node for chaining
   */
  public SocketNode lockSocket(int index, boolean locked) {
    checkSocketIndex(index);
    lockedSockets[index] = locked;
    notifyStateChanged();
    return this;
  }

  /**
   * Returns whether the node can currently be accepted into an empty socket.
   *
   * @param node the candidate node
   * @return true if the filter accepts the node and a socket is empty
   */
  public boolean accepts(CanvasNode node) {
    return node != null && node != this && nearestEmptySocket(node) >= 0 && filter.test(node);
  }

  /**
   * Moves a canvas-owned node into the nearest empty socket.
   *
   * @param node the node to adopt
   * @return true if the node was accepted and socketed
   */
  public boolean trySocket(CanvasNode node) {
    if (!accepts(node)) {
      return false;
    }
    return trySocket(nearestEmptySocket(node), node);
  }

  /**
   * Moves a canvas-owned node into a specific empty socket.
   *
   * @param index zero-based socket index
   * @param node the node to adopt
   * @return true if the node was accepted and socketed
   */
  public boolean trySocket(int index, CanvasNode node) {
    checkSocketIndex(index);
    CanvasArea area = canvas();
    if (area == null
        || node == null
        || node.canvas() != area
        || sockets[index] != null
        || node == this
        || !filter.test(node)) {
      return false;
    }
    NodeState unsocketedState = node.toState();
    if (!area.transferNodeToOwner(node)) {
      return false;
    }
    installNode(index, node, unsocketedState);
    return true;
  }

  /**
   * Places a node into a specific socket during setup.
   *
   * <p>This method can be used before the socket node is added to a canvas to define initially
   * filled or forced slots. It intentionally bypasses the interaction filter.
   *
   * @param index zero-based socket index
   * @param node the node to place
   * @return this node for chaining
   */
  public SocketNode socket(int index, CanvasNode node) {
    checkSocketIndex(index);
    Objects.requireNonNull(node, "node");
    if (node == this || sockets[index] != null) {
      throw new IllegalArgumentException("Socket " + index + " is not available");
    }
    CanvasArea area = canvas();
    if (node.canvas() != null && node.canvas() != area) {
      throw new IllegalArgumentException("Node belongs to a different canvas");
    }
    NodeState unsocketedState = node.toState();
    if (node.canvas() == area && area != null && !area.transferNodeToOwner(node)) {
      throw new IllegalArgumentException("Node is not a top-level member of this canvas");
    }
    if (node.getParent() != null) {
      throw new IllegalArgumentException("Node is already owned by another actor");
    }
    installNode(index, node, unsocketedState);
    attachOwnedNode(node, area);
    return this;
  }

  @Override
  public String typeId() {
    return TYPE;
  }

  @Override
  public void onDragEnter(CanvasDragContext context) {
    highlightedSocket = accepts(context) ? nearestEmptySocket(context.draggedNode()) : -1;
  }

  @Override
  public void onDragOver(CanvasDragContext context) {
    highlightedSocket = accepts(context) ? nearestEmptySocket(context.draggedNode()) : -1;
  }

  @Override
  public void onDragExit(CanvasDragContext context) {
    highlightedSocket = -1;
  }

  @Override
  public boolean onNodeDropped(CanvasDragContext context) {
    int targetSocket = accepts(context) ? nearestEmptySocket(context.draggedNode()) : -1;
    boolean accepted = targetSocket >= 0 && trySocket(targetSocket, context.draggedNode());
    highlightedSocket = -1;
    return accepted;
  }

  private boolean accepts(CanvasDragContext context) {
    return context.draggedNodes().size() == 1 && accepts(context.draggedNode());
  }

  @Override
  public CanvasNode releaseOwnedNodeForDrag(CanvasNode node) {
    int index = indexOf(node);
    CanvasArea area = canvas();
    if (index < 0 || area == null || lockedSockets[index] || !node.movable()) {
      return null;
    }
    SocketEntry entry = sockets[index];
    Vector2 areaCenter =
        node.localToActorCoordinates(area, new Vector2(node.width() / 2f, node.height() / 2f));
    Vector2 positionCenter =
        node.sticky() ? areaCenter : area.areaToWorld(areaCenter.x, areaCenter.y);

    removeActor(node);
    sockets[index] = null;
    node.size(entry.originalWidth(), entry.originalHeight());
    node.position(positionCenter.x - node.width() / 2f, positionCenter.y - node.height() / 2f);
    area.addNode(node);
    invalidateLayout();
    notifyStateChanged();
    return node;
  }

  @Override
  protected void buildContent() {
    label =
        new RichLabel(
            content, DialogDesign.DIALOG_FONT_SPEC_NORMAL.withSize(18).withColor(Color.WHITE));
    label.setAlignment(Align.center);
    label.setWrap(true);
    addActorAt(0, label);
  }

  @Override
  protected void layoutContent() {
    super.layoutContent();
    if (label != null) {
      label.setBounds(4f, GROUP_LINE_TOP, BOX_WIDTH - 8f, BOX_HEIGHT);
    }
    for (int i = 0; i < sockets.length; i++) {
      SocketEntry entry = sockets[i];
      if (entry != null) {
        layoutSocket(i, entry);
      }
    }
  }

  @Override
  protected void drawBackground(Batch batch, float parentAlpha) {
    float x = getX();
    float y = getY() + GROUP_LINE_TOP;
    CanvasGraphics.fill(batch, BOX_COLOR, parentAlpha, x, y, BOX_WIDTH, BOX_HEIGHT);
    CanvasGraphics.outline(batch, BORDER, parentAlpha, x, y, BOX_WIDTH, BOX_HEIGHT, 2f);

    for (int i = 0; i < sockets.length; i++) {
      if (sockets[i] != null) {
        continue;
      }
      float socketX = x + boxX(i + 1);
      CanvasGraphics.fill(batch, EMPTY_COLOR, parentAlpha, socketX, y, BOX_WIDTH, BOX_HEIGHT);
      Color outline = i == highlightedSocket ? HIGHLIGHT : BORDER;
      float thickness = i == highlightedSocket ? 4f : 2f;
      CanvasGraphics.outline(
          batch, outline, parentAlpha, socketX, y, BOX_WIDTH, BOX_HEIGHT, thickness);
    }

    float firstCenter = x + BOX_WIDTH / 2f;
    float lastCenter = x + boxX(socketCount) + BOX_WIDTH / 2f;
    CanvasGraphics.fill(
        batch,
        BORDER,
        parentAlpha,
        firstCenter,
        getY() + GROUP_LINE_Y,
        lastCenter - firstCenter,
        GROUP_LINE_THICKNESS);
    for (int i = 0; i <= socketCount; i++) {
      float center = x + boxX(i) + BOX_WIDTH / 2f;
      CanvasGraphics.fill(
          batch,
          BORDER,
          parentAlpha,
          center,
          getY() + GROUP_LINE_Y,
          GROUP_LINE_THICKNESS,
          GROUP_LINE_TOP - GROUP_LINE_Y);
    }
  }

  @Override
  protected void writeProps(NodeState.Props props) {
    props.put(PROP_CONTENT, content);
    props.put(PROP_SOCKET_COUNT, socketCount);
    props.put(PROP_FILTER_ID, filterId);
    props.put(PROP_NODE_ADDED_CALLBACK_ID, nodeAddedCallbackId);
    for (int i = 0; i < sockets.length; i++) {
      SocketEntry entry = sockets[i];
      props.put(PROP_LOCKED_PREFIX + i, lockedSockets[i]);
      if (entry == null) {
        continue;
      }
      NodeState state =
          entry.node().toState().withSize(entry.originalWidth(), entry.originalHeight());
      byte[] encoded = SNAPSHOT_CODEC.encode(new CanvasSnapshot(List.of(state)));
      props.put(PROP_SOCKET_PREFIX + i, Base64.getEncoder().encodeToString(encoded));
    }
  }

  @Override
  protected void readProps(NodeState state) {
    content = state.prop(PROP_CONTENT, id());
    if (label != null) {
      label.setText(content);
    }
    for (SocketEntry entry : sockets) {
      if (entry != null) {
        removeActor(entry.node());
      }
    }
    socketCount = Math.max(0, state.intProp(PROP_SOCKET_COUNT, socketCount));
    sockets = new SocketEntry[socketCount];
    lockedSockets = new boolean[socketCount];

    filterId = state.prop(PROP_FILTER_ID, null);
    Predicate<CanvasNode> registered = filterId == null ? null : FILTERS.get(filterId);
    filter = filterId == null ? ACCEPT_ALL : registered == null ? REJECT_ALL : registered;
    if (filterId != null && registered == null) {
      LOGGER.warn(
          "Socket filter '{}' of node '{}' is not registered; rejecting all nodes", filterId, id());
    }

    nodeAddedCallbackId = state.prop(PROP_NODE_ADDED_CALLBACK_ID, null);
    TriConsumer<SocketNode, Integer, CanvasNode> registeredCallback =
        nodeAddedCallbackId == null ? null : NODE_ADDED_CALLBACKS.get(nodeAddedCallbackId);
    nodeAddedCallback =
        nodeAddedCallbackId == null
            ? NO_NODE_ADDED_CALLBACK
            : registeredCallback == null ? NO_NODE_ADDED_CALLBACK : registeredCallback;
    if (nodeAddedCallbackId != null && registeredCallback == null) {
      LOGGER.warn(
          "Node-added callback '{}' of node '{}' is not registered; callback will be inert",
          nodeAddedCallbackId,
          id());
    }

    for (int i = 0; i < socketCount; i++) {
      lockedSockets[i] = state.boolProp(PROP_LOCKED_PREFIX + i, false);
      String encoded = state.prop(PROP_SOCKET_PREFIX + i, null);
      if (encoded == null) {
        continue;
      }
      try {
        CanvasSnapshot snapshot = SNAPSHOT_CODEC.decode(Base64.getDecoder().decode(encoded));
        if (snapshot.size() != 1) {
          LOGGER.warn("Socket {} of node '{}' contains an invalid node snapshot", i, id());
          continue;
        }
        NodeState childState = snapshot.nodes().getFirst();
        CanvasNode child = CanvasNodeType.create(childState);
        sockets[i] = new SocketEntry(child, childState.width(), childState.height());
        addActor(child);
      } catch (IllegalArgumentException | IllegalStateException exception) {
        LOGGER.warn(
            "Could not restore socket {} of node '{}': {}", i, id(), exception.getMessage());
      }
    }
    invalidateLayout();
  }

  @Override
  protected void onCanvasChanged(CanvasArea area) {
    for (SocketEntry entry : sockets) {
      if (entry != null) {
        attachOwnedNode(entry.node(), area);
      }
    }
  }

  private int indexOf(CanvasNode node) {
    for (int i = 0; i < sockets.length; i++) {
      SocketEntry entry = sockets[i];
      if (entry != null && entry.node() == node) {
        return i;
      }
    }
    return -1;
  }

  private int nearestEmptySocket(CanvasNode node) {
    int nearest = -1;
    float nearestDistance = Float.POSITIVE_INFINITY;
    for (int i = 0; i < sockets.length; i++) {
      if (sockets[i] != null) {
        continue;
      }
      float dx = node.centerX() - (x() + boxX(i + 1) + BOX_WIDTH / 2f);
      float dy = node.centerY() - (y() + GROUP_LINE_TOP + BOX_HEIGHT / 2f);
      float distance = dx * dx + dy * dy;
      if (distance < nearestDistance) {
        nearest = i;
        nearestDistance = distance;
      }
    }
    return nearest;
  }

  private void installNode(int index, CanvasNode node, NodeState unsocketedState) {
    SocketEntry entry = new SocketEntry(node, unsocketedState.width(), unsocketedState.height());
    sockets[index] = entry;
    addActor(node);
    layoutSocket(index, entry);
    highlightedSocket = -1;
    invalidateLayout();
    notifyStateChanged();
    nodeAddedCallback.accept(this, index, node);
  }

  private void checkSocketIndex(int index) {
    if (index < 0 || index >= sockets.length) {
      throw new IndexOutOfBoundsException(index);
    }
  }

  private void layoutSocket(int index, SocketEntry entry) {
    entry.node().setBounds(boxX(index + 1), GROUP_LINE_TOP, BOX_WIDTH, BOX_HEIGHT);
  }

  private static float preferredWidth(int socketCount) {
    if (socketCount < 0) {
      throw new IllegalArgumentException("socketCount must not be negative");
    }
    return (socketCount + 1) * BOX_WIDTH + socketCount * BOX_SPACING;
  }

  private static float boxX(int index) {
    return index * (BOX_WIDTH + BOX_SPACING);
  }

  private record SocketEntry(CanvasNode node, float originalWidth, float originalHeight) {}
}
