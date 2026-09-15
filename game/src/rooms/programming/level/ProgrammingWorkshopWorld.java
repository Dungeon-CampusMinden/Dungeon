package rooms.programming.level;

import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.level.DungeonLevel;
import engine.level.utils.LevelElement;
import engine.utils.Direction;
import engine.utils.Point;
import engine.utils.Vector2;
import engine.utils.components.draw.DepthLayer;
import engine.utils.components.draw.animation.SpritesheetConfig;
import engine.utils.components.path.SimpleIPath;
import feature.components.CollideComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import rooms.programming.modules.methods.MethodsRoute;

/** Contiguous workshop geometry derived from the executable method bodies. */
final class ProgrammingWorkshopWorld {
  static final int TILE_STEP = 1;
  private static final Point INITIAL = new Point(30, 26);
  private static final Point EXIT = new Point(34, 46);

  private static final boolean[] activated = new boolean[8];
  private static final int[] remaining = new int[8];
  private static final int[] supplied = new int[8];

  record ActionResult(boolean success, String reason, int value) {}

  private ProgrammingWorkshopWorld() {}

  /**
   * Locates the beginning of a station.
   *
   * @param station zero-based station index
   * @return feet position at the station start
   */
  static Point start(int station) {
    return station == 0 ? INITIAL : path(station - 1).getLast();
  }

  /**
   * The original method body turns relative to this station's approach.
   *
   * @param station zero-based station index
   * @return initial heading
   */
  static Direction startFacing(int station) {
    return station == 0 ? Direction.UP : endFacing(station - 1);
  }

  /**
   * Derives the path from the original method body.
   *
   * @param station zero-based station index
   * @return position after every instruction, including stationary actions
   */
  static List<Point> path(int station) {
    Point at = start(station);
    Direction facing = startFacing(station);
    List<Point> points = new ArrayList<>();
    for (var step : MethodsRoute.STATIONS.get(station).body()) {
      if (step.action() == MethodsRoute.Action.TURN) facing = turn(facing, step.direction());
      if (step.action() == MethodsRoute.Action.MOVE)
        at =
            at.translate(
                facing.x() * TILE_STEP * step.amount(), facing.y() * TILE_STEP * step.amount());
      points.add(at);
    }
    return List.copyOf(points);
  }

  static Direction endFacing(int station) {
    Direction result = startFacing(station);
    for (var step : MethodsRoute.STATIONS.get(station).body())
      if (step.action() == MethodsRoute.Action.TURN) result = turn(result, step.direction());
    return result;
  }

  static Direction turn(Direction facing, MethodsRoute.Direction turn) {
    return switch (turn) {
      case LEFT -> facing.turnLeft();
      case RIGHT -> facing.turnRight();
      case BACK -> facing.opposite();
      case NONE -> facing;
    };
  }

  /**
   * Builds two threshold chambers and a continuous rune and crystal passage.
   *
   * @param rooms existing archive and cellar layout
   * @param points named points updated with workshop markers
   * @return expanded layout containing the workshop
   */
  static LevelElement[][] layout(LevelElement[][] rooms, Map<String, Point> points) {
    LevelElement[][] result =
        new LevelElement[Math.max(rooms.length, 54)][Math.max(rooms[0].length, 43)];
    for (LevelElement[] row : result) Arrays.fill(row, LevelElement.SKIP);
    for (int y = 0; y < rooms.length; y++)
      System.arraycopy(rooms[y], 0, result[y], 0, rooms[y].length);
    // The archive entrance and cellar below this boundary retain their original geometry.
    for (int y = 23; y < result.length; y++)
      for (int x = 0; x < Math.min(90, result[y].length); x++) result[y][x] = LevelElement.SKIP;
    floor(result, 27, 23, 38, 33);
    floor(result, 30, 34, 40, 38);
    floor(result, 31, 39, 41, 52);
    // Each five-tile threshold exactly spans its chamber's only opening.
    for (int y : List.of(29, 33))
      for (int x = 27; x <= 38; x++)
        result[y][x] = x >= 30 && x <= 34 ? LevelElement.FLOOR : LevelElement.WALL;
    for (int y = 23; y < result.length - 1; y++)
      for (int x = 1; x < Math.min(89, result[y].length - 1); x++)
        if (result[y][x] == LevelElement.FLOOR)
          for (int dy = -1; dy <= 1; dy++)
            for (int dx = -1; dx <= 1; dx++)
              if (y + dy >= 22 && result[y + dy][x + dx] == LevelElement.SKIP)
                result[y + dy][x + dx] = LevelElement.WALL;
    for (int x = 29; x <= 35; x++) result[22][x] = LevelElement.DOOR;
    // The final altar leaves Nox beside the exit, which opens for the players.
    for (int x = 31; x <= 41; x++)
      result[49][x] = x >= 34 && x <= 38 ? LevelElement.DOOR : LevelElement.WALL;
    points.put("methods-console", new Point(28, 26.4f));
    points.put("prop-workbench-reserve", new Point(28, 26));
    points.put("methods-entry", new Point(30, 24));
    points.put("methods-home", start(0));
    points.put("methods-exit", EXIT);
    points.put("act3-gate-start", new Point(34, 49));
    points.put("act3-gate-end", new Point(38, 49));
    points.put("workshop-experiments", new Point(29.1f, 26.4f));
    points.remove("workshop-calibration");
    points.remove("workshop-power");
    points.remove("prop-workbench-experiments");
    points.remove("prop-workbench-calibration");
    points.remove("prop-workbench-power");
    return result;
  }

