package feature.canvas.nodes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.GdxRuntimeException;
import engine.Game;
import engine.utils.components.draw.TextureMap;
import engine.utils.components.path.SimpleIPath;
import engine.utils.logging.DungeonLogger;
import feature.canvas.CanvasGraphics;
import feature.canvas.CanvasArea;
import feature.canvas.CanvasNode;
import feature.canvas.CanvasNodeType;
import feature.canvas.NodeState;
import java.util.Objects;

/**
 * A canvas node that renders an image at its native dimensions, optionally scaled and outlined.
 *
 * <p>The texture is loaded lazily so image nodes can be created and serialized by a headless
 * server. When the texture is available, the node sizes itself to the rendered image, adding room
 * for the optional outline.
 */
public class ImageNode extends CanvasNode {

  /** Stable type id of this node type. */
  public static final String TYPE = "canvas.image";

  /** Prop key holding the asset path. */
  public static final String PROP_PATH = "path";

  /** Backwards-compatible alias for the asset path property. */
  public static final String PROP_IMAGE_PATH = PROP_PATH;

  /** Prop key holding the image scale. */
  public static final String PROP_SCALE = "scale";

  /** Prop key holding the outline color. */
  public static final String PROP_OUTLINE_COLOR = "outlineColor";

  /** Prop key holding the outline size. */
  public static final String PROP_OUTLINE_SIZE = "outlineSize";

  /** Default image scale. */
  public static final float DEFAULT_SCALE = 1f;

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(ImageNode.class);
  private static final float DEFAULT_WIDTH = 120f;
  private static final float DEFAULT_HEIGHT = 60f;

  private String imagePath;
  private float scale = DEFAULT_SCALE;
  private Color outlineColor;
  private float outlineSize;

  private Texture texture;
  private boolean textureLookupAttempted;
  private int intrinsicWidth;
  private int intrinsicHeight;

