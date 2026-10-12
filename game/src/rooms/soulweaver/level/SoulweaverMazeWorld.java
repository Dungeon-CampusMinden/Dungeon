package rooms.soulweaver.level;

import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.level.DungeonLevel;
import engine.level.utils.LevelElement;
import engine.utils.Point;
import engine.utils.components.draw.state.CharacterStateFactory;
import engine.utils.components.path.SimpleIPath;
import java.util.Arrays;
import java.util.Map;
import rooms.soulweaver.modules.loops.LoopMaze;

/** Builds the remote corridors from the exact cells used by the terminal and executor. */
public final class SoulweaverMazeWorld {
  private SoulweaverMazeWorld() {}

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
                + 6);
    int height =
        Math.max(
            rooms.length,
            (int) origin.y()
                + (LoopMaze.cells().stream().mapToInt(LoopMaze.Cell::y).max().orElseThrow() + 1)
                    * LoopMaze.CELL_HEIGHT
                + 6);
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
    // Walled maintenance bays show the machinery without adding cells to the golem's route.
    for (int[] bay : new int[][] {{15, -4, 5, 3}, {0, 8, 4, 3}}) {
      int left = (int) origin.x() + bay[0];
      int bottom = (int) origin.y() + bay[1];
      for (int y = bottom - 1; y <= bottom + bay[3]; y++)
        for (int x = left - 1; x <= left + bay[2]; x++)
          result[y][x] =
              x == left - 1 || x == left + bay[2] || y == bottom - 1 || y == bottom + bay[3]
                  ? LevelElement.WALL
                  : LevelElement.FLOOR;
    }
    // The winch occupies a service bay beyond the last working position, not the golem's path.
    Point winch = LoopMaze.world(origin, LoopMaze.checkpoints().getLast().goal());
    for (int y = (int) winch.y() - 1; y <= winch.y() + 6; y++)
      for (int x = (int) winch.x() + 5; x <= winch.x() + 10; x++)
        result[y][x] =
            y == winch.y() - 1 || y == winch.y() + 6 || x == winch.x() + 10
                ? LevelElement.WALL
                : LevelElement.FLOOR;
    return result;
  }

  static void spawn(DungeonLevel level) {
    Point origin = level.getPoint("maze-origin");
    Entity monster = new Entity("soulweaver-maze-monster");
    PositionComponent position =
        new PositionComponent(LoopMaze.world(origin, LoopMaze.monster()).translate(1.5f, 0.4f));
    position.scale(2f);
    monster.add(position);
    monster.add(
        new DrawComponent(
            CharacterStateFactory.createStateMachine(
                new SimpleIPath("character/monster/orc_warrior"))));
    Game.add(monster);
  }

  static java.util.List<Point> steamOutlets(Point origin) {
    return java.util.List.of(
        origin.translate(-4, 19), origin.translate(19.3f, 13), origin.translate(19.3f, 4));
  }
}
