package feature.canvas.nodes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import engine.utils.QuadConsumer;
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
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * A labeled node with a fixed number of slots that can adopt other canvas nodes.
 *
 * <p>Empty sockets are rendered as placeholder label-node boxes to the right of the socket node's
 * own content. Accepted nodes are removed from the canvas and rendered in those positions as owned
 * children. Dragging an occupied node releases it back to the canvas at the same visual position.
 *
 * <p>Filters and socket callbacks are attached directly to node prototypes supplied by the canvas
 * definition. They are runtime behavior and are not written into serialized node state.
 */
public class SocketNode extends CanvasNode {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(SocketNode.class);

  /** Stable type id of this node type. */
  public static final String TYPE = "canvas.socket";

  /** Prop key holding the displayed content. */
  public static final String PROP_CONTENT = "content";

  /** Prop key holding the number of sockets. */
  public static final String PROP_SOCKET_COUNT = "socketCount";

  private static final String PROP_SOCKET_PREFIX = "socket.";
  private static final String PROP_LOCKED_PREFIX = "locked.";
  private static final Predicate<CanvasNode> ACCEPT_ALL = node -> true;
  private static final QuadConsumer<SocketNode, Integer, CanvasNode, Boolean>
      NO_SOCKET_CHANGED_CALLBACK = (socket, index, node, added) -> {};
  private static final CanvasSnapshotCodec SNAPSHOT_CODEC = new CanvasSnapshotCodec();

  private static final float BOX_WIDTH = 160f;
  private static final float BOX_HEIGHT = 64f;
  private static final float BOX_SPACING = 12f;
  private static final float GROUP_LINE_Y = 3f;
  private static final float GROUP_LINE_TOP = 10f;
  private static final float GROUP_LINE_THICKNESS = 2f;

  private static final Color DEFAULT_COLOR = LabelNode.DEFAULT_COLOR;
  private static final Color EMPTY_COLOR = new Color(0.20f, 0.28f, 0.42f, 0.35f);
  private static final Color BORDER = new Color(0.85f, 0.88f, 0.95f, 1f);
  private static final Color HIGHLIGHT = new Color(0.25f, 0.82f, 0.38f, 1f);

  private String content;
  private int socketCount;
  private Predicate<CanvasNode> filter = ACCEPT_ALL;
  private QuadConsumer<SocketNode, Integer, CanvasNode, Boolean> socketChangedCallback =
      NO_SOCKET_CHANGED_CALLBACK;
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
    this.color(DEFAULT_COLOR);
    resizeToSockets();
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
   * Uses the given filter for nodes dragged over this socket node.
   *
   * @param value the filter to apply
   * @return this node for chaining
   */
  public SocketNode filter(Predicate<CanvasNode> value) {
    this.filter = Objects.requireNonNull(value, "value");
    notifyStateChanged();
    return this;
  }

