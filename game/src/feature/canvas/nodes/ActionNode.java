package feature.canvas.nodes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import engine.utils.Scene2dElementFactory;
import feature.canvas.CanvasGraphics;
import feature.canvas.CanvasNode;
import feature.canvas.CanvasNodeType;
import feature.canvas.NodeState;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A node that renders a vertical list of buttons.
 *
 * <p>Each entry is an {@link Action}, a pair of a button label and a callback that receives the
 * node the button belongs to. This makes {@code ActionNode} the natural building block for canvas
 * side tool palettes: spawn buttons, reset buttons, "check solution" buttons and so on.
 *
 * <pre>{@code
 * ActionNode tools = new ActionNode("tools",
 *     List.of(new ActionNode.Action("Spawn", node -> spawn(node.canvas()))));
 * tools.sticky(true).movable(false);
 * }</pre>
 *
 * <p>Only button labels are stored in {@link NodeState}. The canvas definition supplies fresh node
 * prototypes when a dialog opens, preserving callbacks without putting them into serialized state.
 * A state-only fallback still renders its buttons, but they are inert.
 */
public class ActionNode extends CanvasNode {

  /** Stable type id of this node type. */
  public static final String TYPE = "canvas.action";

  /** Prop key holding the button labels, separated by {@value #LABEL_SEPARATOR}. */
  public static final String PROP_LABELS = "labels";

  /** Separator used to encode the button labels into a single prop value. */
  public static final String LABEL_SEPARATOR = "\u001f";

  private static final float PADDING = 8f;
  private static final float BUTTON_HEIGHT = 34f;
  private static final float BUTTON_SPACING = 6f;

  private final List<Action> actions = new ArrayList<>();
  private final List<TextButton> buttons = new ArrayList<>();
  private int fontSize = 16;

  /**
   * A single button of an {@link ActionNode}.
   *
   * @param label the button caption
   * @param callback invoked with the owning node when the button is pressed
   */
  public record Action(String label, Consumer<ActionNode> callback) {

    /**
     * Validates the action fields.
     *
     * @param label the button caption
     * @param callback invoked with the owning node when the button is pressed
     */
    public Action {
      Objects.requireNonNull(label, "label");
      Objects.requireNonNull(callback, "callback");
    }
  }

  /**
   * Creates an action node from an explicit action list.
   *
   * <p>The node sizes itself so that all buttons fit.
   *
   * @param id unique node id within a canvas
   * @param actions the buttons to render, in top to bottom order; must not be null
   */
  public ActionNode(String id, List<Action> actions) {
    super(id, 180f, 60f);
    this.actions.addAll(Objects.requireNonNull(actions, "actions"));
    setSize(width(), preferredHeight());
  }

  /**
   * Creates an action node from a state, used by the {@link CanvasNodeType} registry.
   *
   * @param state the state to rebuild from
   */
  public ActionNode(NodeState state) {
    this(state.id(), actionsFromLabels(state));
  }

  /** Registers this node type with the {@link CanvasNodeType} registry. */
  public static void registerType() {
    CanvasNodeType.register(TYPE, ActionNode::new);
  }

  private static List<Action> actionsFromLabels(NodeState state) {
    String labels = state.prop(PROP_LABELS, "");
    if (labels.isBlank()) {
      return List.of();
    }
    return List.of(labels.split(LABEL_SEPARATOR)).stream()
        .map(label -> new Action(label, node -> {}))
        .toList();
  }

  /**
   * Returns the actions of this node.
   *
   * @return an unmodifiable view of the actions, in render order
   */
  public List<Action> actions() {
    return List.copyOf(actions);
  }

  /**
   * Appends an action to this node and resizes it so the new button fits.
   *
   * @param label the button caption
   * @param callback invoked with this node when the button is pressed
   * @return this node for chaining
   */
  public ActionNode action(String label, Consumer<ActionNode> callback) {
    actions.add(new Action(label, callback));
    size(width(), preferredHeight());
    rebuildContent();
    notifyStateChanged();
    return this;
  }

  /**
   * Sets the background color of the node.
   *
   * @param value the new color; must not be null
   * @return this node for chaining
   */
  public ActionNode color(Color value) {
    super.color(value);
    return this;
  }

  /**
   * Sets the font size used for the button labels.
   *
   * @param value the font size in points
   * @return this node for chaining
   */
  public ActionNode fontSize(int value) {
    this.fontSize = value;
    rebuildContent();
    return this;
  }

  /**
   * Returns the height needed to render all buttons.
   *
   * @return the preferred node height
   */
  public float preferredHeight() {
    int count = Math.max(actions.size(), 1);
    return 2 * PADDING + count * BUTTON_HEIGHT + (count - 1) * BUTTON_SPACING;
  }

  @Override
  public String typeId() {
    return TYPE;
  }

  @Override
  protected void buildContent() {
    buttons.clear();
    for (Action action : actions) {
      TextButton button =
          Scene2dElementFactory.createButton(action.label(), "blue-outline", fontSize);
      button.addListener(
          new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
              action.callback().accept(ActionNode.this);
            }
          });
      buttons.add(button);
      addActor(button);
    }
  }

  @Override
  protected void layoutContent() {
    super.layoutContent();
    float buttonWidth = getWidth() - 2 * PADDING;
    float y = getHeight() - PADDING - BUTTON_HEIGHT;
    for (TextButton button : buttons) {
      button.setSize(buttonWidth, BUTTON_HEIGHT);
      button.setPosition(PADDING, y);
      y -= BUTTON_HEIGHT + BUTTON_SPACING;
    }
  }

  @Override
  protected void drawBackground(Batch batch, float parentAlpha) {
    CanvasGraphics.fill(batch, color(), parentAlpha, getX(), getY(), getWidth(), getHeight());
    CanvasGraphics.outline(
        batch, Color.BLACK, parentAlpha, getX(), getY(), getWidth(), getHeight(), 2f);
  }

  @Override
  protected void writeProps(NodeState.Props props) {
    props.put(
        PROP_LABELS, String.join(LABEL_SEPARATOR, actions.stream().map(Action::label).toList()));
  }

  @Override
  protected void readProps(NodeState state) {
    String encodedLabels = state.prop(PROP_LABELS, "");
    List<String> labels =
        encodedLabels.isBlank() ? List.of() : List.of(encodedLabels.split(LABEL_SEPARATOR));
    if (labels.size() == actions.size()
        && java.util.stream.IntStream.range(0, labels.size())
            .allMatch(index -> labels.get(index).equals(actions.get(index).label()))) {
      return;
    }
    List<Action> updated = new ArrayList<>(labels.size());
    for (int index = 0; index < labels.size(); index++) {
      Consumer<ActionNode> callback =
          index < actions.size() ? actions.get(index).callback() : node -> {};
      updated.add(new Action(labels.get(index), callback));
    }
    actions.clear();
    actions.addAll(updated);
    setSize(width(), preferredHeight());
    rebuildContent();
  }
}
