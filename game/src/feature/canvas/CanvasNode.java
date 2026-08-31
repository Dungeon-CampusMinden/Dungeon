package feature.canvas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import engine.utils.Scene2dElementFactory;
import java.util.Objects;

/**
 * Base class for everything that can be placed on a {@link CanvasArea}.
 *
 * <p>A node <em>is</em> a Scene2D {@link Group}: that is the entire contract between the canvas and
 * its nodes. Subclasses are free to fill themselves with arbitrary actors by overriding {@link
 * #buildContent()}, and to replace the default look by overriding {@link #drawBackground(Batch,
 * float)}. Everything else - positioning, dragging, z ordering, clipping, selection - is handled by
 * the canvas.
 *
 * <p>The base implementation renders a rectangle border with the node id centered inside, which
 * makes a bare {@code CanvasNode} immediately usable as a placeholder.
 *
 * <p>Coordinates are <em>world</em> coordinates. The canvas applies pan and zoom on top, so node
 * coordinates and {@link #bounds()} are independent of the current view.
 *
 * <h2>Interaction hooks</h2>
 *
 * All hooks default to a no-op except {@link #onMove(float, float)}, which moves the node when
 * {@link #movable()} is true.
 *
 * <h2>Serialization</h2>
 *
 * Every node type must be identifiable and serializable so that server provided defaults can be
 * sent to the client and local changes can be persisted across closing and reopening the dialog.
 * Subclasses therefore override {@link #typeId()}, {@link #writeProps(NodeState.Props)} and {@link
 * #readProps(NodeState)}, and register a factory with {@link CanvasNodeType}.
 */
public class CanvasNode extends Group {

  /** Type id used by plain, non specialized nodes. */
  public static final String TYPE_ID = "canvas.node";

  private final String id;

  private int z;
  private boolean movable = true;
  private boolean selectable = true;
  private boolean deletable = true;
  private boolean sticky;
  private NodeOrigin origin = NodeOrigin.LOCAL;

  private CanvasArea canvas;
  private boolean selected;
  private boolean contentBuilt;
  private boolean layoutDirty = true;
  private float lastLayoutWidth = Float.NaN;
  private float lastLayoutHeight = Float.NaN;
  private Label defaultLabel;

  /**
   * Creates a node with the given id and a default size of 120x60 world units.
   *
   * @param id unique id within a canvas; must not be null or blank
   */
  public CanvasNode(String id) {
    this(id, 120f, 60f);
  }

  /**
   * Creates a node with the given id and size.
   *
   * @param id unique id within a canvas; must not be null or blank
   * @param width the node width in world units
   * @param height the node height in world units
   */
  public CanvasNode(String id, float width, float height) {
    Objects.requireNonNull(id, "id");
    if (id.isBlank()) {
      throw new IllegalArgumentException("node id must not be blank");
    }
    this.id = id;
    setSize(width, height);
    setTransform(false);
  }

  /**
   * Returns the unique id of this node within its canvas.
   *
   * @return the node id
   */
  public String id() {
    return id;
  }

  /**
   * Returns the world x coordinate of this node.
   *
   * @return the x coordinate
   */
  public float x() {
    return getX();
  }

  /**
   * Returns the world y coordinate of this node.
   *
   * @return the y coordinate
   */
  public float y() {
    return getY();
  }

  /**
   * Moves this node to an absolute world position.
   *
   * @param newX the new x coordinate
   * @param newY the new y coordinate
   * @return this node for chaining
   */
  public CanvasNode position(float newX, float newY) {
    if (getX() != newX || getY() != newY) {
      setPosition(newX, newY);
      notifyStateChanged();
    }
    return this;
  }

  /**
   * Returns the render order of this node. Higher values are drawn in front.
   *
   * @return the z value
   */
  public int z() {
    return z;
  }

