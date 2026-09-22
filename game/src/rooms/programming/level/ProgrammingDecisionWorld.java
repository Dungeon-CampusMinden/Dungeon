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
import engine.utils.components.draw.animation.SpritesheetConfig;
import engine.utils.components.path.SimpleIPath;
import feature.components.CollideComponent;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import rooms.programming.modules.decisions.DecisionMaze;

/** Six rune workshops have gated onward passages and separate outer return galleries. */
final class ProgrammingDecisionWorld {
  static final Point START = new Point(34, 55);
  private static final int[] LEFT_X = {28, 27, 28, 26, 28, 27};
  private static final int[] RIGHT_X = {40, 41, 40, 42, 41, 40};
  private static final int LEFT_RETURN = 20;
  private static final int RIGHT_RETURN = 48;
  private static final int[] JUNCTION_Y = {61, 70, 80, 89, 99, 108, 118};

  static Point junction(int index) {
    return new Point(34, JUNCTION_Y[index]);
  }

  /**
   * @param index current rune
   * @param left whether to locate the left entrance
   * @return center of the physical choice door
   */
  static Point choicePoint(int index, boolean left) {
    return new Point((left ? LEFT_X[index] : RIGHT_X[index]) + 2.5f, JUNCTION_Y[index] + 3.5f);
  }

  static LevelElement[][] layout(LevelElement[][] source, Map<String, Point> points) {
    LevelElement[][] result =
        new LevelElement[Math.max(source.length, 124)][Math.max(source[0].length, 57)];
    for (var row : result) Arrays.fill(row, LevelElement.SKIP);
    for (int y = 0; y < source.length; y++)
      System.arraycopy(source[y], 0, result[y], 0, source[y].length);
    floor(result, 34, 50, 38, 63);
    floor(result, LEFT_RETURN, 55, RIGHT_RETURN + 4, 57);
    floor(result, LEFT_RETURN, 55, LEFT_RETURN + 4, 114);
    floor(result, RIGHT_RETURN, 55, RIGHT_RETURN + 4, 114);
    for (int i = 0; i < 6; i++) {
      int y = JUNCTION_Y[i];
      floor(result, LEFT_X[i], y, RIGHT_X[i] + 4, y + 2);
      floor(result, LEFT_X[i], y, LEFT_X[i] + 4, y + 6);
      floor(result, RIGHT_X[i], y, RIGHT_X[i] + 4, y + 6);
      floor(result, LEFT_RETURN, y + 4, RIGHT_RETURN + 4, y + 6);
      floor(result, 34, y + 4, 38, JUNCTION_Y[i + 1] + 2);
      // A full wall row separates these bays from the branch exits below.
      floor(result, 32, y + 8, 33, y + (i % 2 == 0 ? 8 : 9));
      floor(result, 39, y + 8, 40, y + (i % 2 == 0 ? 8 : 9));
      // Lamps have real floor recesses, outside the swept return lanes.
      int nearLamp = i % 2 == 0 ? 18 : 53;
      int farLamp = i % 2 == 0 ? 53 : 18;
      floor(result, nearLamp, y + 1, nearLamp + 1, y + 3);
      int farLampY = y + (i == 5 ? 1 : 7);
      floor(result, farLamp, farLampY, farLamp + 1, farLampY + 2);
      for (int x : new int[] {LEFT_X[i], RIGHT_X[i]})
        gate(result, points, "act4-" + i + "-entry-" + x, x, y + 3, x + 4, y + 3);
      for (int x : outlets(i))
        gate(result, points, "act4-" + i + "-outlet-" + x, x, y + 4, x, y + 6);
    }
    floor(result, 32, 118, 44, 121);
    for (int y = 50; y < result.length - 1; y++)
      for (int x = 1; x < result[y].length - 1; x++)
        if (result[y][x] == LevelElement.FLOOR || result[y][x] == LevelElement.DOOR)
          for (int dy = -1; dy <= 1; dy++)
            for (int dx = -1; dx <= 1; dx++)
              if (result[y + dy][x + dx] == LevelElement.SKIP)
                result[y + dy][x + dx] = LevelElement.WALL;
    points.put("decisions-start", START);
    points.put("decisions-heart", new Point(41, 120));
    return result;
  }

  private static void floor(LevelElement[][] grid, int x0, int y0, int x1, int y1) {
    for (int y = y0; y <= y1; y++) for (int x = x0; x <= x1; x++) grid[y][x] = LevelElement.FLOOR;
  }