  private static void floor(LevelElement[][] layout, int minX, int minY, int maxX, int maxY) {
    for (int y = minY; y <= maxY; y++)
      for (int x = minX; x <= maxX; x++) layout[y][x] = LevelElement.FLOOR;
  }

  /**
   * Places existing workshop art, with all solid furniture outside the movement lanes.
   *
   * @param level shared room containing the workshop
   */
  static void spawn(DungeonLevel level) {
    for (var station : MethodsRoute.STATIONS) {
      Point at = actionPoint(station.index());
      Entity lamp =
          prop(
              "station-lamp-" + station.index(),
              lampPosition(station.index()),
              "objects/torch",
              1,
              1,
              false);
      lamp.name("programming-prop-torch-workshop-station-" + station.index());
      lamp.fetch(DrawComponent.class).orElseThrow().stateMachine().setState("on", null);
      switch (station.kind()) {
        case GATE -> gate(station.index());
        case RUNE -> {
          // Place stones beside Nox's hands so his large north-facing sprite cannot hide them.
          Point stone =
              at.translate(
                  endFacing(station.index()) == Direction.UP ? 4.5f : 5.5f,
                  endFacing(station.index()) == Direction.UP ? 3 : .5f);
          stone("rune-base-" + station.index(), stone, 1.5f);
          Entity rune = new Entity("programming-methods-rune-" + station.index());
          PositionComponent position = new PositionComponent(stone.translate(.5f, .65f));
          position.scale(.5f);
          rune.add(position);
          DrawComponent draw =
              new DrawComponent(
                  new SimpleIPath("spritesheets/runes.png"),
                  new SpritesheetConfig(16, 0, 1, 1, 16, 16));
          draw.depth(DepthLayer.Ground.depth() + 1);
          draw.tintColor(0x667D8FFF);
          rune.add(draw);
          Game.add(rune);
        }
        case COLLECT -> {
          for (int i = 0; i < station.amount(); i++)
            prop(
                "crystal-" + station.index() + "-" + i,
                at.translate(.5f + i * .7f, -1),
                "items/rpg/item_gem_amethyst.png",
                .8f,
                .8f,
                false);
        }
        case ALTAR -> {
          for (int i = 0; i < 3; i++)
            stone(
                "altar-" + station.index() + "-" + i,
                altarPosition(station.index()).translate(i, 0),
                1);
          float socketSize = .45f;
          float socketSpacing = .55f;
          float firstSocket = (3 - socketSize - (station.amount() - 1) * socketSpacing) / 2;
          for (int i = 0; i < station.amount(); i++)
            prop(
                "socket-" + station.index() + "-" + i,
                altarPosition(station.index()).translate(firstSocket + i * socketSpacing, .5f),
                "items/rpg/item_gem_quartz.png",
                socketSize,
                socketSize,
                false);
        }
      }
    }
    ironBars("exit-door", new Point(34, 49), false);
    for (int station : List.of(6, 7)) {
      prop(
          "altar-link-" + station,
          new Point(station == 6 ? 32 : 40, 47.5f),
          "dungeon/default/floor/floor_1.png",
          .2f,
          2,
          true);
      tint("altar-link-" + station, 0x4E7F9FFF);
    }
    resetAll();
  }

  static Point actionPoint(int station) {
    var body = MethodsRoute.STATIONS.get(station).body();
    var positions = path(station);
    for (int i = 0; i < body.size(); i++) {
      var action = body.get(i).action();
      if (action != MethodsRoute.Action.MOVE && action != MethodsRoute.Action.TURN)
        return positions.get(i);
    }
    return positions.getLast();
  }