  /**
   * Sets the render order of this node and reorders it within its canvas.
   *
   * @param newZ the new z value; higher values are drawn in front
   * @return this node for chaining
   */
  public CanvasNode z(int newZ) {
    if (this.z == newZ) {
      return this;
    }
    this.z = newZ;
    if (canvas != null) {
      canvas.invalidateOrder();
    }
    notifyStateChanged();
    return this;
  }

  /**
   * Returns the node width in world units.
   *
   * @return the width
   */
  public float width() {
    return getWidth();
  }

  /**
   * Returns the node height in world units.
   *
   * @return the height
   */
  public float height() {
    return getHeight();
  }

  /**
   * Resizes this node.
   *
   * @param newWidth the new width in world units
   * @param newHeight the new height in world units
   * @return this node for chaining
   */
  public CanvasNode size(float newWidth, float newHeight) {
    if (getWidth() != newWidth || getHeight() != newHeight) {
      setSize(newWidth, newHeight);
      invalidateLayout();
      notifyStateChanged();
    }
    return this;
  }

  /**
   * Returns whether this node can be dragged by the player.
   *
   * @return true if the node is movable
   */
  public boolean movable() {
    return movable;
  }

  /**
   * Sets whether this node can be dragged by the player.
   *
   * @param value true to make the node movable
   * @return this node for chaining
   */
  public CanvasNode movable(boolean value) {
    this.movable = value;
    return this;
  }

  /**
   * Returns whether this node can be selected.
   *
   * @return true if the node is selectable
   */
  public boolean selectable() {
    return selectable;
  }

  /**
   * Sets whether this node can be selected.
   *
   * @param value true to make the node selectable
   * @return this node for chaining
   */
  public CanvasNode selectable(boolean value) {
    this.selectable = value;
    return this;
  }

  /**
   * Returns whether this node can be deleted by the player.
   *
   * @return true if the node is deletable
   */
  public boolean deletable() {
    return deletable;
  }

  /**
   * Sets whether this node can be deleted by the player.
   *
   * @param value true to make the node deletable
   * @return this node for chaining
   */
  public CanvasNode deletable(boolean value) {
    this.deletable = value;
    return this;
  }

  /**
   * Returns whether this node is pinned to the viewport.
   *
   * <p>A sticky node ignores pan and zoom: it always renders at the same size at the same place
   * inside the canvas viewport, which effectively makes it part of the canvas overlay. Its
   * position, size and bounds are therefore expressed in <em>area</em> coordinates instead of world
   * coordinates. Sticky nodes always render in front of the non sticky ones.
   *
   * @return true if the node is pinned to the viewport
   */
  public boolean sticky() {
    return sticky;
  }

  /**
   * Sets whether this node is pinned to the viewport.
   *
   * <p>Switching the flag while the node is on a canvas re-parents it and keeps the point it was
   * anchored to visually stable by converting its coordinates between world and area space.
   *
   * @param value true to pin the node to the viewport
   * @return this node for chaining
   */
  public CanvasNode sticky(boolean value) {
    if (this.sticky == value) {
      return this;
    }
    this.sticky = value;
    if (canvas != null) {
      canvas.stickyChanged(this);
    }
    notifyStateChanged();
    return this;
  }

  /**
   * Returns whether this node came from the server defaults or was created locally.
   *
   * @return the node origin
   */
  public NodeOrigin origin() {
    return origin;
  }

  /**
   * Sets the origin of this node.
   *
   * <p>This is managed by the framework; puzzle code normally does not call it.
   *
   * @param value the new origin; must not be null
   * @return this node for chaining
   */
  public CanvasNode origin(NodeOrigin value) {
    this.origin = Objects.requireNonNull(value, "origin");
    return this;
  }

  /**
   * Returns the canvas this node currently belongs to.
   *
   * @return the owning canvas, or null when the node is not on a canvas
   */
  public CanvasArea canvas() {
    return canvas;
  }

  /**
   * Returns whether this node is currently selected.
   *
   * @return true if selected
   */
  public boolean selected() {
    return selected;
  }

