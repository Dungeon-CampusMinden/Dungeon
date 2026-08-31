package feature.canvas.nodes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import engine.utils.Scene2dElementFactory;
import engine.utils.logging.DungeonLogger;
import feature.canvas.CanvasGraphics;
import feature.canvas.CanvasNode;
import feature.canvas.CanvasNodeType;
import feature.canvas.NodeState;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
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
 * <h2>Serialization</h2>
 *
 * Callbacks cannot be serialized, so only the labels travel inside the {@link NodeState}. To make
 * an action node survive a canvas being closed and reopened - or being sent from the server as a
 * default node - register its actions once under a stable set id and construct the node with that
 * id:
 *
 * <pre>{@code
 * ActionNode.registerActionSet("myPuzzle.tools", List.of(...));
 * ActionNode tools = new ActionNode("tools", "myPuzzle.tools");
 * }</pre>
 *
 * <p>A node rebuilt from a state whose action set is unknown still renders its buttons, but they do
 * nothing and a warning is logged.
 */
public class ActionNode extends CanvasNode {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(ActionNode.class);

  /** Stable type id of this node type. */
  public static final String TYPE = "canvas.action";

  /** Prop key holding the id of the registered action set, if any. */
  public static final String PROP_ACTION_SET = "actionSet";

  /** Prop key holding the button labels, separated by {@value #LABEL_SEPARATOR}. */
  public static final String PROP_LABELS = "labels";

  /** Separator used to encode the button labels into a single prop value. */
  public static final String LABEL_SEPARATOR = "\u001f";

  private static final Map<String, List<Action>> ACTION_SETS = new ConcurrentHashMap<>();

  private static final float PADDING = 8f;
  private static final float BUTTON_HEIGHT = 34f;
  private static final float BUTTON_SPACING = 6f;

  private final List<Action> actions = new ArrayList<>();
  private final List<TextButton> buttons = new ArrayList<>();
  private String actionSetId;
  private Color color = new Color(0.94f, 0.94f, 0.94f, 0.96f);
  private int fontSize = 16;

  /**
   * A single button of an {@link ActionNode}.
   *
   * @param label the button caption
   * @param callback invoked with the owning node when the button is pressed
   */
  public record Action(String label, Consumer<ActionNode> callback) {

    /** Validates the action fields. */
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
   * Creates an action node from a registered action set.
   *
   * @param id unique node id within a canvas
   * @param actionSetId the id the actions were registered under, see {@link
   *     #registerActionSet(String, List)}
   */
  public ActionNode(String id, String actionSetId) {
    this(id, ACTION_SETS.getOrDefault(actionSetId, List.of()));
    this.actionSetId = actionSetId;
  }

  /**
   * Creates an action node from a state, used by the {@link CanvasNodeType} registry.
   *
   * @param state the state to rebuild from
   */
  public ActionNode(NodeState state) {
    this(state.id(), resolveActions(state));
    this.actionSetId = state.prop(PROP_ACTION_SET, null);
  }

  /** Registers this node type with the {@link CanvasNodeType} registry. */
  public static void registerType() {
    CanvasNodeType.register(TYPE, ActionNode::new);
  }

  /**
   * Registers a named set of actions so action nodes can be rebuilt from a {@link NodeState}.
   *
   * <p>Registering the same set id again replaces the previous actions, which makes it safe to call
   * this from level setup code that may run more than once.
   *
   * @param setId the stable id of the action set; must not be null or blank
   * @param actions the actions of the set; must not be null
   */
  public static void registerActionSet(String setId, List<Action> actions) {
    Objects.requireNonNull(setId, "setId");
    Objects.requireNonNull(actions, "actions");
    if (setId.isBlank()) {
      throw new IllegalArgumentException("action set id must not be blank");
    }
    ACTION_SETS.put(setId, List.copyOf(actions));
  }

  /**
   * Returns the actions registered under the given set id.
   *
   * @param setId the action set id
   * @return the registered actions, or an empty list when the id is unknown
   */
  public static List<Action> actionSet(String setId) {
    return setId == null ? List.of() : ACTION_SETS.getOrDefault(setId, List.of());
  }

  private static List<Action> resolveActions(NodeState state) {
    String setId = state.prop(PROP_ACTION_SET, null);
    List<Action> registered = actionSet(setId);
    if (!registered.isEmpty()) {
      return registered;
    }
    String labels = state.prop(PROP_LABELS, "");
    if (labels.isBlank()) {
      return List.of();
    }
    if (setId != null) {
      LOGGER.warn(
          "Action set '{}' of node '{}' is not registered; buttons will be inert",
          setId,
          state.id());
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
    this.color = new Color(value);
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
    CanvasGraphics.fill(batch, color, parentAlpha, getX(), getY(), getWidth(), getHeight());
    CanvasGraphics.outline(
        batch, Color.BLACK, parentAlpha, getX(), getY(), getWidth(), getHeight(), 2f);
  }

  @Override
  protected void writeProps(NodeState.Props props) {
    props.put(PROP_ACTION_SET, actionSetId);
    props.put(
        PROP_LABELS, String.join(LABEL_SEPARATOR, actions.stream().map(Action::label).toList()));
  }

  @Override
  protected void readProps(NodeState state) {
    String setId = state.prop(PROP_ACTION_SET, null);
    if (setId == null || Objects.equals(setId, actionSetId)) {
      return;
    }
    List<Action> registered = actionSet(setId);
    if (registered.isEmpty()) {
      return;
    }
    this.actionSetId = setId;
    this.actions.clear();
    this.actions.addAll(registered);
    rebuildContent();
  }
}
