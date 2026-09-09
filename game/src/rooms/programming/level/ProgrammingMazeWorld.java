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
    storage(origin);
  }

  private static void storage(Point origin) {
    for (var cell :
        java.util.List.of(
            new LoopMaze.Cell(5, 2), new LoopMaze.Cell(1, 2), new LoopMaze.Cell(4, 6))) {
      Point at = LoopMaze.world(origin, cell);
      for (int i = 0; i < 2; i++) {
        Entity crate =
            ProgrammingCellarMachinery.prop(
                "stored-parts", at.translate(2 + i, .3f), "objects/crate/basic.png", .9f, .9f);
        crate.add(ProgrammingProps.chestCollider());
      }
    }
    Point pump = LoopMaze.world(origin, new LoopMaze.Cell(-1, 5));
    Entity kettle =
        ProgrammingCellarMachinery.prop(
            "pump", pump.translate(1, .2f), "objects/magic_kettle", 1.5f, 1.5f);
    kettle.add(ProgrammingProps.chestCollider());
    // Wall-mounted pipes and their outlets stay outside the five-by-three movement footprint.
    for (Point at : steamOutlets(origin)) {
      ProgrammingCellarMachinery.sheet("pipe", at, 352, 32, 16, 48, .65f, 2);
    }
    for (var cell :
        java.util.List.of(
            new LoopMaze.Cell(0, 0), new LoopMaze.Cell(3, 3), new LoopMaze.Cell(0, 6))) {
      Point at = LoopMaze.world(origin, cell).translate(.1f, -.1f);
      Entity torch = ProgrammingCellarMachinery.prop("lamp", at, "objects/torch", .8f, .8f);
      torch.name("programming-prop-torch-cellar-" + cell.x() + "-" + cell.y());
      torch.fetch(DrawComponent.class).orElseThrow().stateMachine().setState("on", null);
    }
  }

  static java.util.List<Point> steamOutlets(Point origin) {
    return java.util.List.of(
        origin.translate(-4, 16), origin.translate(19.3f, 10), origin.translate(19.3f, 1));
  }

  private static void waypoint(Point at, int index) {
    Entity entity = new Entity("programming-maze-waypoint-" + index);
    PositionComponent position = new PositionComponent(at.translate(.1f, .1f));
    position.scale(.8f);
    entity.add(position);
    DrawComponent draw = new DrawComponent(new SimpleIPath("objects/pressureplate"), "off");
    draw.depth(DepthLayer.Ground.depth());
    draw.tintColor(0xFFCE73FF);
    entity.add(draw);
    Game.add(entity);
  }
}
