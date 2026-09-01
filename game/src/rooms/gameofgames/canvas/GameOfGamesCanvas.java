package rooms.gameofgames.canvas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import feature.canvas.CanvasArea;
import feature.canvas.CanvasDefinition;
import feature.canvas.CanvasMaker;
import feature.canvas.CanvasNode;
import feature.canvas.NodeOrigin;
import feature.canvas.NodeState;
import feature.canvas.nodes.ActionNode;
import feature.canvas.nodes.LabelNode;
import feature.canvas.nodes.SocketNode;
import feature.components.UIComponent;
import java.util.ArrayList;
import java.util.List;

/**
 * Example canvas used by the Game of Games escape room.
 *
 * <p>This is the in-game playground for the canvas framework: it registers a canvas definition with
 * a handful of dummy nodes so panning, zooming, clipping, selection, dragging and the
 * defaults/overlay merge model can be checked while actually playing the room.
 *
 * <p>The example deliberately models the "player found something" trigger through {@link
 * #unlockExtraNodes(boolean)}. Because the default nodes are evaluated on the server every time the
 * canvas is opened, unlocking and reopening the canvas must show the two additional nodes
 * <em>while</em> every locally moved, added or deleted node stays exactly as the player left it.
 *
 * <p>In-game test walkthrough:
 *
 * <ol>
 *   <li>Interact with the terminal in the room: three nodes appear.
 *   <li>Pan with the middle mouse button, zoom with the wheel, drag the nodes around. The anchored
 *       node must refuse to move.
 *   <li>Close the canvas and interact with the terminal again: the arrangement is restored.
 *   <li>Interact with the folder to unlock the extra nodes, then reopen the terminal: two more
 *       nodes appear on top of the restored arrangement.
 * </ol>
 */
public final class GameOfGamesCanvas {

  /** The canvas id used by this example. */
  public static final String CANVAS_ID = "gameofgames-canvas-example";

  /** Id of the action set backing the sticky tool palette. */
  public static final String TOOLS_ACTION_SET = "gameofgames.canvas.tools";

  private static final float AREA_WIDTH = 900f;
  private static final float AREA_HEIGHT = 560f;
  private static final float TOOLS_WIDTH = 200f;
  private static final float TOOLS_MARGIN = 12f;

  private static volatile boolean extraNodesUnlocked = false;

  private GameOfGamesCanvas() {}

  /**
   * Registers the example canvas definition.
   *
   * <p>Must be called on the server and on every client, because the client resolves the canvas
   * layout from this registry when the dialog is opened. Calling it more than once is a no-op.
   */
  public static void register() {
    ActionNode.registerActionSet(
        TOOLS_ACTION_SET,
        List.of(
            new ActionNode.Action("Spawn Node", node -> spawnLabel(node, false)),
            new ActionNode.Action("Spawn Sticky Node", node -> spawnLabel(node, true))));
    definition();
  }

  private static void spawnLabel(ActionNode source, boolean sticky) {
    CanvasArea area = source.canvas();
    if (area == null) {
      return;
    }
    String id = CanvasArea.newLocalId();
    LabelNode spawned = new LabelNode(id, sticky ? "Sticky" : "Node");
    spawned.color(sticky ? Color.valueOf("7a4fb0") : Color.valueOf("2f6f9f"));
    spawned.sticky(sticky);
    if (sticky) {
      // sticky coordinates are viewport coordinates
      spawned.position(TOOLS_MARGIN + TOOLS_WIDTH + TOOLS_MARGIN, AREA_HEIGHT / 2f);
    } else {
      Vector2 center = area.areaToWorld(AREA_WIDTH / 2f, AREA_HEIGHT / 2f);
      spawned.position(area.snap(center.x), area.snap(center.y));
    }
    area.addNode(spawned);
    area.bringToFront(spawned);
  }