  private static void gate(
      LevelElement[][] grid,
      Map<String, Point> points,
      String name,
      int x0,
      int y0,
      int x1,
      int y1) {
    for (int y = y0; y <= y1; y++) for (int x = x0; x <= x1; x++) grid[y][x] = LevelElement.DOOR;
    points.put(name + "-gate-start", new Point(x0, y0));
    points.put(name + "-gate-end", new Point(x1, y1));
  }

  static List<Point> route(int index, DecisionMaze.Side side, boolean correct) {
    boolean left = side == DecisionMaze.Side.LEFT;
    int x = left ? LEFT_X[index] : RIGHT_X[index];
    int back = left ? LEFT_RETURN : RIGHT_RETURN;
    float y = junction(index).y();
    return correct
        ? List.of(new Point(x, y), new Point(x, y + 4), new Point(34, y + 4), junction(index + 1))
        : List.of(
            new Point(x, y),
            new Point(x, y + 4),
            new Point(back, y + 4),
            new Point(back, START.y()),
            START);
  }

  /**
   * Reveals exactly one exit after a choice.
   *
   * @param level shared labyrinth
   * @param index current rune
   * @param side selected branch
   * @param correct whether the branch leads onward
   */
  static void open(DungeonLevel level, int index, DecisionMaze.Side side, boolean correct) {
    boolean left = side == DecisionMaze.Side.LEFT;
    int x = left ? LEFT_X[index] : RIGHT_X[index];
    int outlet = left ? (correct ? x + 5 : LEFT_RETURN + 5) : (correct ? x - 1 : RIGHT_RETURN - 1);
    int y = JUNCTION_Y[index];
    doors(level, x, y + 3, x + 4, y + 3, true);
    doors(level, outlet, y + 4, outlet, y + 6, true);
  }

  /**
   * Closes previous choices after Nox has returned to START.
   *
   * @param level shared labyrinth
   */
  static void reset(DungeonLevel level) {
    for (int i = 0; i < 6; i++) {
      int y = JUNCTION_Y[i];
      for (int x : new int[] {LEFT_X[i], RIGHT_X[i]}) doors(level, x, y + 3, x + 4, y + 3, false);
      for (int x : outlets(i)) doors(level, x, y + 4, x, y + 6, false);
    }
  }

  private static int[] outlets(int index) {
    return new int[] {LEFT_RETURN + 5, LEFT_X[index] + 5, RIGHT_X[index] - 1, RIGHT_RETURN - 1};
  }

  private static void doors(DungeonLevel level, int x0, int y0, int x1, int y1, boolean open) {
    for (int y = y0; y <= y1; y++)
      for (int x = x0; x <= x1; x++)
        level
            .tileAt(new Coordinate(x, y))
            .filter(DoorTile.class::isInstance)
            .map(DoorTile.class::cast)
            .ifPresent(
                door -> {
                  if (open) door.open();
                  else door.close();
                });
  }