  /**
   * Uses the given callback after a node enters or leaves a socket.
   *
   * @param callback the callback to invoke
   * @return this node for chaining
   */
  public SocketNode onSocketChanged(
      QuadConsumer<SocketNode, Integer, CanvasNode, Boolean> callback) {
    socketChangedCallback = Objects.requireNonNull(callback, "callback");
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
    resizeToSockets();
    invalidateLayout();
    notifyStateChanged();
    socketChangedCallback.accept(this, index, node, false);
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
    for (int i = 0; i < sockets.length; i++) {
      SocketEntry entry = sockets[i];
      if (entry != null
          && entry.node() instanceof LabelNode labelNode
          && labelNode.autoSizeIfContentUnbuilt()) {
        sockets[i] = new SocketEntry(labelNode, labelNode.width(), labelNode.height());
      }
    }
    resizeToSockets();
    super.layoutContent();
    if (label != null) {
      label.setBounds(4f, GROUP_LINE_TOP, BOX_WIDTH - 8f, rowHeight());
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
    float rowHeight = rowHeight();
    CanvasGraphics.fill(batch, color(), parentAlpha, x, y, BOX_WIDTH, rowHeight);
    CanvasGraphics.outline(batch, BORDER, parentAlpha, x, y, BOX_WIDTH, rowHeight, 2f);

    for (int i = 0; i < sockets.length; i++) {
      if (sockets[i] != null) {
        continue;
      }
      float socketX = x + boxX(i + 1);
      float socketWidth = slotWidth(i);
      CanvasGraphics.fill(batch, EMPTY_COLOR, parentAlpha, socketX, y, socketWidth, rowHeight);
      Color outline = i == highlightedSocket ? HIGHLIGHT : BORDER;
      float thickness = i == highlightedSocket ? 4f : 2f;
      CanvasGraphics.outline(
          batch, outline, parentAlpha, socketX, y, socketWidth, rowHeight, thickness);
    }

    float firstCenter = x + BOX_WIDTH / 2f;
    float lastCenter =
        socketCount == 0 ? firstCenter : x + boxX(socketCount) + slotWidth(socketCount - 1) / 2f;
    CanvasGraphics.fill(
        batch,
        BORDER,
        parentAlpha,
        firstCenter,
        getY() + GROUP_LINE_Y,
        lastCenter - firstCenter,
        GROUP_LINE_THICKNESS);
    for (int i = 0; i <= socketCount; i++) {
      float boxWidth = i == 0 ? BOX_WIDTH : slotWidth(i - 1);
      float center = x + boxX(i) + boxWidth / 2f;
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
    SocketEntry[] prototypes = sockets;
    for (SocketEntry entry : prototypes) {
      if (entry != null) {
        removeActor(entry.node());
      }
    }
    socketCount = Math.max(0, state.intProp(PROP_SOCKET_COUNT, socketCount));
    sockets = new SocketEntry[socketCount];
    lockedSockets = new boolean[socketCount];

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
        CanvasNode child = matchingPrototype(prototypes, childState);
        if (child == null) {
          child = CanvasNodeType.create(childState);
        } else {
          child.applyState(childState);
        }
        sockets[i] = new SocketEntry(child, childState.width(), childState.height());
        addActor(child);
      } catch (IllegalArgumentException | IllegalStateException exception) {
        LOGGER.warn(
            "Could not restore socket {} of node '{}': {}", i, id(), exception.getMessage());
      }
    }
    resizeToSockets();
    invalidateLayout();
  }

  private CanvasNode matchingPrototype(SocketEntry[] prototypes, NodeState state) {
    for (SocketEntry prototype : prototypes) {
      if (prototype != null
          && prototype.node().id().equals(state.id())
          && prototype.node().typeId().equals(state.typeId())) {
        return prototype.node();
      }
    }
    return null;
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
      float dx = node.centerX() - (x() + boxX(i + 1) + slotWidth(i) / 2f);
      float dy = node.centerY() - (y() + GROUP_LINE_TOP + rowHeight() / 2f);
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
    resizeToSockets();
    layoutSocket(index, entry);
    highlightedSocket = -1;
    invalidateLayout();
    notifyStateChanged();
    socketChangedCallback.accept(this, index, node, true);
  }

  private void checkSocketIndex(int index) {
    if (index < 0 || index >= sockets.length) {
      throw new IndexOutOfBoundsException(index);
    }
  }

  private void layoutSocket(int index, SocketEntry entry) {
    float y = GROUP_LINE_TOP + (rowHeight() - entry.node().height()) / 2f;
    entry.node().position(boxX(index + 1), y);
  }

  private void resizeToSockets() {
    float width = BOX_WIDTH;
    for (int i = 0; i < sockets.length; i++) {
      width += BOX_SPACING + slotWidth(i);
    }
    size(width, GROUP_LINE_TOP + rowHeight());
  }

  private float rowHeight() {
    float height = BOX_HEIGHT;
    for (SocketEntry entry : sockets) {
      if (entry != null) {
        height = Math.max(height, entry.node().height());
      }
    }
    return height;
  }

  private float slotWidth(int index) {
    SocketEntry entry = sockets[index];
    return entry == null ? BOX_WIDTH : entry.node().width();
  }

  private static float preferredWidth(int socketCount) {
    if (socketCount < 0) {
      throw new IllegalArgumentException("socketCount must not be negative");
    }
    return (socketCount + 1) * BOX_WIDTH + socketCount * BOX_SPACING;
  }

  private float boxX(int index) {
    if (index == 0) {
      return 0f;
    }
    float x = BOX_WIDTH + BOX_SPACING;
    for (int i = 0; i < index - 1; i++) {
      x += slotWidth(i) + BOX_SPACING;
    }
    return x;
  }

  private record SocketEntry(CanvasNode node, float originalWidth, float originalHeight) {}
}
