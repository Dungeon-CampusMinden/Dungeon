package feature.canvas;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Serializable description of a single {@link CanvasNode}.
 *
 * <p>This is the single state format used for three purposes:
 *
 * <ul>
 *   <li>transporting the server-provided default nodes to the client (inside a {@link
 *       CanvasSnapshot}),
 *   <li>persisting local player changes, again as a {@link CanvasSnapshot},
 *   <li>reconstructing concrete node instances through {@link CanvasNodeType}.
 * </ul>
 *
 * <p>Inside a snapshot of local changes the meaning of a state is derived from two fields: {@link
 * #origin()} tells whether it describes a locally created node or a modified server default, and
 * {@link #deleted()} marks a server default the player removed. See {@link
 * CanvasSnapshot#mergeWith(CanvasSnapshot, CanvasOptions)}.
 *
 * <p>Type specific state lives in {@link #props()}, a flat {@code String -> String} map. Keeping it
 * flat makes the wire format debuggable and version tolerant: props a client does not understand
 * are carried through unchanged instead of being dropped.
 *
 * @param typeId stable identifier of the node type, see {@link CanvasNodeType}
 * @param id unique node id within a canvas
 * @param origin whether the node came from the server or was created locally
 * @param x world x coordinate
 * @param y world y coordinate
 * @param z render order; higher values are drawn in front
 * @param width node width in world units
 * @param height node height in world units
 * @param movable whether the node can be dragged
 * @param deletable whether the node can be deleted by the player
 * @param sticky whether the node is pinned to the viewport instead of living in the panned and
 *     zoomed world; sticky coordinates are area coordinates
 * @param deleted marks a removed node inside a snapshot of local changes; never set on a node that
 *     is actually rendered
 * @param props type specific state
 */
public record NodeState(
    String typeId,
    String id,
    NodeOrigin origin,
    float x,
    float y,
    int z,
    float width,
    float height,
    boolean movable,
    boolean deletable,
    boolean sticky,
    boolean deleted,
    Map<String, String> props)
    implements Serializable {

  /** Canonical constructor that validates required fields and defensively copies the props. */
  public NodeState {
    Objects.requireNonNull(typeId, "typeId");
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(origin, "origin");
    props = props == null ? Map.of() : Map.copyOf(props);
  }

  /**
   * Creates a state with default flags, no props and zero z.
   *
   * @param typeId stable node type identifier
   * @param id unique node id
   * @param origin node origin
   * @param x world x coordinate
   * @param y world y coordinate
   * @param width node width
   * @param height node height
   * @return the created state
   */
  public static NodeState of(
      String typeId, String id, NodeOrigin origin, float x, float y, float width, float height) {
    return new NodeState(
        typeId, id, origin, x, y, 0, width, height, true, true, false, false, Map.of());
  }

  /**
   * Returns a copy of this state with a different position.
   *
   * @param newX the new world x coordinate
   * @param newY the new world y coordinate
   * @return the modified copy
   */
  public NodeState withPosition(float newX, float newY) {
    return new NodeState(
        typeId, id, origin, newX, newY, z, width, height, movable, deletable, sticky, deleted,
        props);
  }

  /**
   * Returns a copy of this state with a different render order.
   *
   * @param newZ the new z value
   * @return the modified copy
   */
  public NodeState withZ(int newZ) {
    return new NodeState(
        typeId, id, origin, x, y, newZ, width, height, movable, deletable, sticky, deleted, props);
  }

  /**
   * Returns a copy of this state with a different size.
   *
   * @param newWidth the new width
   * @param newHeight the new height
   * @return the modified copy
   */
  public NodeState withSize(float newWidth, float newHeight) {
    return new NodeState(
        typeId, id, origin, x, y, z, newWidth, newHeight, movable, deletable, sticky, deleted,
        props);
  }

  /**
   * Returns a copy of this state with a different origin.
   *
   * @param newOrigin the new origin
   * @return the modified copy
   */
  public NodeState withOrigin(NodeOrigin newOrigin) {
    return new NodeState(
        typeId, id, newOrigin, x, y, z, width, height, movable, deletable, sticky, deleted, props);
  }

  /**
   * Returns a copy of this state with a different id.
   *
   * @param newId the new node id
   * @return the modified copy
   */
  public NodeState withId(String newId) {
    return new NodeState(
        typeId, newId, origin, x, y, z, width, height, movable, deletable, sticky, deleted, props);
  }

  /**
   * Returns a copy of this state with the given props merged over the existing ones.
   *
   * @param extraProps the props to add or overwrite
   * @return the modified copy
   */
  public NodeState withProps(Map<String, String> extraProps) {
    Map<String, String> merged = new LinkedHashMap<>(props);
    merged.putAll(extraProps);
    return new NodeState(
        typeId, id, origin, x, y, z, width, height, movable, deletable, sticky, deleted, merged);
  }

  /**
   * Returns a copy of this state with a different sticky flag.
   *
   * @param newSticky true to pin the node to the viewport
   * @return the modified copy
   */
  public NodeState withSticky(boolean newSticky) {
    return new NodeState(
        typeId, id, origin, x, y, z, width, height, movable, deletable, newSticky, deleted, props);
  }

  /**
   * Creates the marker state used to record that a server default node was deleted locally.
   *
   * @param nodeId the id of the removed default node
   * @return a deletion marker for the given id
   */
  public static NodeState deletion(String nodeId) {
    return new NodeState(
        CanvasNode.TYPE_ID,
        nodeId,
        NodeOrigin.DEFAULT,
        0f,
        0f,
        0,
        0f,
        0f,
        false,
        false,
        false,
        true,
        Map.of());
  }

  /**
   * Reads a string prop.
   *
   * @param key the prop key
   * @param fallback the value returned when the prop is absent
   * @return the prop value or the fallback
   */
  public String prop(String key, String fallback) {
    String value = props.get(key);
    return value == null ? fallback : value;
  }

  /**
   * Reads a float prop.
   *
   * @param key the prop key
   * @param fallback the value returned when the prop is absent or unparsable
   * @return the prop value or the fallback
   */
  public float floatProp(String key, float fallback) {
    String value = props.get(key);
    if (value == null) {
      return fallback;
    }
    try {
      return Float.parseFloat(value);
    } catch (NumberFormatException e) {
      return fallback;
    }
  }

  /**
   * Reads an int prop.
   *
   * @param key the prop key
   * @param fallback the value returned when the prop is absent or unparsable
   * @return the prop value or the fallback
   */
  public int intProp(String key, int fallback) {
    String value = props.get(key);
    if (value == null) {
      return fallback;
    }
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      return fallback;
    }
  }

  /**
   * Reads a boolean prop.
   *
   * @param key the prop key
   * @param fallback the value returned when the prop is absent
   * @return the prop value or the fallback
   */
  public boolean boolProp(String key, boolean fallback) {
    String value = props.get(key);
    return value == null ? fallback : Boolean.parseBoolean(value);
  }

  /**
   * Creates a mutable builder for node props.
   *
   * @return a new empty props builder
   */
  public static Props propsBuilder() {
    return new Props();
  }

  /** Small mutable helper for assembling the flat {@link NodeState#props()} map. */
  public static final class Props {
    private final Map<String, String> values = new LinkedHashMap<>();

    private Props() {}

    /**
     * Stores a string value.
     *
     * @param key the prop key
     * @param value the value; null removes the key
     * @return this builder for chaining
     */
    public Props put(String key, String value) {
      if (value == null) {
        values.remove(key);
      } else {
        values.put(key, value);
      }
      return this;
    }

    /**
     * Stores a float value.
     *
     * @param key the prop key
     * @param value the value
     * @return this builder for chaining
     */
    public Props put(String key, float value) {
      return put(key, Float.toString(value));
    }

    /**
     * Stores an int value.
     *
     * @param key the prop key
     * @param value the value
     * @return this builder for chaining
     */
    public Props put(String key, int value) {
      return put(key, Integer.toString(value));
    }

    /**
     * Stores a boolean value.
     *
     * @param key the prop key
     * @param value the value
     * @return this builder for chaining
     */
    public Props put(String key, boolean value) {
      return put(key, Boolean.toString(value));
    }

    /**
     * Returns the assembled props map.
     *
     * @return an immutable copy of the collected props
     */
    public Map<String, String> build() {
      return Map.copyOf(values);
    }
  }
}