  private static Entity prop(
      String name, Point at, String asset, float width, float height, boolean ground) {
    Entity entity = new Entity("programming-methods-" + name);
    PositionComponent position = new PositionComponent(at);
    position.scale(Vector2.of(width, height));
    entity.add(position);
    DrawComponent draw = new DrawComponent(new SimpleIPath(asset));
    draw.depth(ground ? DepthLayer.Ground.depth() : DepthLayer.Player.depth());
    entity.add(draw);
    Game.add(entity);
    return entity;
  }

  /** Places the atlas's low stone plinth without stretching its pixel proportions. */
  private static void stone(String name, Point at, float scale) {
    Entity entity = new Entity("programming-methods-" + name);
    PositionComponent position = new PositionComponent(at);
    position.scale(scale);
    entity.add(position);
    DrawComponent draw =
        new DrawComponent(
            new SimpleIPath("spritesheets/FD_Dungeon_Free.png"),
            new SpritesheetConfig(272, 144, 1, 1, 16, 16));
    draw.depth(DepthLayer.Ground.depth());
    entity.add(draw);
    Game.add(entity);
  }

  /** Performs an action on the nearest reachable object, independent of the original route. */
  static ActionResult perform(MethodsRoute.Action action, Point position, int amount) {
    Optional<Integer> target = stationAt(action, position);
    if (target.isEmpty()) return new ActionResult(false, "Kein passendes Objekt in Reichweite.", 0);
    int station = target.orElseThrow();
    switch (action) {
      case OPEN_GATE -> {
        activated[station] = true;
        tint("gate-" + station, 0xFFFFFF00);
        Game.levelEntities()
            .filter(entity -> entity.name().equals("programming-methods-gate-" + station))
            .forEach(entity -> entity.remove(CollideComponent.class));
      }
      case ACTIVATE_RUNE -> {
        activated[station] = true;
        tint("rune-" + station, 0xA8FFFFFF);
        for (int i = 0; i < remaining[station + 2]; i++)
          tint("crystal-" + (station + 2) + "-" + i, 0xFFFFFFFF);
      }
      case COLLECT -> {
        if (!activated[station - 2])
          return new ActionResult(
              false, "Der Runenstein hat dieses Kristallfeld noch nicht freigegeben.", 0);
        int collected = remaining[station];
        remaining[station] = 0;
        for (int i = 0; i < collected; i++) tint("crystal-" + station + "-" + i, 0xFFFFFF00);
        return new ActionResult(true, "", collected);
      }
      case PLACE -> {
        int capacity = MethodsRoute.STATIONS.get(station).amount() - supplied[station];
        if (amount < 0 || amount > capacity)
          return new ActionResult(
              false, "Dieser Altar hat noch " + capacity + " freie Fassungen.", 0);
        for (int i = supplied[station]; i < supplied[station] + amount; i++)
          tint("socket-" + station + "-" + i, 0xBDA0FFFF);
        supplied[station] += amount;
        if (supplied[station] == MethodsRoute.STATIONS.get(station).amount())
          tint("altar-link-" + station, 0xBDA0FFFF);
      }
      default -> {
        return new ActionResult(false, "Kein Weltbefehl.", 0);
      }
    }
    return new ActionResult(true, "", amount);
  }

  /** Reach is measured from each object's working position at Nox's hands and feet. */
  static Optional<Integer> stationAt(MethodsRoute.Action action, Point position) {
    MethodsRoute.Kind kind =
        switch (action) {
          case OPEN_GATE -> MethodsRoute.Kind.GATE;
          case ACTIVATE_RUNE -> MethodsRoute.Kind.RUNE;
          case COLLECT -> MethodsRoute.Kind.COLLECT;
          case PLACE -> MethodsRoute.Kind.ALTAR;
          default -> null;
        };
    int nearest = -1;
    float distance = 1.25f;
    for (var station : MethodsRoute.STATIONS) {
      if (station.kind() != kind) continue;
      float candidate = Point.calculateDistance(position, actionPoint(station.index()));
      if (candidate < distance) {
        nearest = station.index();
        distance = candidate;
      }
    }
    return nearest < 0 ? Optional.empty() : Optional.of(nearest);
  }

