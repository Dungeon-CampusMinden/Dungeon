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
import java.util.List;

/** Example canvas used by the Game of Games escape room. */
public final class GameOfGamesCanvas {

  private static final String CANVAS_ID = "gameofgames-canvas-example";
  private static final float AREA_WIDTH = 900f;
  private static final float AREA_HEIGHT = 560f;
  private static final float TOOLS_WIDTH = 200f;
  private static final float TOOLS_MARGIN = 12f;

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
    socket.position(440f, 0f);
    socket.onSocketChanged(GameOfGamesCanvas::onSocketChanged);

    LabelNode fixed = new LabelNode("rule-socket-fix", "=");
    socket.socket(1, fixed).lockSocket(1, true);

    LabelNode first = new LabelNode("rule-socket-fill-1", "String test");
    first.position(440f, 100f);
    nodes.add(first);

    LabelNode second =
        new LabelNode(
            "rule-socket-fill-2",
            "\"Hello world! This is a very long string,[n]so let's see how it looks.\"");
    second.position(440f, 200f);
    nodes.add(second);
    nodes.add(socket);
    return nodes;
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
}
