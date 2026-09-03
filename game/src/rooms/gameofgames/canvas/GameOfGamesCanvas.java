package rooms.gameofgames.canvas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import feature.canvas.CanvasArea;
import feature.canvas.CanvasContext;
import feature.canvas.CanvasDefinition;
import feature.canvas.CanvasMaker;
import feature.canvas.CanvasNode;
import feature.canvas.nodes.ActionNode;
import feature.canvas.nodes.LabelNode;
import feature.canvas.nodes.SocketNode;
import feature.components.UIComponent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/** Example canvas used by the Game of Games escape room. */
public final class GameOfGamesCanvas {

  private static final String CANVAS_ID = "gameofgames-canvas-example";
  private static final float AREA_WIDTH = 900f;
  private static final float AREA_HEIGHT = 560f;
  private static final float TOOLS_WIDTH = 200f;
  private static final float TOOLS_MARGIN = 12f;

  private static final HashMap<String, Integer> VALUE_MAP = new HashMap<>();

  private static final CanvasDefinition CANVAS =
      CanvasMaker.define(
          CANVAS_ID,
          canvas ->
              canvas
                  .title("Rule Board [img=items/rpg/item_book_black_lines.png]")
                  .areaSize(AREA_WIDTH, AREA_HEIGHT)
                  .options(o -> o.zoom(0.25f, 4f).grid(32f, false).snapToGrid(false))
                  .nodes(GameOfGamesCanvas::nodes));

  private static boolean extraNodesUnlocked;

  private GameOfGamesCanvas() {}

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
   * @param unlocked true to serve the additional nodes
   */
  public static void unlockExtraNodes(boolean unlocked) {
    extraNodesUnlocked = unlocked;
  }

  /**
   * Opens the example canvas for the given hero.
   *
   * @param heroId the entity id of the hero opening the canvas
   * @param targetEntityIds the entities that should see the dialog
   * @return the component holding the dialog
   */
  public static UIComponent show(int heroId, int... targetEntityIds) {
    return CANVAS.open(heroId, targetEntityIds);
  }

  private static List<CanvasNode> nodes(CanvasContext context) {
    List<CanvasNode> nodes = new ArrayList<>();

    CanvasNode plain = new CanvasNode("rule-plain", 160f, 64f);
    plain.position(0f, 0f);
    nodes.add(plain);

    LabelNode custom = new LabelNode("rule-custom", "Custom Node");
    custom.color(Color.valueOf("2f6f9f"));
    custom.position(220f, 0f);
    nodes.add(custom);

    LabelNode anchored = new LabelNode("rule-anchor", "Anchored");
    anchored.color(Color.valueOf("9f4f2f"));
    anchored.position(110f, 160f);
    anchored.movable(false);
    anchored.deletable(false);
    nodes.add(anchored);

    ActionNode tools =
        new ActionNode(
            "rule-tools",
            List.of(
                new ActionNode.Action("Spawn Node", node -> spawnLabel(node, false)),
                new ActionNode.Action("Spawn Sticky Node", node -> spawnLabel(node, true))));
    tools.sticky(true);
    tools.size(TOOLS_WIDTH, tools.preferredHeight());
    tools.position(TOOLS_MARGIN, AREA_HEIGHT - TOOLS_MARGIN - tools.height());
    tools.movable(false);
    tools.deletable(false);
    tools.selectable(false);
    nodes.add(tools);

    if (context.prototypeRun() || extraNodesUnlocked) {
      LabelNode extraOne = new LabelNode("rule-extra-1", "Unlocked A");
      extraOne.color(Color.valueOf("3f8f4f"));
      extraOne.position(0f, 320f);
      nodes.add(extraOne);

      LabelNode extraTwo = new LabelNode("rule-extra-2", "Unlocked B");
      extraTwo.color(Color.valueOf("3f8f4f"));
      extraTwo.position(220f, 320f);
      nodes.add(extraTwo);
    }

    SocketNode socket = new SocketNode("rule-socket", "public static", 3);
    socket.position(40f, 500f);
    socket.onSocketChanged(GameOfGamesCanvas::onSocketChanged);

    LabelNode fixed = new LabelNode("rule-socket-fix", "=");
    socket.socket(1, fixed).lockSocket(1, true);

    LabelNode first = new LabelNode("rule-socket-fill-1", "String test");
    first.position(40f, 400f);
    nodes.add(first);

    LabelNode second =
        new LabelNode(
            "rule-socket-fill-2",
            "\"Hello world! This is a very long string,[n]so let's see how it looks.\"");
    second.position(40f, 300f);
    nodes.add(second);
    nodes.add(socket);


    SocketNode equi1 = new SocketNode("equation1", "=", 2).placeSelfBefore(1).onSocketChanged(GameOfGamesCanvas::checkSocketEquation);
    SocketNode equi2 = new SocketNode("equation2", "=", 2).placeSelfBefore(1).onSocketChanged(GameOfGamesCanvas::checkSocketEquation);
    SocketNode equi3 = new SocketNode("equation3", "=", 2).placeSelfBefore(1).onSocketChanged(GameOfGamesCanvas::checkSocketEquation);
    SocketNode equi4 = new SocketNode("equation4", "=", 2).placeSelfBefore(1).onSocketChanged(GameOfGamesCanvas::checkSocketEquation);

    equi1.position(800f, 0f).size(50f, 50f);
    equi2.position(800f, 100f).size(50f, 50f);
    equi3.position(800f, 200f).size(50f, 50f);
    equi4.position(800f, 300f).size(50f, 50f);

    nodes.add(equi1);
    nodes.add(equi2);
    nodes.add(equi3);
    nodes.add(equi4);

    nodes.add(createValueNode("-9.33 + (7 / 3) * 4", 0));
    nodes.add(createValueNode("21", 21));

    nodes.add(createValueNode("24 + 2 * 3", 30));
    nodes.add(createValueNode("10^2", 100));

    nodes.add(createValueNode("100 / 300 - 1 / 3", 0));
    nodes.add(createValueNode("62 / 2 - 1", 30));

    nodes.add(createValueNode("Amount of cm[n]in a meter", 100));
    nodes.add(createValueNode("7 * 3", 21));



    return nodes;
  }

