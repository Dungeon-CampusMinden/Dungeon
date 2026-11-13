package rooms.morphco.level;

import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.utils.Point;
import engine.utils.Rectangle;
import engine.utils.Vector2;
import engine.utils.components.draw.DepthLayer;
import engine.utils.components.path.SimpleIPath;
import feature.components.CollideComponent;
import feature.entities.deco.Deco;
import feature.timer.WorldTimerComponent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import rooms.morphco.puzzles.WordClassPuzzle;
import rooms.systemRecovery.entities.TransportEntityFactory;

/**
 * The objects of the room: what each kind looks like and what the room code does with it.
 *
 * <p>The level file places every object as a {@link MorphCoObjectPrefab} instance, so the objects
 * are positioned and moved in the level editor. The prefab builds the look of its kind and names
 * the entities after the instance. The room code only attaches behaviour by entity name with {@link
 * #on(String, Consumer)}; this also reaches objects the editor respawns after a move.
 */
final class MorphCoObjects {

  private static final String ART = MorphCoProps.ART;

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
    deco("mirror", Deco.LabWindowSmall);
    deco("fuse-box", Deco.LabElectricPanel);
    deco("cup", Deco.Cup);
    plain("tall-plant", "office/plant.png", 1.2f, new Rectangle(0.9f, 0.5f, 0.15f, 0f));

    // Small framed pictures on the wall; interacting shows the large picture.
    for (String poster :
        List.of("form", "warning", "noeat", "luggage", "summer", "safety", "clock", "marmot")) {
      texture("picture-" + poster, "wall_" + poster + ".png", 9 / 16f, null);
    }

    // Door and hall signs.
    texture("sign-f04", "sign_f04.png", 15 / 16f, null);
    texture("sign-a12", "sign_a12.png", 15 / 16f, null);
    texture("sign-wc", "sign_wc.png", 15 / 16f, null);
    texture("sign-control", "sign_control.png", 13 / 16f, null);
    texture("sign-kitchen", "sign_kitchen.png", 13 / 16f, null);
    texture("sign-storage", "sign_storage.png", 13 / 16f, null);
    texture("sign-production", "sign_production.png", 13 / 16f, null);
    texture("sign-restricted", "sign_restricted.png", 15 / 16f, null);
    texture("sign-motto", "sign_motto.png", 11 / 16f, null);
    texture("cctv", "sign_cctv.png", 15 / 16f, null);

    // Corridor.
    LOOKS.put("shift-clock", MorphCoObjects::shiftClock);

    // Entrance and control room.
    texture("card-slot", "card_reader.png", 12 / 16f, null);
    plain("alarm-light", "objects/tech/Alert_1.png", 0.45f, null);
    plain("desk-pc", "office/desk-with-pc.png", 2f, new Rectangle(1.9f, 0.9f, 0.05f, 0f));
    switchable(
        "console",
        "console_alarm.png",
        "console_off.png",
        1.62f,
        new Rectangle(2f, 0.8f, 0.05f, 0f));

    // F-04.
    texture(
        "flexion-machine", "flexion_machine.png", 44 / 16f, new Rectangle(2.4f, 2f, 0.2f, 0.4f));
    switchable("display-f04", "display_f04_error.png", "display_f04_ok.png", 1f, null);
    LOOKS.put("crate", MorphCoObjects::crate);
    LOOKS.put("belt", MorphCoObjects::belt);
    plain("word-sheet", "items/rpg/item_paper.png", 0.7f, null);

    // A-12.
    texture("affix-sorter", "affix_sorter.png", 3f, new Rectangle(2.8f, 1.2f, 0.1f, 0f));
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
   * @param kind object kind
   * @param name instance name, used as entity name
   * @param position bottom-left corner
   * @param number per-instance setting of the kind
   * @return the entities, not added to the game yet
   */
  static List<Entity> build(String kind, String name, Point position, int number) {
    Look look = LOOKS.get(kind);
    if (look == null) return List.of();
    List<Entity> entities = look.build(name, position, number);
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

  /** Forgets the behaviour of a previous level. */
  static void reset() {
    BEHAVIOURS.clear();
  }

  private static void texture(String kind, String file, float size, Rectangle collider) {
    plain(kind, ART + file, size, collider);
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

  // A conveyor belt of {@code number} System Recovery segments; every segment carries the
  // instance name. The belt lies under the word cards on it, like the other walkable objects.
  private static List<Entity> belt(String name, Point position, int number) {
    List<Entity> segments = new ArrayList<>();
    for (int index = 0; index < Math.max(1, number); index++) {
      Entity segment = TransportEntityFactory.conveyorSegment(position.translate(index, 0));
      segment.name(name);
      segment
          .fetch(DrawComponent.class)
          .ifPresent(draw -> draw.depth(DepthLayer.BackgroundDeco.depth()));
      segments.add(segment);
    }
    return segments;
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
