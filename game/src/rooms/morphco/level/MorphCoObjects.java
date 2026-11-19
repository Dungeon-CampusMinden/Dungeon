package rooms.morphco.level;

import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.level.Tile;
import engine.level.elements.ILevel;
import engine.level.utils.LevelElement;
import engine.utils.Direction;
import engine.utils.Point;
import engine.utils.Rectangle;
import engine.utils.Vector2;
import engine.utils.components.draw.DepthLayer;
import engine.utils.components.draw.animation.AnimationConfig;
import engine.utils.components.draw.animation.SpritesheetConfig;
import engine.utils.components.draw.state.State;
import engine.utils.components.path.SimpleIPath;
import engine.utils.logging.DungeonLogger;
import feature.components.CollideComponent;
import feature.entities.deco.Deco;
import feature.timer.WorldTimerComponent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import rooms.morphco.puzzles.WordClassPuzzle;

/**
 * The objects of the room: what each kind looks like and what the room code does with it.
 *
 * <p>The level file places every object as a {@link MorphCoObjectPrefab} instance, so the objects
 * are positioned and moved in the level editor. The prefab builds the look of its kind and names
 * the entities after the instance. The room code only attaches behaviour by entity name with {@link
 * #on(String, Consumer)}; this also reaches objects the editor respawns after a move.
 */
final class MorphCoObjects {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(MorphCoObjects.class);
  private static final String ART = MorphCoProps.ART;

  /** Pixels per tile of the room's own art. */
  private static final int PIXELS_PER_TILE = 16;

  /** Height of the warning stripe at the bottom of a wall front; wall objects hang above it. */
  private static final float WALL_STRIPE = 3 / 16f;

  // belt.png: pictures of the running and of the broken belt, their height and the length of the
  // longest belt in tiles; belt_end.png: the width of an end drum.
  private static final int BELT_RUNNING = 16;
  private static final int BELT_BROKEN = 32;
  private static final int BELT_HEIGHT = 28;
  private static final int BELT_TILES = 40;
  private static final int BELT_END = 6;

  // belt_tunnel.png: width of its hood in front of the wall and how far it reaches below the belt.
  private static final int TUNNEL_HOOD = 12;
  private static final int TUNNEL_BELOW = 2;

  // Frames per picture of a belt: the arrows of a running belt move one pixel per picture, about
  // two tiles a second.
  private static final int BELT_FRAMES = 2;

  // The surface between the rails, in tiles above the belt's bottom edge.
  private static final float BELT_SURFACE_BOTTOM = 0.3f;
  private static final float BELT_SURFACE_TOP = 1.4f;

  /**
   * Builds the entities of an object kind. The entities are not added to the game yet.
   *
   * <p>{@code number} is a per-instance setting: the crate index, the length of a belt, or the size
   * of a click area in tenths of a tile.
   */
  @FunctionalInterface
  interface Look {
    List<Entity> build(String name, Point position, int number);
  }

  private static final Map<String, Look> LOOKS = new LinkedHashMap<>();
  private static final Map<String, Consumer<Entity>> BEHAVIOURS = new HashMap<>();
  private static final Set<String> ON_WALL = new HashSet<>();

