package feature.canvas.nodes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Align;
import feature.canvas.CanvasGraphics;
import feature.canvas.CanvasNode;
import feature.canvas.CanvasNodeType;
import feature.canvas.NodeState;
import feature.hud.dialogs.DialogDesign;
import feature.hud.elements.RichLabel;

/**
 * A simple node that renders a piece of text inside a colored box.
 *
 * <p>Besides being immediately useful for prototypes and demos, this class is the reference
 * implementation of the node serialization contract: it declares a stable {@link #typeId()},
 * registers a factory with {@link CanvasNodeType} and round-trips its text and sizing properties
 * through {@link NodeState#props()}.
 */
public class LabelNode extends CanvasNode {

  /** Stable type id of this node type. */
  public static final String TYPE = "canvas.label";

  private static final float CONTENT_PADDING = 20f;

  /** Prop key holding the displayed text. */
  public static final String PROP_TEXT = "text";

  /** Backwards-compatible alias for the common node color property. */
  public static final String PROP_COLOR = CanvasNode.PROP_COLOR;

  /** Prop key holding the font size. */
  public static final String PROP_FONT_SIZE = "fontSize";

  /** Prop key holding the maximum outer width, or zero for no maximum. */
  public static final String PROP_MAX_WIDTH = "maxWidth";

  public static final Color DEFAULT_COLOR = Color.valueOf("2f6f9f");

  private String text;
  private float maxWidth;
  private int fontSize = 18;
  private RichLabel label;

  /**
   * Creates a label node.
   *
   * @param id unique node id within a canvas
   * @param text the text to display
   */
  public LabelNode(String id, String text) {
    super(id, 160f, 64f);
    this.color(DEFAULT_COLOR);
    this.text = text == null ? id : text;
  }

  /**
   * Creates a label node from a state, used by the {@link CanvasNodeType} registry.
   *
   * @param state the state to rebuild from
   */
  public LabelNode(NodeState state) {
    this(state.id(), state.prop(PROP_TEXT, state.id()));
  }

  /** Registers this node type with the {@link CanvasNodeType} registry. */
  public static void registerType() {
    CanvasNodeType.register(TYPE, LabelNode::new);
  }

  /**
   * Returns the displayed text.
   *
   * @return the text
   */
  public String text() {
    return text;
  }

  /**
   * Sets the displayed text.
   *
   * @param value the new text
   * @return this node for chaining
   */
  public LabelNode text(String value) {
    this.text = value == null ? id() : value;
    if (label != null) {
      label.setText(RichLabel.toRichText(this.text));
      if (!isOwnedByNode()) {
        autoSizeToLabel();
      }
    }
    notifyStateChanged();
    return this;
  }

  /**
   * Returns the maximum outer width of this node.
   *
   * <p>A value of zero disables the maximum, allowing the node to grow to the natural width of its
   * content.
   *
   * @return the maximum width, or zero when unlimited
   */
  public float maxWidth() {
    return maxWidth;
  }

  /**
   * Sets the maximum outer width of this node.
   *
   * <p>When the content exceeds this width, the label wraps and the node grows vertically instead
   * of becoming wider. A value of zero disables the maximum.
   *
   * @param value the maximum width, or zero to disable it; must not be negative or infinite
   * @return this node for chaining
   */
  public LabelNode maxWidth(float value) {
    if (!Float.isFinite(value) || value < 0f) {
      throw new IllegalArgumentException("maxWidth must be finite and non-negative");
    }
    if (this.maxWidth == value) {
      return this;
    }
    this.maxWidth = value;
    if (label != null) {
      configureLabel();
      if (!isOwnedByNode()) {
        autoSizeToLabel();
      }
      invalidateLayout();
    }
    notifyStateChanged();
    return this;
  }

  /**
   * Sets the box color.
   *
   * @param value the new color; must not be null
   * @return this node for chaining
   */
  @Override
  public LabelNode color(Color value) {
    super.color(value);
    return this;
  }