  private static int yOffset = 0;

  private static LabelNode createValueNode(String text, int value) {
    LabelNode node = new LabelNode(CanvasArea.newLocalId(), text);
    node.position(650f, yOffset);
    VALUE_MAP.put(text, value);
    yOffset += 100;
    return node;
  }

  private static void spawnLabel(ActionNode source, boolean sticky) {
    CanvasArea area = source.canvas();
    if (area == null) {
      return;
    }
    LabelNode spawned = new LabelNode(CanvasArea.newLocalId(), sticky ? "Sticky" : "Node");
    spawned.color(sticky ? Color.valueOf("7a4fb0") : Color.valueOf("2f6f9f"));
    spawned.sticky(sticky);
    if (sticky) {
      spawned.position(TOOLS_MARGIN + TOOLS_WIDTH + TOOLS_MARGIN, AREA_HEIGHT / 2f);
    } else {
      Vector2 center = area.areaToWorld(AREA_WIDTH / 2f, AREA_HEIGHT / 2f);
      spawned.position(area.snap(center.x), area.snap(center.y));
    }
    area.addNode(spawned);
    area.bringToFront(spawned);
  }

  private static void onSocketChanged(
      SocketNode socketNode, int index, CanvasNode node, boolean added) {
    boolean correct1 =
        socketNode.socket(0).map(n -> n.id().equals("rule-socket-fill-1")).orElse(false);
    boolean correct2 =
        socketNode.socket(2).map(n -> n.id().equals("rule-socket-fill-2")).orElse(false);
    socketNode.color(correct1 && correct2 ? Color.GREEN : LabelNode.DEFAULT_COLOR);
  }

  private static void checkSocketEquation(SocketNode socketNode, int index, CanvasNode node, boolean added) {
    if(socketNode.socketedNodes().size() < 2) {
      socketNode.color(LabelNode.DEFAULT_COLOR);
      return;
    }

    LabelNode left = (LabelNode) socketNode.socketedNodes().get(0);
    LabelNode right = (LabelNode) socketNode.socketedNodes().get(1);

    Integer leftValue = VALUE_MAP.get(left.text());
    Integer rightValue = VALUE_MAP.get(right.text());

    boolean correct = leftValue != null && rightValue != null && leftValue.equals(rightValue);
    socketNode.color(correct ? Color.GREEN : LabelNode.DEFAULT_COLOR);
  }
}