  static {
    // Furniture in the room's own top-down art.
    texture("server", "server.png", 15 / 16f, new Rectangle(0.9f, 0.6f, 0.02f, 0f));
    texture("lockers", "lockers.png", 31 / 16f, new Rectangle(1.9f, 0.7f, 0.05f, 0f));
    texture("chair", "chair.png", 14 / 16f, null);
    texture("table", "table.png", 24 / 16f, new Rectangle(2.4f, 0.5f, 0.05f, 0f));
    texture("cabinet", "cabinet.png", 20 / 16f, new Rectangle(1.2f, 0.6f, 0.03f, 0f));
    texture("tank", "tank.png", 18 / 16f, new Rectangle(1f, 0.6f, 0.06f, 0f));
    texture("cart", "cart.png", 18 / 16f, new Rectangle(1f, 0.5f, 0.06f, 0f));
    LOOKS.put("meeting-table", MorphCoObjects::meetingTable);

    // Office furniture from the shared decorations.
    deco("bookshelf", Deco.BookShelf);
    deco("bookshelf-large", Deco.BookshelfLarge);
    deco("plant", Deco.Plant);
    deco("potted-tree", Deco.PottedTree);
    deco("trash-can", Deco.TrashCanRed);
    deco("boxes", Deco.SoulweaverSmallCrate);
    deco("sink", Deco.Sink);
    deco("toilet", Deco.TrashCanBlue);
    deco("mirror", Deco.LabWindowSmall);
    deco("cup", Deco.Cup);
    texture("tall-plant", "potted_plant.png", 14 / 16f, new Rectangle(0.45f, 0.25f, 0.2f, 0f));

    // Small framed pictures on the wall; interacting shows the large picture.
    for (String poster :
        List.of("form", "warning", "noeat", "luggage", "summer", "safety", "clock", "marmot")) {
      onWall("picture-" + poster, "wall_" + poster + ".png", 9 / 16f);
    }

    // Door and hall signs, drawn with 32 pixels per tile so that their lettering stays legible.
    // The door and hall signs are equally high; every sign with lettering uses the same font.
    onWall("sign-f04", "sign_f04.png", 13 / 16f);
    onWall("sign-a12", "sign_a12.png", 13 / 16f);
    onWall("sign-wc", "sign_wc.png", 13 / 16f);
    onWall("sign-control", "sign_control.png", 13 / 16f);
    onWall("sign-kitchen", "sign_kitchen.png", 13 / 16f);
    onWall("sign-storage", "sign_storage.png", 13 / 16f);
    onWall("sign-production", "sign_production.png", 13 / 16f);
    onWall("sign-restricted", "sign_restricted.png", 15 / 16f);
    onWall("sign-motto", "sign_motto.png", 11 / 16f);
    onWall("cctv", "sign_cctv.png", 15 / 16f);

    // Corridor.
    LOOKS.put("shift-clock", MorphCoObjects::shiftClock);
    // The side quest at the snack machine: the machine, the locker key and the banknote.
    deco("snack-machine", Deco.SnackDispenser);
    plain("locker-key", "items/key/small_key.png", 0.6f, null);
    texture("banknote", "banknote.png", 0.6f, null);
    // Kitchen.
    deco("coffee-maker", Deco.CoffeeMaker);

    // Entrance and control room.
    plain("card-slot", "objects/card_reader/closed.png", 12 / 16f, null);
    plain("alarm-light", "objects/tech/Alert_1.png", 0.45f, null);
    LOOKS.put("desk-pc", MorphCoObjects::deskPc);
    // The red lamp of the unwired fuse box blinks; it turns green once the cables are in. Each of
    // the two frames is 32x44.
    blinking(
        "fuse-box",
        "fuse_box.png",
        32,
        44,
        "fuse_box_ok.png",
        2f,
        new Rectangle(1.8f, 0.8f, 0.1f, 0f));
    switchable(
        "console",
        "console_alarm.png",
        "console_off.png",
        1.62f,
        new Rectangle(2f, 0.8f, 0.05f, 0f));

    // F-04. Like A-12, the machine bobs in a loop of eight 46x59 pictures; its lamp turns green
    // after the repair.
    machine(
        "flexion-machine",
        "flexion_machine.png",
        "flexion_machine_ok.png",
        46,
        59,
        new Rectangle(2.4f, 2f, 0.2f, 0.4f));
    // A display on a stand, 32 pixels per tile like the signs; each of its two frames is 80x70.
    blinking(
        "display-f04",
        "display_f04_error.png",
        80,
        70,
        "display_f04_ok.png",
        70 / 32f,
        new Rectangle(0.9f, 0.3f, 0.8f, 0f));
    LOOKS.put("crate", MorphCoObjects::crate);
    LOOKS.put("belt", (name, position, number) -> belt(name, position, number, false, true, true));
    LOOKS.put(
        "belt-left", (name, position, number) -> belt(name, position, number, true, true, true));
    // Without a drum the belt runs on into the next belt.
    LOOKS.put(
        "belt-left-no-drum-left",
        (name, position, number) -> belt(name, position, number, true, false, true));
    LOOKS.put(
        "belt-left-no-drum-right",
        (name, position, number) -> belt(name, position, number, true, true, false));
    LOOKS.put("belt-left-tunnel-right", MorphCoObjects::beltWithTunnel);
    // A word card lies on the belt: above it and below the players, like the belt tunnel.
    LOOKS.put(
        "word-sheet",
        (name, position, number) -> {
          Entity sheet =
              MorphCoProps.buildTexture(name, position, "items/rpg/item_paper.png", 0.7f, null);
          sheet.fetch(DrawComponent.class).orElseThrow().depth(DepthLayer.Ground.depth());
          return List.of(sheet);
        });

    // A-12.
    // The sorter bobs in a loop of eight 48x52 pictures. The red light of the broken sorter blinks;
    // it turns green after the repair.
    machine(
        "affix-sorter",
        "affix_sorter.png",
        "affix_sorter_ok.png",
        48,
        52,
        new Rectangle(2.8f, 1.2f, 0.1f, 0f));
    switchable("display-a12", "display_a12_error.png", "display_a12_ok.png", 1f, null);
    LOOKS.put("keypad", MorphCoObjects::keypad);
    LOOKS.put(
        "click-area",
        (name, position, number) ->
            List.of(
                MorphCoProps.buildTexture(
                    name, position, ART + "hotspot.png", number / 10f, null)));
    texture("pile", "pile.png", 19 / 16f, new Rectangle(2.3f, 0.9f, 0.1f, 0f));
    texture("station", "station.png", 32 / 16f, new Rectangle(2.9f, 1.2f, 0.05f, 0f));

    LOOKS.put("shift-lead", MorphCoObjects::shiftLead);
  }