  /**
   * Returns the world space bounds of this node.
   *
   * @return a new rectangle describing the node bounds
   */
  public Rectangle bounds() {
    return new Rectangle(getX(), getY(), getWidth(), getHeight());
  }

  /**
   * Returns the world x coordinate of this node's center.
   *
   * @return the center x coordinate
   */
  public float centerX() {
    return getX() + getWidth() / 2f;
  }

  /**
   * Returns the world y coordinate of this node's center.
   *
   * @return the center y coordinate
   */
  public float centerY() {
    return getY() + getHeight() / 2f;
  }

  // ---------------------------------------------------------------- interaction hooks

  /**
   * Called when the node is clicked.
   *
   * <p>Default implementation does nothing.
   *
   * @param localX the click x coordinate relative to the node
   * @param localY the click y coordinate relative to the node
   * @param button the mouse button, see {@link com.badlogic.gdx.Input.Buttons}
   */
  public void onClick(float localX, float localY, int button) {}

  /**
   * Called while the node is dragged.
   *
   * <p>Default implementation moves the node by the given delta when {@link #movable()} is true and
   * does nothing otherwise.
   *
   * @param dx the movement along x in world units
   * @param dy the movement along y in world units
   */
  public void onMove(float dx, float dy) {
    if (movable) {
      position(getX() + dx, getY() + dy);
    }
  }

  /**
   * Called after the node has been added to a canvas.
   *
   * <p>Default implementation does nothing.
   *
   * @param area the canvas the node was added to
   */
  public void onAdd(CanvasArea area) {}

  /**
   * Called after the node has been removed from a canvas.
   *
   * <p>Default implementation does nothing.
   *
   * @param area the canvas the node was removed from
   */
  public void onRemove(CanvasArea area) {}

  /**
   * Called when a drag of this node ends.
   *
   * <p>Default implementation does nothing. This is the natural place to inspect {@link
   * CanvasArea#intersectsOther(CanvasNode)} and react to being dropped onto another node.
   *
   * @param worldX the world x coordinate the pointer was released at
   * @param worldY the world y coordinate the pointer was released at
   */
  public void onDrop(float worldX, float worldY) {}

  /**
   * Called when the selection state of this node changes.
   *
   * <p>Default implementation does nothing; the selection outline itself is drawn by the canvas.
   *
   * @param isSelected the new selection state
   */
  public void onSelectionChanged(boolean isSelected) {}

  // ---------------------------------------------------------------- rendering

  /**
   * Builds the visual content of this node.
   *
   * <p>Called lazily before the node is drawn for the first time, so nodes can safely be
   * constructed on a headless server where no graphics context exists. The base implementation adds
   * a centered label showing the node id.
   */
  protected void buildContent() {
    defaultLabel = Scene2dElementFactory.createLabel(id, 16, Color.BLACK);
    defaultLabel.setAlignment(Align.center);
    addActor(defaultLabel);
  }

  /**
   * Discards and rebuilds the visual content of this node.
   *
   * <p>Use this after changing state that {@link #buildContent()} depends on.
   */
  protected void rebuildContent() {
    clearChildren();
    defaultLabel = null;
    contentBuilt = false;
    invalidateLayout();
  }

  /**
   * Draws the node background before its children.
   *
   * <p>The base implementation fills the node with a translucent white box and draws a black
   * border. Override with an empty body to render nothing but the child actors.
   *
   * @param batch the batch to draw with
   * @param parentAlpha the inherited alpha
   */
  protected void drawBackground(Batch batch, float parentAlpha) {
    CanvasGraphics.fill(batch, BACKGROUND, parentAlpha, getX(), getY(), getWidth(), getHeight());
    CanvasGraphics.outline(batch, BORDER, parentAlpha, getX(), getY(), getWidth(), getHeight(), 2f);
  }

  private static final Color BACKGROUND = new Color(1f, 1f, 1f, 0.92f);
  private static final Color BORDER = new Color(0f, 0f, 0f, 1f);

