package feature.canvas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.utils.logging.DungeonLogger;
import feature.hud.dialogs.DialogCallbackResolver;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * A pannable, zoomable and clipped viewport that hosts arbitrary {@link CanvasNode}s.
 *
 * <p>The area separates two coordinate systems:
 *
 * <ul>
 *   <li><b>world</b> coordinates, in which nodes are positioned and sized. These are what {@link
 *       CanvasNode#x()}, {@link CanvasNode#bounds()} and all queries operate on, and they are
 *       independent of the current view.
 *   <li><b>area</b> coordinates, the on screen pixels of the viewport. The current pan offset and
 *       zoom factor map between them, see {@link #worldToArea(float, float)} and {@link
 *       #areaToWorld(float, float)}.
 * </ul>
 *
 * <p>Nodes live in an inner transformed group, which gives them zooming for free; anything drawn
 * outside the viewport bounds is clipped away.
 *
 * <h2>Interaction</h2>
 *
 * <ul>
 *   <li>mouse wheel zooms, anchored at the cursor so the world point under the cursor stays put
 *   <li>the configured pan button (middle mouse by default) drags the view
 *   <li>left dragging a node moves it, left dragging empty space rubber band selects
 *   <li>{@code Delete} removes the selection, arrow keys nudge it
 * </ul>
 *
 * <p>Interactions of individual nodes take precedence: if a child actor of a node (a button for
 * example) already handled a touch, the canvas does not start a drag.
 */
public class CanvasArea extends Group {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(CanvasArea.class);

  /** Prefix used for ids of nodes the player created locally. */
  public static final String LOCAL_ID_PREFIX = "local:";

  private final String canvasId;
  private final CanvasOptions options;
  private final Group world = new Group();
  private final Group sticky = new Group();
  private final Map<String, CanvasNode> nodesById = new LinkedHashMap<>();
  private final Set<CanvasNode> selection = new LinkedHashSet<>();
  private final List<CanvasListener> listeners = new ArrayList<>();

  private String dialogId;

  private float panX;
  private float panY;
  private float zoom;
  private boolean orderDirty;
  private float viewportWidth;
  private float viewportHeight;

  private DragMode dragMode = DragMode.NONE;
  private int dragPointerButton = -1;
  private final Vector2 lastPointerWorld = new Vector2();
  private final Vector2 lastPointerArea = new Vector2();
  private final Vector2 rubberStart = new Vector2();
  private final Vector2 rubberEnd = new Vector2();
  private CanvasNode draggedNode;
  private List<CanvasNode> draggedGroup = List.of();
  private CanvasNode draggedNodeOwner;
  private final Set<CanvasNode> dragTargets = new LinkedHashSet<>();
  private boolean dragMoved;
  private final Vector2 pressArea = new Vector2();

  /** Pointer movement in pixels below which a press is still treated as a click, not a drag. */
  private static final float CLICK_SLOP = 3f;

  private enum DragMode {
    NONE,
    PAN,
    NODE,
    RUBBER_BAND
  }

  /**
   * Creates a canvas area.
   *
   * @param canvasId the id of the canvas this area belongs to; used for overlay persistence
   * @param width the viewport width in pixels
   * @param height the viewport height in pixels
   * @param options the canvas configuration; must not be null
   */
  public CanvasArea(String canvasId, float width, float height, CanvasOptions options) {
    this.canvasId = Objects.requireNonNull(canvasId, "canvasId");
    this.options = Objects.requireNonNull(options, "options");
    this.zoom = options.clampZoom(options.initialZoom());

    setSize(width, height);
    setTransform(false);
    setTouchable(Touchable.enabled);

    world.setTransform(true);
    world.setScale(zoom);
    addActor(world);
    sticky.setTransform(false);
    addActor(sticky);

    resetView();
    addListener(new CanvasInputListener());
  }

  @Override
  protected void sizeChanged() {
    super.sizeChanged();
    panX += (getWidth() - viewportWidth) / 2f;
    panY += (getHeight() - viewportHeight) / 2f;
    viewportWidth = getWidth();
    viewportHeight = getHeight();
    applyViewTransform();
  }

  /**
   * Returns the id of the canvas this area belongs to.
   *
   * @return the canvas id
   */
  public String canvasId() {
    return canvasId;
  }

  /**
   * Returns the configuration of this canvas.
   *
   * @return the canvas options
   */
  public CanvasOptions options() {
    return options;
  }

  /**
   * Sets the dialog id used to route server events.
   *
   * @param value the dialog id of the surrounding dialog
   */
  public void dialogId(String value) {
    this.dialogId = value;
  }

  /**
   * Returns the dialog id used to route server events.
   *
   * @return the dialog id, or null when the canvas is not embedded in a dialog
   */
  public String dialogId() {
    return dialogId;
  }

  // ---------------------------------------------------------------- node management

  /**
   * Adds a node to the canvas.
   *
   * @param node the node to add; must not be null and must have an id that is free
   * @param <T> the concrete node type
   * @return the added node, for chaining
   * @throws IllegalStateException if a node with the same id is already on this canvas
   */
  public <T extends CanvasNode> T addNode(T node) {
    Objects.requireNonNull(node, "node");
    if (nodesById.containsKey(node.id())) {
      throw new IllegalStateException(
          "A node with id '" + node.id() + "' is already on canvas '" + canvasId + "'");
    }
    nodesById.put(node.id(), node);
    groupOf(node).addActor(node);
    node.attach(this);
    orderDirty = true;
    node.onAdd(this);
    listeners.forEach(l -> l.onNodeAdded(this, node));
    return node;
  }

  /**
   * Adds several nodes to the canvas.
   *
   * @param newNodes the nodes to add
   */
  public void addNodes(Collection<? extends CanvasNode> newNodes) {
    newNodes.forEach(this::addNode);
  }

  /**
   * Removes a node from the canvas.
   *
   * @param node the node to remove
   * @return true if the node was on this canvas and got removed
   */
  public boolean removeNode(CanvasNode node) {
    return removeNode(node, true, true);
  }

  /**
   * Removes a node from top-level canvas ownership while retaining its canvas context.
   *
   * <p>Container nodes use this before re-parenting a node into themselves. The transferred node no
   * longer participates in canvas selection, queries or top-level persistence, but can still use
   * {@link CanvasNode#canvas()} while nested.
   *
   * @param node the node to transfer
   * @return true if the node was transferred out of top-level canvas ownership
   */
  public boolean transferNodeToOwner(CanvasNode node) {
    return removeNode(node, false, false);
  }

  private boolean removeNode(CanvasNode node, boolean detach, boolean cancelDrag) {
    if (node == null || nodesById.get(node.id()) != node) {
      return false;
    }
    nodesById.remove(node.id());
    groupOf(node).removeActor(node);
    if (selection.remove(node)) {
      node.setSelectedInternal(false);
      fireSelectionChanged();
    }
    if (cancelDrag && draggedNode == node) {
      draggedNode = null;
    }
    dragTargets.remove(node);
    if (detach) {
      node.attach(null);
    }
    node.onRemove(this);
    listeners.forEach(l -> l.onNodeRemoved(this, node));
    return true;
  }

  /**
   * Removes the node with the given id.
   *
   * @param nodeId the id of the node to remove
   * @return true if a node was removed
   */
  public boolean removeNode(String nodeId) {
    return removeNode(nodesById.get(nodeId));
  }

  /** Removes every node from the canvas. */
  public void clear() {
    for (CanvasNode node : new ArrayList<>(nodesById.values())) {
      removeNode(node);
    }
  }

  /**
   * Returns all nodes currently on the canvas, in insertion order.
   *
   * @return an unmodifiable view of the nodes
   */
  public List<CanvasNode> nodes() {
    return List.copyOf(nodesById.values());
  }

  /**
   * Looks up a node by id.
   *
   * @param nodeId the id to look up
   * @return the node, if present
   */
  public Optional<CanvasNode> nodeById(String nodeId) {
    return Optional.ofNullable(nodesById.get(nodeId));
  }

  /**
   * Returns the number of nodes on the canvas.
   *
   * @return the node count
   */
  public int nodeCount() {
    return nodesById.size();
  }

  /**
   * Generates an id that is guaranteed not to collide with server provided default node ids.
   *
   * @return a fresh local node id
   */
  public static String newLocalId() {
    return LOCAL_ID_PREFIX + UUID.randomUUID();
  }

  /**
   * Raises a node above all other nodes.
   *
   * @param node the node to raise
   */
  public void bringToFront(CanvasNode node) {
    if (node == null || nodesById.get(node.id()) != node) {
      return;
    }
    int max = sameSpaceAs(node).mapToInt(CanvasNode::z).max().orElse(0);
    if (node.z() < max) {
      node.z(max + 1);
    }
  }

  /** Marks the node render order as outdated; the canvas re-sorts before the next draw. */
  void invalidateOrder() {
    orderDirty = true;
  }

  private Group groupOf(CanvasNode node) {
    return node.sticky() ? sticky : world;
  }

  /**
   * Re-parents a node after its sticky flag changed and converts its coordinates.
   *
   * <p>Called by {@link CanvasNode#sticky(boolean)} only.
   *
   * @param node the node whose sticky flag changed
   */
  void stickyChanged(CanvasNode node) {
    if (nodesById.get(node.id()) != node) {
      return;
    }
    Vector2 converted =
        node.sticky() ? worldToArea(node.x(), node.y()) : areaToWorld(node.x(), node.y());
    world.removeActor(node);
    sticky.removeActor(node);
    node.setPosition(converted.x, converted.y);
    groupOf(node).addActor(node);
    orderDirty = true;
  }

  /**
   * Returns all nodes that share the coordinate space of the given node.
   *
   * @param node the reference node
   * @return the nodes living in the same space, including the reference node itself
   */
  private Stream<CanvasNode> sameSpaceAs(CanvasNode node) {
    return nodesById.values().stream().filter(other -> other.sticky() == node.sticky());
  }

  /**
   * Forwards a node state change to all registered listeners.
   *
   * @param node the node whose state changed
   */
  void onNodeStateChanged(CanvasNode node) {
    listeners.forEach(l -> l.onNodeStateChanged(this, node));
  }

  /**
   * Registers a listener for structural canvas changes.
   *
   * @param listener the listener to add; must not be null
   */
  public void addCanvasListener(CanvasListener listener) {
    listeners.add(Objects.requireNonNull(listener, "listener"));
  }

  /**
   * Removes a previously registered listener.
   *
   * @param listener the listener to remove
   */
  public void removeCanvasListener(CanvasListener listener) {
    listeners.remove(listener);
  }

  // ---------------------------------------------------------------- queries

  /**
   * Returns the node with the highest z value that overlaps the given node.
   *
   * <p>This is the primary building block for drop based puzzle logic: a node can call it from
   * {@link CanvasNode#onDrop(float, float)} to find out what it was dropped onto.
   *
   * @param own the node to test; it is never returned itself
   * @return the topmost overlapping node, if any
   */
  public Optional<CanvasNode> intersectsOther(CanvasNode own) {
    return intersectsAll(own).stream().findFirst();
  }

  /**
   * Returns all nodes that overlap the given node, topmost first.
   *
   * @param own the node to test; it is never contained in the result
   * @return the overlapping nodes ordered by descending z
   */
  public List<CanvasNode> intersectsAll(CanvasNode own) {
    if (own == null) {
      return List.of();
    }
    Rectangle ownBounds = own.bounds();
    return sameSpaceAs(own)
        .filter(other -> other != own)
        .filter(other -> other.bounds().overlaps(ownBounds))
        .sorted(Comparator.comparingInt((CanvasNode n) -> n.z()).reversed())
        .toList();
  }

  /**
   * Returns the topmost node containing the given world point.
   *
   * @param worldX the world x coordinate
   * @param worldY the world y coordinate
   * @return the topmost node at that point, if any
   */
  public Optional<CanvasNode> nodeAt(float worldX, float worldY) {
    return nodesById.values().stream()
        .filter(node -> !node.sticky())
        .filter(node -> node.bounds().contains(worldX, worldY))
        .max(Comparator.comparingInt(CanvasNode::z));
  }

  /**
   * Returns all nodes that overlap the given world space rectangle.
   *
   * @param worldRect the rectangle in world coordinates; must not be null
   * @return the overlapping nodes ordered by descending z
   */
  public List<CanvasNode> nodesInRect(Rectangle worldRect) {
    Objects.requireNonNull(worldRect, "worldRect");
    return nodesById.values().stream()
        .filter(node -> !node.sticky())
        .filter(node -> node.bounds().overlaps(worldRect))
        .sorted(Comparator.comparingInt((CanvasNode n) -> n.z()).reversed())
        .toList();
  }

  /**
   * Returns the node whose center is closest to the center of the given node.
   *
   * @param own the reference node; it is never returned itself
   * @param maxDistance the maximum center distance in world units
   * @return the closest node within the distance, if any
   */
  public Optional<CanvasNode> nearestNode(CanvasNode own, float maxDistance) {
    if (own == null) {
      return Optional.empty();
    }
    return sameSpaceAs(own)
        .filter(other -> other != own)
        .filter(other -> distance(own, other) <= maxDistance)
        .min(Comparator.comparingDouble(other -> distance(own, other)));
  }

  private static float distance(CanvasNode a, CanvasNode b) {
    float dx = a.centerX() - b.centerX();
    float dy = a.centerY() - b.centerY();
    return (float) Math.sqrt(dx * dx + dy * dy);
  }

  // ---------------------------------------------------------------- selection

  /**
   * Returns the currently selected nodes.
   *
   * @return an unmodifiable view of the selection
   */
  public Set<CanvasNode> selection() {
    return Collections.unmodifiableSet(selection);
  }

  /**
   * Replaces the selection with a single node.
   *
   * @param node the node to select, or null to clear the selection
   */
  public void select(CanvasNode node) {
    if (node == null) {
      clearSelection();
      return;
    }
    select(List.of(node), false);
  }

  /**
   * Selects the given nodes.
   *
   * @param toSelect the nodes to select; nodes that are not selectable are ignored
   * @param additive true to keep the current selection, false to replace it
   */
  public void select(Collection<CanvasNode> toSelect, boolean additive) {
    if (!options.selectionEnabled()) {
      return;
    }
    Set<CanvasNode> before = new LinkedHashSet<>(selection);
    if (!additive || !options.multiSelectEnabled()) {
      selection.forEach(node -> node.setSelectedInternal(false));
      selection.clear();
    }
    for (CanvasNode node : toSelect) {
      if (!node.selectable() || nodesById.get(node.id()) != node) {
        continue;
      }
      if (!options.multiSelectEnabled() && !selection.isEmpty()) {
        break;
      }
      if (selection.add(node)) {
        node.setSelectedInternal(true);
      }
    }
    if (!before.equals(selection)) {
      fireSelectionChanged();
    }
  }

  /** Clears the current selection. */
  public void clearSelection() {
    if (selection.isEmpty()) {
      return;
    }
    selection.forEach(node -> node.setSelectedInternal(false));
    selection.clear();
    fireSelectionChanged();
  }

  private void fireSelectionChanged() {
    Set<CanvasNode> snapshot = Set.copyOf(selection);
    listeners.forEach(l -> l.onSelectionChanged(this, snapshot));
  }

  /**
   * Removes every deletable node of the current selection.
   *
   * @return the number of removed nodes
   */
  public int deleteSelection() {
    List<CanvasNode> deletable = selection.stream().filter(CanvasNode::deletable).toList();
    deletable.forEach(this::removeNode);
    return deletable.size();
  }

  // ---------------------------------------------------------------- view

  /**
   * Returns the current zoom factor.
   *
   * @return the zoom factor
   */
  public float zoom() {
    return zoom;
  }

  /**
   * Sets the zoom factor, keeping the center of the viewport fixed.
   *
   * @param value the new zoom factor; clamped into the configured range
   */
  public void zoom(float value) {
    zoomAt(value, getWidth() / 2f, getHeight() / 2f);
  }

  /**
   * Sets the zoom factor while keeping the world point under the given area position fixed.
   *
   * @param value the new zoom factor; clamped into the configured range
   * @param areaX the anchor x coordinate inside the viewport
   * @param areaY the anchor y coordinate inside the viewport
   */
  public void zoomAt(float value, float areaX, float areaY) {
    float clamped = options.clampZoom(value);
    if (clamped == zoom) {
      return;
    }
    Vector2 anchor = areaToWorld(areaX, areaY);
    zoom = clamped;
    panX = areaX - anchor.x * zoom;
    panY = areaY - anchor.y * zoom;
    applyViewTransform();
  }

  /**
   * Returns the current pan offset in area coordinates.
   *
   * @return a new vector holding the pan offset
   */
  public Vector2 pan() {
    return new Vector2(panX, panY);
  }

  /**
   * Sets the pan offset.
   *
   * @param x the new pan offset along x
   * @param y the new pan offset along y
   */
  public void pan(float x, float y) {
    this.panX = x;
    this.panY = y;
    applyViewTransform();
  }

  /**
   * Moves the view by the given amount of pixels.
   *
   * @param dx the movement along x
   * @param dy the movement along y
   */
  public void panBy(float dx, float dy) {
    pan(panX + dx, panY + dy);
  }

  /** Resets zoom to the configured initial value and centers the content. */
  public void resetView() {
    zoom = options.clampZoom(options.initialZoom());
    Rectangle content = contentBounds();
    if (content == null) {
      panX = getWidth() / 2f;
      panY = getHeight() / 2f;
    } else {
      panX = getWidth() / 2f - (content.x + content.width / 2f) * zoom;
      panY = getHeight() / 2f - (content.y + content.height / 2f) * zoom;
    }
    applyViewTransform();
  }

  /** Zooms and pans so that all nodes fit into the viewport. */
  public void zoomToFit() {
    Rectangle content = contentBounds();
    if (content == null || content.width <= 0f || content.height <= 0f) {
      resetView();
      return;
    }
    float margin = 0.9f;
    float fit = Math.min(getWidth() / content.width, getHeight() / content.height) * margin;
    zoom = options.clampZoom(fit);
    panX = getWidth() / 2f - (content.x + content.width / 2f) * zoom;
    panY = getHeight() / 2f - (content.y + content.height / 2f) * zoom;
    applyViewTransform();
  }

  /**
   * Returns the world space bounding box of all nodes.
   *
   * @return the bounding box, or null when the canvas is empty
   */
  public Rectangle contentBounds() {
    if (nodesById.isEmpty()) {
      return null;
    }
    float minX = Float.MAX_VALUE;
    float minY = Float.MAX_VALUE;
    float maxX = -Float.MAX_VALUE;
    float maxY = -Float.MAX_VALUE;
    for (CanvasNode node : nodesById.values()) {
      if (node.sticky()) {
        continue;
      }
      minX = Math.min(minX, node.x());
      minY = Math.min(minY, node.y());
      maxX = Math.max(maxX, node.x() + node.width());
      maxY = Math.max(maxY, node.y() + node.height());
    }
    return new Rectangle(minX, minY, maxX - minX, maxY - minY);
  }

  private void applyViewTransform() {
    world.setScale(zoom);
    world.setPosition(panX, panY);
  }

  /**
   * Converts world coordinates into area coordinates.
   *
   * @param worldX the world x coordinate
   * @param worldY the world y coordinate
   * @return a new vector holding the area coordinates
   */
  public Vector2 worldToArea(float worldX, float worldY) {
    return new Vector2(worldX * zoom + panX, worldY * zoom + panY);
  }

  /**
   * Converts area coordinates into world coordinates.
   *
   * @param areaX the area x coordinate
   * @param areaY the area y coordinate
   * @return a new vector holding the world coordinates
   */
  public Vector2 areaToWorld(float areaX, float areaY) {
    return new Vector2((areaX - panX) / zoom, (areaY - panY) / zoom);
  }

  /**
   * Snaps a world coordinate value to the configured grid.
   *
   * @param value the value to snap
   * @return the snapped value, or the original value when snapping is disabled
   */
  public float snap(float value) {
    if (!options.snapToGrid()) {
      return value;
    }
    float size = options.gridSize();
    return Math.round(value / size) * size;
  }

  // ---------------------------------------------------------------- server events

  /**
   * Sends an event to the server.
   *
   * <p>On a network client this sends a dialog response message; in single player the registered
   * callback is executed directly. The key must match a handler registered through {@link
   * CanvasMaker.Builder#onEvent(String, java.util.function.Consumer)}.
   *
   * @param key the event key; must not be null
   * @param payload optional payload, may be null
   */
  public void fireServerEvent(String key, DialogResponseMessage.Payload payload) {
    Objects.requireNonNull(key, "key");
    if (dialogId == null) {
      LOGGER.warn("Canvas '{}' fired event '{}' without a dialog id; ignoring", canvasId, key);
      return;
    }
    DialogCallbackResolver.createButtonCallback(dialogId, key).accept(payload);
  }

  /**
   * Sends an event without a payload to the server.
   *
   * @param key the event key; must not be null
   */
  public void fireServerEvent(String key) {
    fireServerEvent(key, null);
  }

  // ---------------------------------------------------------------- rendering

  @Override
  public void draw(Batch batch, float parentAlpha) {
    if (orderDirty) {
      orderDirty = false;
      world.getChildren().sort(NODE_ORDER);
      sticky.getChildren().sort(NODE_ORDER);
    }

    CanvasGraphics.fill(
        batch, options.backgroundColor(), parentAlpha, getX(), getY(), getWidth(), getHeight());

    batch.flush();
    if (!clipBegin()) {
      return;
    }
    try {
      drawGrid(batch, parentAlpha);
      super.draw(batch, parentAlpha);
      drawOverlays(batch, parentAlpha);
    } finally {
      batch.flush();
      clipEnd();
    }
  }

  private static final Comparator<Actor> NODE_ORDER =
      Comparator.comparingInt(actor -> actor instanceof CanvasNode node ? node.z() : 0);

  private void drawGrid(Batch batch, float parentAlpha) {
    if (!options.gridEnabled()) {
      return;
    }
    float step = options.gridSize() * zoom;
    if (step < 4f) {
      return;
    }
    Color color = options.gridColor();
    float originX = getX() + panX % step;
    float originY = getY() + panY % step;
    if (originX > getX()) {
      originX -= step;
    }
    if (originY > getY()) {
      originY -= step;
    }
    for (float x = originX; x <= getX() + getWidth(); x += step) {
      CanvasGraphics.fill(batch, color, parentAlpha, x, getY(), 1f, getHeight());
    }
    for (float y = originY; y <= getY() + getHeight(); y += step) {
      CanvasGraphics.fill(batch, color, parentAlpha, getX(), y, getWidth(), 1f);
    }
  }

  private void drawOverlays(Batch batch, float parentAlpha) {
    for (CanvasNode node : selection) {
      drawNodeOutline(batch, parentAlpha, node, options.selectionColor(), 2f);
    }
    if (dragMode == DragMode.RUBBER_BAND) {
      Rectangle rect = rubberBandRect();
      Vector2 min = worldToArea(rect.x, rect.y);
      CanvasGraphics.fill(
          batch,
          new Color(
              options.selectionColor().r,
              options.selectionColor().g,
              options.selectionColor().b,
              0.15f),
          parentAlpha,
          getX() + min.x,
          getY() + min.y,
          rect.width * zoom,
          rect.height * zoom);
      CanvasGraphics.outline(
          batch,
          options.selectionColor(),
          parentAlpha,
          getX() + min.x,
          getY() + min.y,
          rect.width * zoom,
          rect.height * zoom,
          1f);
    }
  }

  private void drawNodeOutline(
      Batch batch, float parentAlpha, CanvasNode node, Color color, float thickness) {
    Rectangle bounds = node.bounds();
    if (node.sticky()) {
      CanvasGraphics.outline(
          batch,
          color,
          parentAlpha,
          getX() + bounds.x,
          getY() + bounds.y,
          bounds.width,
          bounds.height,
          thickness);
      return;
    }
    drawWorldOutline(batch, parentAlpha, bounds, color, thickness);
  }

  private void drawWorldOutline(
      Batch batch, float parentAlpha, Rectangle worldRect, Color color, float thickness) {
    Vector2 min = worldToArea(worldRect.x, worldRect.y);
    CanvasGraphics.outline(
        batch,
        color,
        parentAlpha,
        getX() + min.x,
        getY() + min.y,
        worldRect.width * zoom,
        worldRect.height * zoom,
        thickness);
  }

  private Rectangle rubberBandRect() {
    float x = Math.min(rubberStart.x, rubberEnd.x);
    float y = Math.min(rubberStart.y, rubberEnd.y);
    return new Rectangle(
        x, y, Math.abs(rubberEnd.x - rubberStart.x), Math.abs(rubberEnd.y - rubberStart.y));
  }

  // ---------------------------------------------------------------- input

  private NodeHit nodeHitOf(Actor target) {
    Actor current = target;
    CanvasNode nestedNode = null;
    CanvasNode directOwner = null;
    while (current != null && current != this) {
      if (current instanceof CanvasNode node) {
        if (nestedNode == null) {
          nestedNode = node;
        } else if (directOwner == null) {
          directOwner = node;
        }
        if (nodesById.get(node.id()) == node) {
          return new NodeHit(nestedNode, nestedNode == node ? null : directOwner);
        }
      }
      current = current.getParent();
    }
    return null;
  }

  private record NodeHit(CanvasNode node, CanvasNode owner) {}

  private CanvasDragContext dragContext(
      CanvasNode target, CanvasNode dragged, float areaX, float areaY) {
    Vector2 worldPointer = areaToWorld(areaX, areaY);
    float targetX = target.sticky() ? areaX : worldPointer.x;
    float targetY = target.sticky() ? areaY : worldPointer.y;
    return new CanvasDragContext(
        this,
        dragged,
        draggedGroup,
        worldPointer.x,
        worldPointer.y,
        areaX,
        areaY,
        targetX - target.x(),
        targetY - target.y());
  }

  private void updateDragTargets(float areaX, float areaY) {
    if (draggedNode == null || draggedNodeOwner != null) {
      clearDragTargets(areaX, areaY);
      return;
    }
    List<CanvasNode> overlapping = intersectsAll(draggedNode);
    Set<CanvasNode> current = new LinkedHashSet<>(overlapping);
    for (CanvasNode target : new ArrayList<>(dragTargets)) {
      if (!current.contains(target)) {
        target.onDragExit(dragContext(target, draggedNode, areaX, areaY));
        dragTargets.remove(target);
      }
    }
    for (CanvasNode target : overlapping) {
      CanvasDragContext context = dragContext(target, draggedNode, areaX, areaY);
      if (dragTargets.add(target)) {
        target.onDragEnter(context);
      }
      target.onDragOver(context);
    }
  }

  private void clearDragTargets(float areaX, float areaY) {
    if (draggedNode != null) {
      for (CanvasNode target : new ArrayList<>(dragTargets)) {
        target.onDragExit(dragContext(target, draggedNode, areaX, areaY));
      }
    }
    dragTargets.clear();
  }

  private boolean dispatchDrop(float areaX, float areaY) {
    if (draggedNode == null || draggedNodeOwner != null || draggedGroup.size() != 1) {
      return false;
    }
    for (CanvasNode target : intersectsAll(draggedNode)) {
      if (target.onNodeDropped(dragContext(target, draggedNode, areaX, areaY))) {
        return true;
      }
    }
    return false;
  }

  private boolean panRequested(int button) {
    if (button == options.panButton()) {
      return true;
    }
    return options.panWithSpace()
        && button == Input.Buttons.LEFT
        && Gdx.input != null
        && Gdx.input.isKeyPressed(Input.Keys.SPACE);
  }

  private boolean additiveRequested() {
    if (Gdx.input == null) {
      return false;
    }
    return Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)
        || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)
        || Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
        || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
  }

  /** Routes pointer and keyboard input to view manipulation and node interactions. */
  private final class CanvasInputListener extends InputListener {

    @Override
    public boolean mouseMoved(InputEvent event, float x, float y) {
      if (event.getStage() != null) {
        event.getStage().setScrollFocus(CanvasArea.this);
      }
      return false;
    }

    @Override
    public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
      if (event.getStage() != null) {
        event.getStage().setScrollFocus(CanvasArea.this);
      }
    }

    @Override
    public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
      if (toActor != null && toActor.isDescendantOf(CanvasArea.this)) {
        return;
      }
      if (event.getStage() != null && event.getStage().getScrollFocus() == CanvasArea.this) {
        event.getStage().setScrollFocus(null);
      }
    }

    @Override
    public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY) {
      float factor = (float) Math.pow(1f + options.zoomSpeed(), -amountY);
      zoomAt(zoom * factor, x, y);
      return true;
    }

    @Override
    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
      if (pointer != 0 || dragMode != DragMode.NONE) {
        return false;
      }
      if (event.isHandled()) {
        // A child actor of a node (a button for example) already consumed this touch.
        return false;
      }
      if (event.getStage() != null) {
        event.getStage().setKeyboardFocus(CanvasArea.this);
        event.getStage().setScrollFocus(CanvasArea.this);
      }

      dragPointerButton = button;
      dragMoved = false;
      pressArea.set(x, y);
      lastPointerArea.set(x, y);
      lastPointerWorld.set(areaToWorld(x, y));

      if (panRequested(button)) {
        dragMode = DragMode.PAN;
        return true;
      }
      if (button != Input.Buttons.LEFT) {
        dragPointerButton = -1;
        return false;
      }

      NodeHit nodeHit = nodeHitOf(event.getTarget());
      if (nodeHit != null) {
        CanvasNode hit = nodeHit.node();
        draggedNodeOwner = nodeHit.owner();
        if (draggedNodeOwner != null) {
          draggedNode = hit;
          draggedGroup = List.of(hit);
          dragMode = DragMode.NODE;
          return true;
        }
        boolean additive = additiveRequested();
        if (!selection.contains(hit)) {
          select(List.of(hit), additive);
        } else if (additive) {
          selection.remove(hit);
          hit.setSelectedInternal(false);
          fireSelectionChanged();
        }
        draggedNode = hit;
        draggedGroup = selection.contains(hit) ? List.copyOf(selection) : List.of(hit);
        dragMode = DragMode.NODE;
        bringToFront(hit);
        return true;
      }

      if (options.rubberBandEnabled() && options.selectionEnabled()) {
        if (!additiveRequested()) {
          clearSelection();
        }
        rubberStart.set(lastPointerWorld);
        rubberEnd.set(lastPointerWorld);
        dragMode = DragMode.RUBBER_BAND;
        return true;
      }

      clearSelection();
      dragMode = DragMode.NONE;
      dragPointerButton = -1;
      return true;
    }

    @Override
    public void touchDragged(InputEvent event, float x, float y, int pointer) {
      if (pointer != 0 || dragMode == DragMode.NONE) {
        return;
      }
      if (!dragMoved && Vector2.dst(pressArea.x, pressArea.y, x, y) > CLICK_SLOP) {
        dragMoved = true;
      }
      switch (dragMode) {
        case PAN -> {
          panBy(x - lastPointerArea.x, y - lastPointerArea.y);
          lastPointerArea.set(x, y);
        }
        case NODE -> {
          if (dragMoved && draggedNodeOwner != null) {
            CanvasNode released = draggedNodeOwner.releaseOwnedNodeForDrag(draggedNode);
            draggedNodeOwner = null;
            if (released == null) {
              draggedNode = null;
              draggedGroup = List.of();
              dragMode = DragMode.NONE;
              return;
            }
            draggedNode = released;
            draggedGroup = List.of(released);
            select(released);
            bringToFront(released);
          } else if (draggedNodeOwner != null) {
            // Owned nodes must not move until their owner has released them for dragging.
            return;
          }
          Vector2 current = areaToWorld(x, y);
          float worldDx = current.x - lastPointerWorld.x;
          float worldDy = current.y - lastPointerWorld.y;
          float areaDx = x - lastPointerArea.x;
          float areaDy = y - lastPointerArea.y;
          for (CanvasNode node : draggedGroup) {
            if (node.sticky()) {
              node.onMove(areaDx, areaDy);
            } else {
              node.onMove(worldDx, worldDy);
            }
          }
          lastPointerWorld.set(current);
          lastPointerArea.set(x, y);
          updateDragTargets(x, y);
        }
        case RUBBER_BAND -> {
          rubberEnd.set(areaToWorld(x, y));
          lastPointerArea.set(x, y);
        }
        default -> {
          // nothing to do
        }
      }
    }

    @Override
    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
      if (pointer != 0 || button != dragPointerButton) {
        return;
      }
      Vector2 worldPos = areaToWorld(x, y);
      DragMode finished = dragMode;
      dragMode = DragMode.NONE;
      dragPointerButton = -1;

      switch (finished) {
        case NODE -> {
          if (options.snapToGrid()) {
            draggedGroup.stream()
                .filter(node -> !node.sticky())
                .forEach(node -> node.position(snap(node.x()), snap(node.y())));
          }
          boolean dropConsumed = dragMoved && dispatchDrop(x, y);
          if (!dragMoved && draggedNode != null) {
            Vector2 stagePointer = localToStageCoordinates(new Vector2(x, y));
            Vector2 localPointer = draggedNode.stageToLocalCoordinates(stagePointer);
            draggedNode.onClick(localPointer.x, localPointer.y, button);
          } else if (!dropConsumed) {
            draggedGroup.forEach(
                node -> {
                  if (node.canvas() != CanvasArea.this) {
                    return;
                  }
                  if (node.sticky()) {
                    node.onDrop(x, y);
                  } else {
                    node.onDrop(worldPos.x, worldPos.y);
                  }
                });
          }
          clearDragTargets(x, y);
          draggedNode = null;
          draggedGroup = List.of();
          draggedNodeOwner = null;
        }
        case RUBBER_BAND -> {
          rubberEnd.set(worldPos);
          if (dragMoved) {
            select(nodesInRect(rubberBandRect()), additiveRequested());
          }
        }
        default -> {
          // pan or nothing, no follow up action
        }
      }
    }

    @Override
    public boolean keyDown(InputEvent event, int keycode) {
      if (!options.keyboardShortcutsEnabled()) {
        return false;
      }
      float step = options.nudgeStep();
      switch (keycode) {
        case Input.Keys.FORWARD_DEL, Input.Keys.DEL -> {
          return deleteSelection() > 0;
        }
        case Input.Keys.LEFT -> {
          return nudge(-step, 0f);
        }
        case Input.Keys.RIGHT -> {
          return nudge(step, 0f);
        }
        case Input.Keys.UP -> {
          return nudge(0f, step);
        }
        case Input.Keys.DOWN -> {
          return nudge(0f, -step);
        }
        default -> {
          return false;
        }
      }
    }

    private boolean nudge(float dx, float dy) {
      if (selection.isEmpty()) {
        return false;
      }
      float factor = additiveRequested() ? options.gridSize() : 1f;
      selection.forEach(node -> node.onMove(dx * factor, dy * factor));
      return true;
    }
  }
}