  private MorphCoObjects() {}

  /**
   * Returns the object kinds, in the order the editor lists them.
   *
   * @return the kinds
   */
  static List<String> kinds() {
    return List.copyOf(LOOKS.keySet());
  }

  /**
   * Builds the entities of an object and attaches the behaviour the room registered for them.
   *
   * <p>Signs and pictures hang on the wall: their bottom edge rests on the warning stripe of the
   * wall tile at their position or right below it, so they all hang at the same height however
   * exactly they were placed. Only their x position is free.
   *
   * @param level level the object is placed in
   * @param kind object kind
   * @param name instance name, used as entity name
   * @param position bottom-left corner
   * @param number per-instance setting of the kind
   * @return the entities, not added to the game yet
   */
  static List<Entity> build(ILevel level, String kind, String name, Point position, int number) {
    Look look = LOOKS.get(kind);
    if (look == null) return List.of();
    Point placed = ON_WALL.contains(kind) ? hang(level, position) : position;
    List<Entity> entities = look.build(name, placed, number);
    for (Entity entity : entities) {
      Consumer<Entity> behaviour = BEHAVIOURS.get(entity.name());
      if (behaviour != null) behaviour.accept(entity);
    }
    return entities;
  }

  /**
   * Attaches behaviour to the objects with an entity name: to the ones already in the level and to
   * every later respawn, e.g. after the object was moved in the level editor.
   *
   * @param name entity name (the instance name of the object)
   * @param behaviour adds interactions and sets the state of an object
   */
  static void on(String name, Consumer<Entity> behaviour) {
    BEHAVIOURS.put(name, behaviour);
    Game.levelEntities().filter(entity -> entity.name().equals(name)).toList().forEach(behaviour);
  }

  /** Warns about behaviour without an object in the level, e.g. after renaming an instance. */
  static void warnMissing() {
    Set<String> names = Game.levelEntities().map(Entity::name).collect(Collectors.toSet());
    BEHAVIOURS.keySet().stream()
        .filter(name -> !names.contains(name))
        .sorted()
        .forEach(name -> LOGGER.warn("No object named '{}' in the level", name));
  }

  /** Forgets the behaviour of a previous level. */
  static void reset() {
    BEHAVIOURS.clear();
  }

  private static void texture(String kind, String file, float size, Rectangle collider) {
    plain(kind, ART + file, size, collider);
  }

  private static void onWall(String kind, String file, float size) {
    texture(kind, file, size, null);
    ON_WALL.add(kind);
  }

  // The position on the wall tile at the given position or right below it; away from a wall the
  // position stays as it is.
  private static Point hang(ILevel level, Point position) {
    int row = (int) position.y();
    for (int y = row; y >= row - 1; y--) {
      boolean wall =
          level
              .tileAt(new Point(position.x(), y))
              .map(Tile::levelElement)
              .filter(element -> element == LevelElement.WALL)
              .isPresent();
      if (wall) return new Point(position.x(), y + WALL_STRIPE);
    }
    return position;
  }

  private static void plain(String kind, String path, float size, Rectangle collider) {
    LOOKS.put(
        kind,
        (name, position, number) ->
            List.of(MorphCoProps.buildTexture(name, position, path, size, collider)));
  }

  private static void switchable(
      String kind, String off, String on, float size, Rectangle collider) {
    LOOKS.put(
        kind,
        (name, position, number) ->
            List.of(
                MorphCoProps.buildSwitchable(name, position, ART + off, ART + on, size, collider)));
  }