  @Override
  public void draw(Batch batch, float parentAlpha) {
    if (!contentBuilt) {
      contentBuilt = true;
      buildContent();
      invalidateLayout();
    }
    if (layoutDirty || lastLayoutWidth != getWidth() || lastLayoutHeight != getHeight()) {
      layoutDirty = false;
      lastLayoutWidth = getWidth();
      lastLayoutHeight = getHeight();
      layoutContent();
    }
    drawBackground(batch, parentAlpha);
    super.draw(batch, parentAlpha);
  }

  /**
   * Positions the child actors of this node.
   *
   * <p>Called before the node is drawn whenever its size changed or {@link #invalidateLayout()} was
   * called. The base implementation stretches the default id label across the whole node.
   */
  protected void layoutContent() {
    if (defaultLabel != null) {
      defaultLabel.setSize(getWidth(), getHeight());
      defaultLabel.setPosition(0f, 0f);
    }
  }

  /** Marks the child layout of this node as outdated. */
  protected void invalidateLayout() {
    layoutDirty = true;
  }

  // ---------------------------------------------------------------- serialization

  /**
   * Returns the stable type identifier of this node.
   *
   * <p>Must match the id the node type was registered with in {@link CanvasNodeType}.
   *
   * @return the type identifier
   */
  public String typeId() {
    return TYPE_ID;
  }

  /**
   * Captures the full state of this node.
   *
   * <p>The common fields are written by this method; type specific state is contributed by {@link
   * #writeProps(NodeState.Props)}.
   *
   * @return the serializable state of this node
   */
  public final NodeState toState() {
    NodeState.Props props = NodeState.propsBuilder();
    writeProps(props);
    return new NodeState(
        typeId(),
        id,
        origin,
        getX(),
        getY(),
        z,
        getWidth(),
        getHeight(),
        movable,
        deletable,
        sticky,
        props.build());
  }

  /**
   * Restores the state of this node.
   *
   * <p>Applies the common fields and then hands the state to {@link #readProps(NodeState)} for type
   * specific state. The node id is not changed; states are always applied to the node they belong
   * to.
   *
   * @param state the state to apply; must not be null
   */
  public final void applyState(NodeState state) {
    Objects.requireNonNull(state, "state");
    setPosition(state.x(), state.y());
    setSize(state.width(), state.height());
    this.z = state.z();
    this.movable = state.movable();
    this.deletable = state.deletable();
    this.sticky = state.sticky();
    this.origin = state.origin();
    readProps(state);
    invalidateLayout();
    if (canvas != null) {
      canvas.invalidateOrder();
    }
  }

  /**
   * Writes type specific state.
   *
   * <p>Default implementation writes nothing.
   *
   * @param props the props builder to write to
   */
  protected void writeProps(NodeState.Props props) {}

  /**
   * Reads type specific state.
   *
   * <p>Default implementation reads nothing.
   *
   * @param state the state to read from
   */
  protected void readProps(NodeState state) {}

  // ---------------------------------------------------------------- framework internals

  /**
   * Notifies the owning canvas that persistent node state changed.
   *
   * <p>Subclasses should call this after mutating state that {@link #writeProps(NodeState.Props)}
   * writes, so the change is captured in the local overlay.
   */
  protected void notifyStateChanged() {
    if (canvas != null) {
      canvas.onNodeStateChanged(this);
    }
  }

  /**
   * Sets the owning canvas. Called by {@link CanvasArea} only.
   *
   * @param area the owning canvas, or null when removed
   */
  void attach(CanvasArea area) {
    this.canvas = area;
  }

  /**
   * Updates the selection flag and fires {@link #onSelectionChanged(boolean)} on change.
   *
   * @param value the new selection state
   */
  void setSelectedInternal(boolean value) {
    if (this.selected == value) {
      return;
    }
    this.selected = value;
    onSelectionChanged(value);
  }
}
