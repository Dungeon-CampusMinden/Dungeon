package rooms.programming.level;

import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.level.DungeonLevel;
import engine.level.utils.LevelElement;
import engine.utils.Point;
import engine.utils.Vector2;
import engine.utils.components.draw.DepthLayer;
import engine.utils.components.draw.state.CharacterStateFactory;
import engine.utils.components.path.SimpleIPath;
import java.util.Arrays;
import java.util.Map;
import rooms.programming.modules.loops.LoopMaze;

/** Builds the remote corridors from the exact cells used by the terminal and executor. */
public final class ProgrammingMazeWorld {
  private ProgrammingMazeWorld() {}

  /**
   * Expands the authored rooms with disconnected, walled five-by-three maze cells.
   *
   * @param rooms the authored workshop and archive tiles
   * @param points named points including the remote maze origin
   * @return the complete physical level used by host and clients
   */
  public static LevelElement[][] layout(LevelElement[][] rooms, Map<String, Point> points) {
    Point origin = points.get("maze-origin");
    int width =
        Math.max(
            rooms[0].length,
            (int) origin.x()
                + (LoopMaze.cells().stream().mapToInt(LoopMaze.Cell::x).max().orElseThrow() + 1)
                    * LoopMaze.CELL_WIDTH
                + 2);
    int height =
        Math.max(
            rooms.length,
            (int) origin.y()
                + (LoopMaze.cells().stream().mapToInt(LoopMaze.Cell::y).max().orElseThrow() + 1)
                    * LoopMaze.CELL_HEIGHT
                + 2);
    LevelElement[][] result = new LevelElement[height][width];
    for (LevelElement[] row : result) Arrays.fill(row, LevelElement.SKIP);
    for (int y = 0; y < rooms.length; y++)
      System.arraycopy(rooms[y], 0, result[y], 0, rooms[y].length);
    for (LoopMaze.Cell cell : LoopMaze.cells()) {
      Point at = LoopMaze.world(origin, cell);
      for (int y = (int) at.y() - 1; y <= at.y() + LoopMaze.CELL_HEIGHT; y++)
        for (int x = (int) at.x() - 1; x <= at.x() + LoopMaze.CELL_WIDTH; x++)
          result[y][x] = LevelElement.WALL;
    }
    for (LoopMaze.Cell cell : LoopMaze.cells()) {
      Point at = LoopMaze.world(origin, cell);
      for (int y = (int) at.y(); y < at.y() + LoopMaze.CELL_HEIGHT; y++)
        for (int x = (int) at.x(); x < at.x() + LoopMaze.CELL_WIDTH; x++)
          result[y][x] = LevelElement.FLOOR;
    }
    return result;
  }

  static void spawn(DungeonLevel level) {
    Point origin = level.getPoint("maze-origin");
    // One inset floor plate identifies each canonical destination in the live world view.
    for (int index = 0; index < LoopMaze.checkpoints().size(); index++) {
      Point goal = LoopMaze.world(origin, LoopMaze.checkpoints().get(index).goal());
      waypoint(goal, index);
    }
    Entity monster = new Entity("programming-maze-monster");
    PositionComponent position =
        new PositionComponent(LoopMaze.world(origin, LoopMaze.monster()).translate(1.5f, 0.4f));
    position.scale(2f);
    monster.add(position);
    monster.add(
        new DrawComponent(
            CharacterStateFactory.createStateMachine(
                new SimpleIPath("character/monster/orc_warrior"))));
    Game.add(monster);
    Point pit = LoopMaze.world(origin, LoopMaze.pit());
    Entity abyss = new Entity("programming-maze-abyss");
    PositionComponent pitPosition = new PositionComponent(pit);
    pitPosition.scale(Vector2.of(LoopMaze.CELL_WIDTH, LoopMaze.CELL_HEIGHT));
    abyss.add(pitPosition);
    DrawComponent pitDraw =
        new DrawComponent(new SimpleIPath("dungeon/default/floor/pit_open.png"));
    pitDraw.depth(DepthLayer.Ground.depth());
    abyss.add(pitDraw);
    Game.add(abyss);
  }

  private static void waypoint(Point at, int index) {
    Entity entity = new Entity("programming-maze-waypoint-" + index);
    PositionComponent position = new PositionComponent(at);
    position.scale(Vector2.of(LoopMaze.CELL_WIDTH, LoopMaze.CELL_HEIGHT));
    entity.add(position);
    DrawComponent draw = new DrawComponent(new SimpleIPath("objects/pressureplate"), "off");
    draw.depth(DepthLayer.Ground.depth());
    draw.tintColor(0xFFCE73FF);
    entity.add(draw);
    Game.add(entity);
  }
}