  private static void blinking(
      String kind,
      String off,
      int frameWidth,
      int frameHeight,
      String on,
      float size,
      Rectangle collider) {
    LOOKS.put(
        kind,
        (name, position, number) ->
            List.of(
                MorphCoProps.buildBlinking(
                    name, position, ART + off, frameWidth, frameHeight, ART + on, size, collider)));
  }

  // A machine that bobs in a loop cut from {@code broken}, and from {@code repaired} after the
  // repair, see MorphCoProps.buildMachine.
  private static void machine(
      String kind,
      String broken,
      String repaired,
      int frameWidth,
      int frameHeight,
      Rectangle collider) {
    LOOKS.put(
        kind,
        (name, position, number) ->
            List.of(
                MorphCoProps.buildMachine(
                    name,
                    position,
                    ART + broken,
                    ART + repaired,
                    frameWidth,
                    frameHeight,
                    (float) frameWidth / PIXELS_PER_TILE,
                    collider)));
  }

  private static void deco(String kind, Deco deco) {
    LOOKS.put(
        kind, (name, position, number) -> List.of(MorphCoProps.buildDeco(name, position, deco)));
  }

  // The labelled crate of a word class; {@code number} picks the class.
  private static List<Entity> crate(String name, Point position, int number) {
    WordClassPuzzle.Crate[] classes = WordClassPuzzle.Crate.values();
    String file =
        "crate_" + classes[Math.floorMod(number, classes.length)].name().toLowerCase(Locale.ROOT);
    return List.of(
        MorphCoProps.buildTexture(
            name, position, ART + file + ".png", 1.375f, new Rectangle(1.4f, 0.7f, 0.05f, 0f)));
  }

  // A conveyor belt {@code number} tiles long, with an end drum on the left and on the right side
  // as given. belt.png holds the belt in 16 pixels per tile, long enough for 40 tiles: 16 pictures
  // of the running belt, then 32 of the broken belt, which stands still and jerks forward, stacked
  // one below the other. The arrows of a mirrored belt point to the left. The arrows repeat every
  // tile from the end the belt comes from; a belt that runs on into the next one at its other end
  // shifts its picture so that the arrows continue there. The belt lies under the word cards on it,
  // like the other walkable objects; its controller switches it on after the repair of the hall,
  // and Conveyors carries the players along its surface.
  private static List<Entity> belt(
      String name,
      Point position,
      int number,
      boolean mirrored,
      boolean drumLeft,
      boolean drumRight) {
    int drums = (drumLeft ? 1 : 0) + (drumRight ? 1 : 0);
    int width = Math.clamp(number, 1, BELT_TILES) * PIXELS_PER_TILE - drums * BELT_END;
    boolean openAhead = mirrored ? !drumLeft : !drumRight;
    int shift = openAhead ? Math.floorMod(-width, PIXELS_PER_TILE) : 0;
    float scale = (float) Math.min(width, BELT_HEIGHT) / PIXELS_PER_TILE;
    State broken =
        new State(
            MorphCoProps.OFF,
            new SimpleIPath(ART + "belt.png"),
            new AnimationConfig(
                    new SpritesheetConfig(
                        shift, BELT_RUNNING * BELT_HEIGHT, BELT_BROKEN, 1, width, BELT_HEIGHT))
                .scaleX(scale)
                .framesPerSprite(BELT_FRAMES)
                .mirrored(mirrored));
    State running =
        new State(
            MorphCoProps.ON,
            new SimpleIPath(ART + "belt.png"),
            new AnimationConfig(
                    new SpritesheetConfig(shift, 0, BELT_RUNNING, 1, width, BELT_HEIGHT))
                .scaleX(scale)
                .framesPerSprite(BELT_FRAMES)
                .mirrored(mirrored));
    float end = (float) BELT_END / PIXELS_PER_TILE;
    Point start = position.translate(drumLeft ? end : 0, 0);
    Entity belt = MorphCoProps.switchable(name, start, broken, running, null);
    Rectangle surface =
        new Rectangle(
            (float) width / PIXELS_PER_TILE,
            BELT_SURFACE_TOP - BELT_SURFACE_BOTTOM,
            start.x(),
            start.y() + BELT_SURFACE_BOTTOM);
    belt.add(new Conveyors.Belt(mirrored ? Direction.LEFT : Direction.RIGHT, surface));
    List<Entity> entities = new ArrayList<>();
    if (drumLeft) entities.add(beltEnd(name, position, true));
    entities.add(belt);
    if (drumRight)
      entities.add(beltEnd(name, start.translate((float) width / PIXELS_PER_TILE, 0), false));
    return entities;
  }

