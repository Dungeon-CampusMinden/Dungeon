package rooms.programming.level;

import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.level.DungeonLevel;
import engine.level.elements.tile.DoorTile;
import engine.level.utils.Coordinate;
import engine.level.utils.LevelElement;
import engine.utils.Point;
import engine.utils.Vector2;
import engine.utils.components.draw.DepthLayer;
import engine.utils.components.path.SimpleIPath;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import rooms.programming.modules.decisions.DecisionMaze;

/** Six small threshold chambers share an outer return passage. Positions are Nox's feet. */
final class ProgrammingDecisionWorld {
  static final Point START = new Point(34, 54);

  static Point junction(int index) {
    return new Point(34, 60 + index * 10);
  }

  static LevelElement[][] layout(LevelElement[][] source, Map<String, Point> points) {
    LevelElement[][] result =
        new LevelElement[Math.max(source.length, 127)][Math.max(source[0].length, 57)];
    for (var row : result) Arrays.fill(row, LevelElement.SKIP);
    for (int y = 0; y < source.length; y++)
      System.arraycopy(source[y], 0, result[y], 0, source[y].length);
    floor(result, 34, 50, 38, 59);
    floor(result, 18, 54, 54, 57);
    floor(result, 18, 54, 22, 117);
    floor(result, 50, 54, 54, 117);
    for (int i = 0; i < 6; i++) {
      int y = (int) junction(i).y();
      floor(result, 26, y, 46, y + 2);
      floor(result, 26, y, 30, y + 7);
      floor(result, 42, y, 46, y + 7);
      floor(result, 18, y + 5, 54, y + 7);
      floor(result, 34, y + 5, 38, y + 12);
      for (int x : new int[] {26, 42}) {
        for (int dx = 0; dx < 5; dx++) result[y + 3][x + dx] = LevelElement.DOOR;
        String id = "act4-" + i + "-" + x;
        points.put(id + "-gate-start", new Point(x, y + 3));
        points.put(id + "-gate-end", new Point(x + 4, y + 3));
      }
    }
    // Shallow furnishing alcoves sit outside the five-tile travel lanes.
    for (int i = 0; i < 6; i++) {
      int y = 60 + i * 10;
      floor(result, 31, y + 5, 33, y + 8);
      floor(result, 39, y + 5, 41, y + 8);
    }
    floor(result, 30, 120, 44, 124);
    for (int y = 50; y < result.length - 1; y++)
      for (int x = 1; x < result[y].length - 1; x++)
        if (result[y][x] == LevelElement.FLOOR || result[y][x] == LevelElement.DOOR)
          for (int dy = -1; dy <= 1; dy++)
            for (int dx = -1; dx <= 1; dx++)
              if (result[y + dy][x + dx] == LevelElement.SKIP)
                result[y + dy][x + dx] = LevelElement.WALL;
    points.put("decisions-console", new Point(40, 54));
    points.put("decisions-start", START);
    points.put("decisions-heart", new Point(41, 122));
    return result;
  }

  private static void floor(LevelElement[][] grid, int x0, int y0, int x1, int y1) {
    for (int y = y0; y <= y1; y++) for (int x = x0; x <= x1; x++) grid[y][x] = LevelElement.FLOOR;
  }

  static List<Point> route(int index, DecisionMaze.Side side, boolean correct) {
    int x = side == DecisionMaze.Side.LEFT ? 26 : 42;
    float y = junction(index).y();
    return correct
        ? List.of(new Point(x, y), new Point(x, y + 5), new Point(34, y + 5), junction(index + 1))
        : List.of(
            new Point(x, y),
            new Point(x, y + 5),
            new Point(x == 26 ? 18 : 50, y + 5),
            new Point(x == 26 ? 18 : 50, 54),
            START);
  }

  static void open(DungeonLevel level, int index, DecisionMaze.Side side) {
    int x = side == DecisionMaze.Side.LEFT ? 26 : 42;
    for (int dx = 0; dx < 5; dx++)
      level
          .tileAt(new Coordinate(x + dx, (int) junction(index).y() + 3))
          .filter(DoorTile.class::isInstance)
          .map(DoorTile.class::cast)
          .ifPresent(DoorTile::open);
  }

