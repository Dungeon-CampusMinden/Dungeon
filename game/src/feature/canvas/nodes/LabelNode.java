package feature.canvas.nodes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import engine.utils.Scene2dElementFactory;
import feature.canvas.CanvasGraphics;
import feature.canvas.CanvasNode;
import feature.canvas.CanvasNodeType;
import feature.canvas.NodeState;

/**
 * A simple node that renders a piece of text inside a colored box.
 *
 * <p>Besides being immediately useful for prototypes and demos, this class is the reference
 * implementation of the node serialization contract: it declares a stable {@link #typeId()},
 * registers a factory with {@link CanvasNodeType} and round-trips its text and color through {@link
 * NodeState#props()}.
 */
public class LabelNode extends CanvasNode {

  /** Stable type id of this node type. */
  public static final String TYPE = "canvas.label";

  /** Prop key holding the displayed text. */
  public static final String PROP_TEXT = "text";

  /** Prop key holding the box color as an RGBA8888 hex string. */
  public static final String PROP_COLOR = "color";

  /** Prop key holding the font size. */
  public static final String PROP_FONT_SIZE = "fontSize";

  private String text;
  private Color color = new Color(0.20f, 0.28f, 0.42f, 0.95f);
  private int fontSize = 18;
  private Label label;

  /**
   * Creates a label node.
   *
   * @param id unique node id within a canvas
   * @param text the text to display
   */
  public LabelNode(String id, String text) {
    super(id, 160f, 64f);
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
      label.setText(this.text);
    }
    notifyStateChanged();
    return this;
  }

  /**
   * Returns the box color.
   *
   * @return the color
   */
  public Color color() {
    return color;
  }

  /**
   * Sets the box color.
   *
   * @param value the new color; must not be null
   * @return this node for chaining
   */
  public LabelNode color(Color value) {
    this.color = new Color(value);
    notifyStateChanged();
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
    label = Scene2dElementFactory.createLabel(text, fontSize, Color.WHITE);
    label.setAlignment(Align.center);
    label.setWrap(true);
    addActor(label);
  }

  @Override
  protected void layoutContent() {
    super.layoutContent();
    if (label != null) {
      label.setSize(getWidth() - 8f, getHeight());
      label.setPosition(4f, 0f);
    }
  }

  @Override
  protected void drawBackground(Batch batch, float parentAlpha) {
    CanvasGraphics.fill(batch, color, parentAlpha, getX(), getY(), getWidth(), getHeight());
    CanvasGraphics.outline(batch, BORDER, parentAlpha, getX(), getY(), getWidth(), getHeight(), 2f);
  }

  private static final Color BORDER = new Color(0.85f, 0.88f, 0.95f, 1f);

  @Override
  protected void writeProps(NodeState.Props props) {
    props.put(PROP_TEXT, text);
    props.put(PROP_COLOR, color.toString());
    props.put(PROP_FONT_SIZE, fontSize);
  }

  @Override
  protected void readProps(NodeState state) {
    this.text = state.prop(PROP_TEXT, id());
    this.fontSize = state.intProp(PROP_FONT_SIZE, fontSize);
    String hex = state.prop(PROP_COLOR, null);
    if (hex != null) {
      try {
        this.color = Color.valueOf(hex);
      } catch (RuntimeException e) {
        // keep the current color when the stored value is not a valid color
      }
    }
    rebuildContent();
  }
}