  /**
   * Sets the font size used for the text.
   *
   * @param value the font size in points
   * @return this node for chaining
   */
  public LabelNode fontSize(int value) {
    this.fontSize = value;
    rebuildContent();
    notifyStateChanged();
    return this;
  }

  @Override
  public String typeId() {
    return TYPE;
  }

  @Override
  protected void buildContent() {
    label =
        new RichLabel(
            RichLabel.toRichText(text),
            DialogDesign.DIALOG_FONT_SPEC_NORMAL.withSize(fontSize).withColor(Color.WHITE));
    label.setAlignment(Align.center);
    configureLabel();
    addActor(label);
    if (!isOwnedByNode()) {
      autoSizeToLabel();
    }
  }

  @Override
  protected void layoutContent() {
    super.layoutContent();
    if (label != null) {
      boolean owned = isOwnedByNode();
      boolean wraps = owned || maxWidth > 0f;
      float preferredWidth = owned ? 0f : contentMaxWidth();
      boolean configurationChanged =
          label.getWrap() != wraps || label.getMaxPrefWidth() != preferredWidth;
      configureLabel();
      if (!owned && configurationChanged) {
        autoSizeToLabel();
      }
      float padding = 2f * CONTENT_PADDING;
      label.setSize(Math.max(0f, getWidth() - padding), Math.max(0f, getHeight() - padding));
      label.setPosition(CONTENT_PADDING, CONTENT_PADDING);
    }
  }

  private boolean isOwnedByNode() {
    return getParent() instanceof CanvasNode;
  }

  private void configureLabel() {
    boolean owned = isOwnedByNode();
    boolean wraps = owned || maxWidth > 0f;
    float preferredWidth = owned ? 0f : contentMaxWidth();
    if (label.getWrap() != wraps) {
      label.setWrap(wraps);
    }
    if (label.getMaxPrefWidth() != preferredWidth) {
      label.setMaxPrefWidth(preferredWidth);
    }
  }

  private float contentMaxWidth() {
    return maxWidth > 0f ? Math.max(1f, maxWidth - 2f * CONTENT_PADDING) : 0f;
  }

  @Override
  protected void setParent(Group parent) {
    super.setParent(parent);
    invalidateLayout();
  }

  private void autoSizeToLabel() {
    configureLabel();
    label.pack();
    float padding = 2f * CONTENT_PADDING;
    float width = label.getWidth() + padding;
    if (maxWidth > 0f) {
      width = Math.min(width, maxWidth);
    }
    size(width, label.getHeight() + padding);
  }

  boolean autoSizeIfContentUnbuilt() {
    if (isContentBuilt()) {
      return false;
    }
    ensureContentBuilt();
    autoSizeToLabel();
    return true;
  }

  @Override
  protected void drawBackground(Batch batch, float parentAlpha) {
    CanvasGraphics.fill(batch, color(), parentAlpha, getX(), getY(), getWidth(), getHeight());
    CanvasGraphics.outline(batch, BORDER, parentAlpha, getX(), getY(), getWidth(), getHeight(), 2f);
  }

  private static final Color BORDER = new Color(0.85f, 0.88f, 0.95f, 1f);

  @Override
  protected void writeProps(NodeState.Props props) {
    props.put(PROP_TEXT, text);
    props.put(PROP_FONT_SIZE, fontSize);
    props.put(PROP_MAX_WIDTH, maxWidth);
  }

  @Override
  protected void readProps(NodeState state) {
    this.text = state.prop(PROP_TEXT, id());
    this.fontSize = state.intProp(PROP_FONT_SIZE, fontSize);
    float storedMaxWidth = state.floatProp(PROP_MAX_WIDTH, 0f);
    this.maxWidth =
        Float.isFinite(storedMaxWidth) && storedMaxWidth > 0f ? storedMaxWidth : 0f;
    rebuildContent();
  }
}