  static void spawn(DungeonLevel level, ProgrammingDecisionRuntime runtime) {
    Entity book =
        prop(
            "console",
            level.getPoint("decisions-console"),
            "items/rpg/item_book_brown.png",
            1,
            1,
            false);
    book.add(new InteractionComponent(new Interaction((target, who) -> runtime.show(who), 3f)));
    for (int i = 0; i < 6; i++) {
      int n = i;
      float y = junction(i).y();
      for (int x : new int[] {25, 47}) {
        Entity torch =
            prop("torch-" + i + "-" + x, new Point(x, y + 3), "objects/torch", 1, 1, false);
        torch.name("programming-prop-torch-decisions-" + i + "-" + x);
        torch.fetch(DrawComponent.class).orElseThrow().stateMachine().setState("on", null);
      }
      Entity rune =
          prop(
              "rune-" + i, new Point(39, y + 5), "items/rpg/item_gem_amethyst.png", .8f, .8f, true);
      rune.add(
          new InteractionComponent(
              new Interaction(
                  (target, who) -> ProgrammingGolemRuntime.showText(who, DecisionMaze.event(n)),
                  2f)));
      Entity tablet =
          prop(
              "decision-tablet-" + i,
              new Point(39.5f, y),
              "items/rpg/item_scroll.png",
              .8f,
              .8f,
              true);
      tablet.add(new InteractionComponent(new Interaction((target, who) -> runtime.show(who), 3f)));
      rune.fetch(DrawComponent.class)
          .orElseThrow()
          .tintColor(
              new int[] {0x87B5DFFF, 0xADA1F7FF, 0xEFA26BFF, 0xDAC283FF, 0xEE9565FF, 0xFFC666FF}
                  [i]);
      switch (i) {
        case 0 -> {
          prop("drained-vessel", new Point(31.5f, y + 6), "objects/vase", 1, 1, false);
          prop("note", new Point(32.6f, y + 7), "items/rpg/item_scroll.png", .5f, .5f, true);
        }
        case 1 -> {
          for (int c = 0; c < 4; c++)
            prop(
                "source-crystal-" + c,
                new Point(31.3f + c * .45f, y + 6 + c % 2 * .7f),
                "items/rpg/item_gem_amethyst.png",
                .6f,
                .6f,
                true);
        }
        case 2 -> {
          prop("strength-stone", new Point(31.4f, y + 6), "objects/stone", 1.4f, 1.4f, false);
          prop("pick", new Point(32.7f, y + 7), "items/rpg/pickaxe_crusty.png", .7f, .7f, true);
        }
        case 3 -> {
          Entity kettle =
              prop("forge", new Point(31.4f, y + 6), "objects/magic_kettle", 1.3f, 1.3f, false);
          kettle.name("programming-prop-forge-kettle-decisions");
        }
        case 4 -> {
          prop(
              "vessel",
              new Point(31.4f, y + 6),
              "objects/cauldron/cauldron.png",
              1.4f,
              1.4f,
              false);
        }
        default -> {
          prop("seal", new Point(31.4f, y + 6), "items/rpg/item_book_brown.png", 1, 1, false);
        }
      }
      for (int x : new int[] {26, 42}) {
        Entity threshold =
            prop(
                "threshold-" + i + "-" + x,
                new Point(x, y + 3),
                "rooms/programming/sluice.png",
                5,
                .25f,
                true);
        threshold.fetch(DrawComponent.class).orElseThrow().tintColor(0xBFA26CFF);
      }
    }
    // The shrine stands beside Nox's arrival footprint so his sprite cannot hide the flame.
    prop("heart-plinth-left", new Point(39.6f, 121), "objects/stone", 1.6f, 1.6f, true);
    prop("heart-plinth-right", new Point(41.2f, 121), "objects/stone", 1.6f, 1.6f, true);
    for (float x : new float[] {39.6f, 42.5f})
      prop(
          "heart-offering-" + x,
          new Point(x, 120.2f),
          "items/rpg/item_gem_amethyst.png",
          .8f,
          .8f,
          true);
    Entity fire = prop("heart", new Point(40.3f, 121.5f), "objects/torch", 2, 2, false);
    fire.name("programming-prop-torch-decisions-heart");
    fire.fetch(DrawComponent.class).orElseThrow().stateMachine().setState("on", null);
    fire.add(new InteractionComponent(new Interaction((target, who) -> runtime.show(who), 4f)));
  }

  private static Entity prop(
      String name, Point at, String asset, float w, float h, boolean ground) {
    Entity entity = new Entity("programming-decisions-" + name);
    PositionComponent position = new PositionComponent(at);
    position.scale(Vector2.of(w, h));
    entity.add(position);
    DrawComponent draw = new DrawComponent(new SimpleIPath(asset));
    draw.depth(ground ? DepthLayer.Ground.depth() : DepthLayer.Player.depth());
    entity.add(draw);
    Game.add(entity);
    return entity;
  }
}