  /**
   * Returns whether the two additional default nodes are currently unlocked.
   *
   * @return true if the extended default set is served
   */
  public static boolean extraNodesUnlocked() {
    return extraNodesUnlocked;
  }

  /**
   * Toggles the two additional default nodes.
   *
   * <p>Takes effect the next time the canvas is opened.
   *
   * @param unlocked true to serve five default nodes, false to serve three
   */
  public static void unlockExtraNodes(boolean unlocked) {
    extraNodesUnlocked = unlocked;
  }

  /**
   * Returns the example canvas definition, registering it on first use.
   *
   * @return the canvas definition
   */
  public static CanvasDefinition definition() {
    return CanvasMaker.lookup(CANVAS_ID).orElseGet(GameOfGamesCanvas::buildDefinition);
  }

  /**
   * Opens the example canvas for the given hero.
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
        .title("Rule Board")
        .areaSize(AREA_WIDTH, AREA_HEIGHT)
        .options(o -> o.zoom(0.25f, 4f).grid(32f, false).snapToGrid(false))
        .nodes(GameOfGamesCanvas::currentDefaults)
        .build();
  }

  private static List<NodeState> currentDefaults() {
    List<NodeState> states = new ArrayList<>();

    // Plain base node: default rendering (bordered box showing the node id).
    states.add(
        NodeState.of(CanvasNode.TYPE_ID, "rule-plain", NodeOrigin.DEFAULT, 0f, 0f, 160f, 64f));

    LabelNode custom = new LabelNode("rule-custom", "Custom Node");
    custom.color(Color.valueOf("2f6f9f"));
    custom.position(220f, 0f);
    states.add(custom.toState());

    LabelNode anchored = new LabelNode("rule-anchor", "Anchored");
    anchored.color(Color.valueOf("9f4f2f"));
    anchored.position(110f, 160f);
    anchored.movable(false);
    anchored.deletable(false);
    states.add(anchored.toState());

    // Sticky tool palette: pinned to the top-left of the viewport, unaffected by pan and zoom.
    ActionNode tools = new ActionNode("rule-tools", TOOLS_ACTION_SET);
    tools.sticky(true);
    tools.size(TOOLS_WIDTH, tools.preferredHeight());
    tools.position(TOOLS_MARGIN, AREA_HEIGHT - TOOLS_MARGIN - tools.height());
    tools.movable(false);
    tools.deletable(false);
    tools.selectable(false);
    states.add(tools.toState());

    if (extraNodesUnlocked) {
      LabelNode extraOne = new LabelNode("rule-extra-1", "Unlocked A");
      extraOne.color(Color.valueOf("3f8f4f"));
      extraOne.position(0f, 320f);
      states.add(extraOne.toState());

      LabelNode extraTwo = new LabelNode("rule-extra-2", "Unlocked B");
      extraTwo.color(Color.valueOf("3f8f4f"));
      extraTwo.position(220f, 320f);
      states.add(extraTwo.toState());
    }

    SocketNode socket = new SocketNode("rule-socket", "public static", 3);
    socket.position(440f, 0f);

    LabelNode var = new LabelNode("rule-socket-fix", "=");
    socket.socket(1, var).lockSocket(1, true);

    var = new LabelNode("rule-socket-fill-1", "String test");
    var.position(440f, 100f);
    states.add(var.toState());

    var = new LabelNode("rule-socket-fill-2", "\"Hello world! This is a very long string, so let's see how it looks.\"");
    var.position(440f, 200f);
    states.add(var.toState());

    socket.onNodeAdded("on-win", (socketNode, index, node) -> {
      boolean correct1 = socketNode.socket(0).map(n -> n.id().equals("rule-socket-fill-1")).orElse(false);
      boolean correct2 = socketNode.socket(2).map(n -> n.id().equals("rule-socket-fill-2")).orElse(false);
      if (correct1 && correct2) {
        socketNode.content("!! public static");
      } else {
        socketNode.content("public static");
      }
    });

    states.add(socket.toState());

    return states;
  }
}
