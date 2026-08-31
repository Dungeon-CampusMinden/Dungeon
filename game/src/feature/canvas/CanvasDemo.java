package feature.canvas;

import com.badlogic.gdx.graphics.Color;
import feature.canvas.nodes.LabelNode;
import feature.components.UIComponent;
import java.util.ArrayList;
import java.util.List;

/**
 * Hand-test entry point for the canvas framework.
 *
 * <p>Opens a canvas containing a few dummy nodes so that panning, zooming, clipping, selection,
 * dragging and the defaults/overlay merge model can be verified without a concrete puzzle.
 *
 * <p>The demo intentionally exposes {@link #unlockExtraNodes(boolean)}: it emulates the "player
 * found an item" trigger that grows the server side default node set from three to five. Because
 * the defaults are re-evaluated server side every time the canvas is opened, calling {@code
 * unlockExtraNodes(true)} and then reopening the canvas must show the two extra nodes
 * <em>while</em> every locally moved, added or deleted node stays exactly as the player left it.
 *
 * <p>Typical manual test:
 *
 * <ol>
 *   <li>{@code CanvasDemo.show(heroId)} — three nodes appear.
 *   <li>Move them around, add nodes with a custom node type, delete one.
 *   <li>Close the dialog, reopen it — the arrangement is restored.
 *   <li>{@code CanvasDemo.unlockExtraNodes(true)}, reopen — two more nodes appear on top of the
 *       restored arrangement.
 * </ol>
 */
public final class CanvasDemo {

  /** The canvas id used by the demo. */
  public static final String CANVAS_ID = "canvas-demo";

  private static volatile boolean extraNodesUnlocked = false;

  private CanvasDemo() {}

  /**
   * Returns whether the two additional default nodes are currently unlocked.
   *
   * @return true if the extended default set is active
   */
  public static boolean extraNodesUnlocked() {
    return extraNodesUnlocked;
  }

  /**
   * Toggles the two additional default nodes.
   *
   * <p>Takes effect the next time the demo canvas is opened.
   *
   * @param unlocked true to serve five default nodes, false to serve three
   */
  public static void unlockExtraNodes(boolean unlocked) {
    extraNodesUnlocked = unlocked;
  }

  /**
   * Builds (and registers) the demo canvas definition.
   *
   * @return the demo canvas definition
   */
  public static CanvasDefinition definition() {
    return CanvasMaker.lookup(CANVAS_ID).orElseGet(CanvasDemo::buildDefinition);
  }

  /**
   * Opens the demo canvas for the given hero.
   *
   * @param heroId the entity id of the hero opening the canvas
   * @param targetEntityIds the entities the dialog is shown for; empty means the hero only
   * @return the {@link UIComponent} holding the dialog
   */
  public static UIComponent show(int heroId, int... targetEntityIds) {
    return CanvasMaker.show(definition(), heroId, targetEntityIds);
  }

  private static CanvasDefinition buildDefinition() {
    return CanvasMaker.builder(CANVAS_ID)
        .title("Canvas Demo")
        .areaSize(900f, 560f)
        .options(o -> o.zoom(0.25f, 4f).grid(32f, true).snapToGrid(true))
        .nodes(CanvasDemo::currentDefaults)
        .onEvent("demoEvent", payload -> {})
        .build();
  }

  private static List<NodeState> currentDefaults() {
    List<NodeState> states = new ArrayList<>();

    // Plain base node: default rendering (bordered box showing the node id).
    states.add(
        NodeState.of(CanvasNode.TYPE_ID, "demo-plain", NodeOrigin.DEFAULT, 0f, 0f, 160f, 64f));

    // Custom rendering node.
    LabelNode label = new LabelNode("demo-label", "Custom Node");
    label.color(Color.valueOf("2f6f9f"));
    label.position(220f, 0f);
    states.add(label.toState());

    // Non-movable node: dragging it must not change its position.
    LabelNode anchored = new LabelNode("demo-anchor", "Anchored");
    anchored.color(Color.valueOf("9f4f2f"));
    anchored.position(110f, 160f);
    anchored.movable(false);
    anchored.deletable(false);
    states.add(anchored.toState());

    if (extraNodesUnlocked) {
      LabelNode extraOne = new LabelNode("demo-extra-1", "Unlocked A");
      extraOne.color(Color.valueOf("3f8f4f"));
      extraOne.position(0f, 320f);
      states.add(extraOne.toState());

      LabelNode extraTwo = new LabelNode("demo-extra-2", "Unlocked B");
      extraTwo.color(Color.valueOf("3f8f4f"));
      extraTwo.position(220f, 320f);
      states.add(extraTwo.toState());
    }

    return states;
  }
}