  /**
   * Creates an image node.
   *
   * @param id unique node id within a canvas
   * @param imagePath the internal asset path of the image
   */
  public ImageNode(String id, String imagePath) {
    super(id, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    this.imagePath = requireImagePath(imagePath);
    discoverImageDimensions();
  }

  /**
   * Creates an image node from a serialized state.
   *
   * @param state the state to rebuild from
   */
  public ImageNode(NodeState state) {
    this(state.id(), pathFrom(state));
  }

  /** Registers this node type with the {@link CanvasNodeType} registry. */
  public static void registerType() {
    CanvasNodeType.register(TYPE, ImageNode::new);
  }

  /**
   * Returns the internal asset path of the image.
   *
   * @return the image path
   */
  public String imagePath() {
    return imagePath;
  }

  /**
   * Sets the internal asset path of the image.
   *
   * @param value the new image path
   * @return this node for chaining
   */
  public ImageNode imagePath(String value) {
    String newPath = requireImagePath(value);
    if (imagePath.equals(newPath)) {
      return this;
    }
    imagePath = newPath;
    texture = null;
    textureLookupAttempted = false;
    intrinsicWidth = 0;
    intrinsicHeight = 0;
    discoverImageDimensions();
    notifyStateChanged();
    return this;
  }

  /**
   * Returns the image scale.
   *
   * @return the scale multiplier
   */
  public float scale() {
    return scale;
  }

  /**
   * Sets the image scale while preserving its aspect ratio.
   *
   * @param value the scale multiplier; must be finite and greater than zero
   * @return this node for chaining
   */
  public ImageNode scale(float value) {
    validateScale(value);
    if (scale == value) {
      return this;
    }
    scale = value;
    updateNodeSize();
    notifyStateChanged();
    return this;
  }

  /**
   * Returns the outline color, or null when no outline color is configured.
   *
   * @return the outline color
   */
  public Color outlineColor() {
    return outlineColor;
  }

  /**
   * Returns the outline size.
   *
   * @return the outline thickness in world units
   */
  public float outlineSize() {
    return outlineSize;
  }

  /**
   * Adds an outline around the image.
   *
   * @param color the outline color
   * @param size the outline thickness in world units; must be finite and non-negative
   * @return this node for chaining
   */
  public ImageNode outline(Color color, float size) {
    Objects.requireNonNull(color, "outlineColor");
    validateOutlineSize(size);
    outlineColor = new Color(color);
    outlineSize = size;
    updateNodeSize();
    notifyStateChanged();
    return this;
  }

  /**
   * Sets the outline color.
   *
   * <p>Passing null clears the outline color and disables outline rendering.
   *
   * @param value the outline color, or null to disable the outline
   * @return this node for chaining
   */
  public ImageNode outlineColor(Color value) {
    outlineColor = value == null ? null : new Color(value);
    updateNodeSize();
    notifyStateChanged();
    return this;
  }

  /**
   * Sets the outline thickness.
   *
   * @param value the outline thickness in world units; must be finite and non-negative
   * @return this node for chaining
   */
  public ImageNode outlineSize(float value) {
    validateOutlineSize(value);
    outlineSize = value;
    updateNodeSize();
    notifyStateChanged();
    return this;
  }

  /**
   * Disables the image outline.
   *
   * @return this node for chaining
   */
  public ImageNode clearOutline() {
    if (outlineColor == null && outlineSize == 0f) {
      return this;
    }
    outlineColor = null;
    outlineSize = 0f;
    updateNodeSize();
    notifyStateChanged();
    return this;
  }

  @Override
  public String typeId() {
    return TYPE;
  }

  @Override
  public void onAdd(CanvasArea area) {
    ensureTexture();
  }

  @Override
  protected void buildContent() {
    ensureTexture();
  }

  @Override
  protected void drawBackground(Batch batch, float parentAlpha) {
    ensureTexture();
    if (texture == null) {
      return;
    }

    updateNodeSize();
    float border = hasOutline() ? outlineSize : 0f;
    float imageWidth = texture.getWidth() * scale;
    float imageHeight = texture.getHeight() * scale;

    Color previous = batch.getColor().cpy();
    batch.setColor(1f, 1f, 1f, parentAlpha);
    batch.draw(texture, getX() + border, getY() + border, imageWidth, imageHeight);
    batch.setColor(previous);

    if (border > 0f) {
      CanvasGraphics.outline(
          batch,
          outlineColor,
          parentAlpha,
          getX(),
          getY(),
          getWidth(),
          getHeight(),
          border);
    }
  }

  @Override
  protected void writeProps(NodeState.Props props) {
    props.put(PROP_PATH, imagePath);
    props.put(PROP_SCALE, scale);
    props.put(PROP_OUTLINE_SIZE, outlineSize);
    if (outlineColor != null) {
      props.put(PROP_OUTLINE_COLOR, outlineColor.toString());
    }
  }

  @Override
  protected void readProps(NodeState state) {
    String storedPath = pathFrom(state);
    if (!imagePath.equals(storedPath)) {
      imagePath = storedPath;
      texture = null;
      textureLookupAttempted = false;
      intrinsicWidth = 0;
      intrinsicHeight = 0;
    }

    float storedScale = state.floatProp(PROP_SCALE, DEFAULT_SCALE);
    scale = isValidScale(storedScale) ? storedScale : DEFAULT_SCALE;

    float storedOutlineSize = state.floatProp(PROP_OUTLINE_SIZE, 0f);
    outlineSize =
        Float.isFinite(storedOutlineSize) && storedOutlineSize >= 0f ? storedOutlineSize : 0f;

    String storedOutlineColor = state.prop(PROP_OUTLINE_COLOR, null);
    outlineColor = parseColor(storedOutlineColor);

    discoverImageDimensions();
    updateNodeSize();
  }

  private static String pathFrom(NodeState state) {
    Objects.requireNonNull(state, "state");
    String path = state.prop(PROP_PATH, null);
    if (path == null) {
      path = state.prop("imagePath", null);
    }
    return requireImagePath(path);
  }

  private static String requireImagePath(String value) {
    Objects.requireNonNull(value, "imagePath");
    if (value.isBlank()) {
      throw new IllegalArgumentException("imagePath must not be blank");
    }
    return value;
  }

  private static void validateScale(float value) {
    if (!isValidScale(value)) {
      throw new IllegalArgumentException("scale must be finite and greater than zero");
    }
  }

  private static boolean isValidScale(float value) {
    return Float.isFinite(value) && value > 0f;
  }

  private static void validateOutlineSize(float value) {
    if (!Float.isFinite(value) || value < 0f) {
      throw new IllegalArgumentException("outlineSize must be finite and non-negative");
    }
  }

  private boolean hasOutline() {
    return outlineColor != null && outlineSize > 0f;
  }

  private void ensureTexture() {
    if (texture != null || textureLookupAttempted || Game.isHeadless()) {
      return;
    }
    textureLookupAttempted = true;
    texture = TextureMap.instance().textureAt(new SimpleIPath(imagePath));
    if (texture == null) {
      LOGGER.warn("Could not load image '{}' for canvas node '{}'", imagePath, id());
      return;
    }
    intrinsicWidth = texture.getWidth();
    intrinsicHeight = texture.getHeight();
    updateNodeSize();
  }

  private void discoverImageDimensions() {
    Texture cached = TextureMap.instance().get(imagePath);
    if (cached != null) {
      intrinsicWidth = cached.getWidth();
      intrinsicHeight = cached.getHeight();
      updateNodeSize();
      return;
    }
    if (Gdx.files == null) {
      return;
    }
    FileHandle file = Gdx.files.internal(imagePath);
    if (!file.exists()) {
      return;
    }

    try {
      Pixmap pixmap = new Pixmap(file);
      try {
        intrinsicWidth = pixmap.getWidth();
        intrinsicHeight = pixmap.getHeight();
      } finally {
        pixmap.dispose();
      }
      updateNodeSize();
    } catch (GdxRuntimeException e) {
      LOGGER.warn("Could not inspect image '{}' for canvas node '{}'", imagePath, id());
    }
  }

  private void updateNodeSize() {
    if (intrinsicWidth <= 0 || intrinsicHeight <= 0) {
      return;
    }
    float border = hasOutline() ? outlineSize : 0f;
    float width = intrinsicWidth * scale + 2f * border;
    float height = intrinsicHeight * scale + 2f * border;
    if (getWidth() != width || getHeight() != height) {
      setSize(width, height);
      invalidateLayout();
    }
  }

  private static Color parseColor(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return Color.valueOf(value);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }
}