  // The drum at an end of a belt; belt_end.png shows the right end.
  private static Entity beltEnd(String name, Point position, boolean left) {
    Entity end =
        MorphCoProps.buildTexture(
            name + "-end",
            position,
            ART + "belt_end.png",
            (float) BELT_END / PIXELS_PER_TILE,
            null);
    end.fetch(DrawComponent.class).orElseThrow().currentAnimation().mirrored(left);
    return end;
  }

  // A belt with arrows to the left that comes out of the wall on its right: there the opening
  // belt_tunnel.png with a curtain of rubber strips, like at a baggage claim, replaces the drum.
  // Its hood covers the last pixels of the belt, its right edge the edge of the wall; it lies above
  // the belt and below the players.
  private static List<Entity> beltWithTunnel(String name, Point position, int number) {
    int tiles = Math.clamp(number, 1, BELT_TILES);
    List<Entity> entities = belt(name, position, tiles, true, true, false);
    Point opening =
        position.translate(
            tiles - (float) TUNNEL_HOOD / PIXELS_PER_TILE, -(float) TUNNEL_BELOW / PIXELS_PER_TILE);
    Entity tunnel =
        MorphCoProps.buildTexture(name + "-tunnel", opening, ART + "belt_tunnel.png", 1f, null);
    tunnel.fetch(DrawComponent.class).orElseThrow().depth(DepthLayer.Ground.depth());
    entities.add(tunnel);
    return entities;
  }

  // The shift clock on the wall: an invisible spot on which the clients draw the active play time
  // as a red LED display.
  private static List<Entity> shiftClock(String name, Point position, int number) {
    Entity clock = MorphCoProps.buildTexture(name, position, ART + "hotspot.png", 0.5f, null);
    clock.add(WorldTimerComponent.stopwatch(0));
    return List.of(clock);
  }

  // A keypad that hangs in front of a machine, like the lock of the maintenance hatch of A-12.
  private static List<Entity> keypad(String name, Point position, int number) {
    return List.of(
        MorphCoProps.onTable(
            MorphCoProps.buildSwitchable(
                name, position, "objects/keypad/off.png", "objects/keypad/on.png", 0.45f, null),
            position));
  }

  // The control room table with the phone and the coffee cup on it.
  private static List<Entity> meetingTable(String name, Point position, int number) {
    Entity table =
        MorphCoProps.buildTexture(
            name, position, ART + "table.png", 24 / 16f, new Rectangle(2.4f, 0.5f, 0.05f, 0f));
    Entity phone =
        MorphCoProps.onTable(
            MorphCoProps.buildTexture(
                "phone", position.translate(0.35f, 0.6f), ART + "phone.png", 9 / 16f, null),
            position);
    Entity cup =
        MorphCoProps.onTable(
            MorphCoProps.buildDeco("coffee", position.translate(1.8f, 0.6f), Deco.Cup), position);
    return List.of(table, phone, cup);
  }

  // The desk with the computer from The Last Hour, at its size there. The computer runs on the
  // backup battery, so its screen is on.
  private static List<Entity> deskPc(String name, Point position, int number) {
    Entity pc = new Entity(name);
    pc.add(new PositionComponent(position));
    DrawComponent draw = new DrawComponent(new SimpleIPath("objects/desk_with_pc"), "on");
    draw.depth(DepthLayer.Player.depth());
    pc.add(draw);
    pc.add(new CollideComponent(new Rectangle(2.0f, 1.3f, 0.8f, 1f)));
    return List.of(pc);
  }

  // The shift lead; the lab-coat sprite only has undirected idle and run animations.
  private static List<Entity> shiftLead(String name, Point position, int number) {
    Entity npc = new Entity(name);
    npc.add(new PositionComponent(position));
    DrawComponent draw = new DrawComponent(new SimpleIPath("character/shift_lead"));
    draw.depth(DepthLayer.Player.depth());
    npc.add(draw);
    CollideComponent collider = new CollideComponent(Vector2.of(0.1f, 0f), Vector2.of(0.8f, 0.6f));
    collider.isSolid(true);
    npc.add(collider);
    return List.of(npc);
  }
}