  static void spawn(DungeonLevel level) {
    for (int i = 0; i < 6; i++) {
      int n = i;
      float y = junction(i).y();
      torch("gallery-" + i, new Point(i % 2 == 0 ? 18.5f : 53.5f, y + 2));
      torch("gallery-source-" + i, new Point(i % 2 == 0 ? 53.5f : 18.5f, y + (i == 5 ? 2 : 8)));
      // Workbenches repeat the archive's furniture; each source has its own tools and vessels.
      if (i == 0 || i == 3 || i == 5) bench("source-bench-" + i, new Point(32, y + 8));
      else plinth("source-base-" + i, new Point(32, y + 8), 1.6f);
      plinth("rune-base-" + i, new Point(39, y + 8), .8f);
      plinth("tablet-base-" + i, new Point(40, y + 8), .8f);
      Entity rune =
          prop(
              "rune-" + i,
              new Point(39.1f, y + 8.3f),
              "items/rpg/item_gem_amethyst.png",
              .5f,
              .5f,
              false);
      rune.add(
          new InteractionComponent(
              new Interaction(
                  (target, who) -> ProgrammingGolemRuntime.showText(who, DecisionMaze.event(n)),
                  2f)));
      rune.fetch(DrawComponent.class)
          .orElseThrow()
          .tintColor(
              new int[] {0x87B5DFFF, 0xADA1F7FF, 0xEFA26BFF, 0xDAC283FF, 0xEE9565FF, 0xFFC666FF}
                  [i]);
      prop(
          "decision-tablet-" + i,
          new Point(40.1f, y + 8.3f),
          "items/rpg/item_scroll.png",
          .55f,
          .55f,
          false);
      switch (i) {
        case 0 -> {
          prop("drained-vessel", new Point(32.1f, y + 8.4f), "objects/vase", .8f, .8f, false);
          prop("note", new Point(33, y + 8.5f), "items/rpg/item_scroll.png", .5f, .5f, false);
        }
        case 1 -> {
          for (int c = 0; c < 4; c++)
            prop(
                "source-crystal-" + c,
                new Point(32.1f + c % 2 * .7f, y + 8.3f + c / 2 * .5f),
                "items/rpg/item_gem_amethyst.png",
                .6f,
                .6f,
                false);
        }
        case 2 -> {
          prop("strength-stone", new Point(32.1f, y + 8.3f), "objects/stone", 1.1f, 1.1f, false);
          prop("pick", new Point(33.1f, y + 8.4f), "items/rpg/pickaxe_crusty.png", .6f, .6f, false);
        }
        case 3 -> {
          Entity kettle =
              prop("forge", new Point(32.1f, y + 8.4f), "objects/magic_kettle", 1, 1, false);
          kettle.name("programming-prop-forge-kettle-decisions");
          prop(
              "forge-tools",
              new Point(33.1f, y + 8.5f),
              "items/rpg/pickaxe_crusty.png",
              .6f,
              .6f,
              false);
        }
        case 4 ->
            prop(
                "vessel",
                new Point(32.2f, y + 8.2f),
                "objects/cauldron/cauldron.png",
                1.2f,
                1.2f,
                false);
        default -> {
          prop(
              "seal", new Point(32.2f, y + 8.5f), "items/rpg/item_book_brown.png", .8f, .8f, false);
          prop(
              "seal-gem",
              new Point(33.1f, y + 8.5f),
              "items/rpg/item_gem_amethyst.png",
              .5f,
              .5f,
              false);
        }
      }
    }
    plinth("heart-plinth", new Point(40, 119), 2);
    for (float x : new float[] {39.2f, 42f})
      prop(
          "heart-offering-" + x,
          new Point(x, 119.4f),
          "items/rpg/item_gem_amethyst.png",
          .8f,
          .8f,
          false);
    Entity fire = torch("heart", new Point(40, 119.5f));
    fire.fetch(PositionComponent.class).orElseThrow().scale(2);
    prop("heart-inscription", new Point(40.6f, 118), "items/rpg/item_scroll.png", .8f, .8f, false);
  }

  private static Entity torch(String name, Point at) {
    Entity torch = prop("torch-" + name, at, "objects/torch", 1, 1, false);
    torch.name("programming-prop-torch-decisions-" + name);
    torch.fetch(DrawComponent.class).orElseThrow().stateMachine().setState("on", null);
    ProgrammingProps.switchableTorch(torch);
    return torch;
  }

  private static void bench(String name, Point at) {
    Entity bench = atlas(name, at, 192, 352, 32, 16, 1, DepthLayer.Ground.depth() + 1);
    bench.add(new CollideComponent(Vector2.of(.05f, .05f), Vector2.of(1.9f, .65f)));
  }

  private static void plinth(String name, Point at, float scale) {
    atlas(name, at, 272, 144, 16, 16, scale, DepthLayer.Ground.depth());
  }

  private static Entity atlas(
      String name, Point at, int x, int y, int w, int h, float scale, int depth) {
    Entity entity = new Entity("programming-decisions-" + name);
    PositionComponent position = new PositionComponent(at);
    position.scale(scale);
    entity.add(position);
    DrawComponent draw =
        new DrawComponent(
            new SimpleIPath("spritesheets/FD_Dungeon_Free.png"),
            new SpritesheetConfig(x, y, 1, 1, w, h));
    draw.depth(depth);
    entity.add(draw);
    Game.add(entity);
    return entity;
  }

  private static Entity prop(
      String name, Point at, String asset, float w, float h, boolean ground) {
    Entity entity = new Entity("programming-decisions-" + name);
    PositionComponent position = new PositionComponent(at);
    position.scale(Vector2.of(w, h));
    entity.add(position);
    DrawComponent draw = new DrawComponent(new SimpleIPath(asset));
    draw.depth(ground ? DepthLayer.Ground.depth() : DepthLayer.Ground.depth() + 2);
    entity.add(draw);
    Game.add(entity);
    return entity;
  }
}