  /** The test rig restores every object before a run, including fields behind inactive runes. */
  static void resetAll() {
    Arrays.fill(activated, false);
    Arrays.fill(supplied, 0);
    Arrays.fill(remaining, 0);
    for (var station : MethodsRoute.STATIONS) {
      int index = station.index();
      switch (station.kind()) {
        case GATE -> {
          tint("gate-" + index, 0xFFFFFFFF);
          Game.levelEntities()
              .filter(entity -> entity.name().equals("programming-methods-gate-" + index))
              .forEach(entity -> entity.add(gateCollider(index)));
        }
        case RUNE -> tint("rune-" + index, 0x667D8FFF);
        case COLLECT -> {
          remaining[index] = station.amount();
          for (int i = 0; i < station.amount(); i++) tint("crystal-" + index + "-" + i, 0xFFFFFF00);
        }
        case ALTAR -> {
          tint("altar-link-" + index, 0x4E7F9FFF);
          for (int i = 0; i < station.amount(); i++) tint("socket-" + index + "-" + i, 0xFFFFFFFF);
        }
      }
    }
    tint("exit-door", 0xFFFFFFFF);
  }

  static boolean solved() {
    return MethodsRoute.STATIONS.stream()
        .filter(station -> station.kind() == MethodsRoute.Kind.ALTAR)
        .allMatch(station -> supplied[station.index()] == station.amount());
  }

  static void openExit() {
    tint("exit-door", 0xFFFFFF00);
  }

  private static Point altarPosition(int station) {
    return actionPoint(station).translate(-3, .2f);
  }

  private static Point lampPosition(int station) {
    return switch (station) {
      case 0, 1 -> actionPoint(station).translate(-1.5f, 2.5f);
      case 2 -> new Point(30.5f, 36);
      case 3 -> new Point(31.5f, 39);
      case 4 -> new Point(40.5f, 39);
      case 5 -> new Point(40.5f, 41);
      case 6, 7 -> new Point(40.5f, actionPoint(station).y() + .5f);
      default -> throw new IllegalArgumentException("Unknown workshop station " + station);
    };
  }

  private static Point gatePosition(int station) {
    if (station != 0 && station != 1)
      throw new IllegalArgumentException("Not a gate station " + station);
    return start(station).translate(0, 3);
  }

  private static void gate(int station) {
    Entity entity = ironBars("gate-" + station, gatePosition(station), false);
    entity.add(gateCollider(station));
  }

  /**
   * Small lattice panels retain source pixel density; the collider fills the wall opening.
   *
   * @param name entity name suffix
   * @param at lower-left gate anchor
   * @param side whether the gate fills a vertical passage
   * @return gate entity to receive the collider
   */
  private static Entity ironBars(String name, Point at, boolean side) {
    Entity entity = new Entity("programming-methods-" + name);
    entity.add(new PositionComponent(at));
    Game.add(entity);
    int columns = side ? 1 : 5;
    int rows = side ? 4 : 3;
    float rowHeight = side ? 1 : .5f;
    Point base = side ? at : at.translate(0, -.5f);
    for (int row = 0; row < rows; row++)
      for (int column = 0; column < columns; column++)
        ironPanel(
            name + "-bars-" + row + "-" + column,
            base.translate(column, row * rowHeight),
            392,
            !side && row == 0 ? 344 : 336,
            16,
            side ? 16 : 8);
    // The metal header remains on the masonry when the portcullis lifts.
    for (int column = 0; column < columns; column++)
      ironPanel(
          name + "-frame-" + column, base.translate(column, rows * rowHeight), 392, 326, 16, 4);
    return entity;
  }

  private static void ironPanel(String name, Point at, int x, int y, int width, int height) {
    Entity entity = new Entity("programming-methods-" + name);
    PositionComponent position = new PositionComponent(at);
    position.scale(Math.min(width, height) / 16f);
    entity.add(position);
    DrawComponent draw =
        new DrawComponent(
            new SimpleIPath("spritesheets/FD_Dungeon_Free.png"),
            new SpritesheetConfig(x, y, 1, 1, width, height));
    draw.depth(DepthLayer.Player.depth());
    entity.add(draw);
    Game.add(entity);
  }

  private static CollideComponent gateCollider(int station) {
    return new CollideComponent(Vector2.of(0, 0), Vector2.of(5, 1));
  }

  private static void tint(String suffix, int color) {
    Game.levelEntities()
        .filter(
            entity ->
                entity.name().equals("programming-methods-" + suffix)
                    || entity.name().startsWith("programming-methods-" + suffix + "-bars-"))
        .forEach(
            entity -> entity.fetch(DrawComponent.class).ifPresent(draw -> draw.tintColor(color)));
  }
}
