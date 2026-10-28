package wizard.runner.room;

import engine.utils.Rectangle;
import escaperoom.foundation.room.model.RoomDecoration;
import escaperoom.foundation.room.model.RoomPoint;
import feature.entities.deco.Deco;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Hand-built level piece read from a bundled {@code .level} file.
 *
 * <p>The file name prefix selects the piece kind: {@code room_}, {@code corridor_}, or {@code
 * exit_}. Named points are slots: {@code keypad_N}, {@code terminal_N}, {@code container_N}, {@code
 * chest_N}, {@code item_N}, and {@code hint_N} hold riddle stations; {@code door_<side>_N} marks an
 * outer-wall tile where the generator may connect a neighbouring piece. Only the layout, the named
 * points, and the decorations of the JSON level format are read.
 *
 * <p>A station slot lies on floor, on furniture, or on a wall. Loading rejects a template unless
 * every slot, every door entrance, and the start stay connected over free floor while every slot is
 * occupied and decoration colliders block their tiles.
 *
 * @param name file name without extension
 * @param kind piece kind
 * @param rows top-to-bottom tiles using {@code ' '}, {@code '.'}, {@code '#'}, and {@code 'E'}
 * @param slots station slots by kind in slot-number order
 * @param doors door slots in slot order
 * @param decorations decorations in file order
 * @param start free floor tile nearest to the piece centre
 */
record RoomTemplate(
    String name,
    Kind kind,
    List<String> rows,
    Map<SlotKind, List<RoomPoint>> slots,
    List<DoorSlot> doors,
    List<RoomDecoration> decorations,
    RoomPoint start) {
  private static final Pattern SLOT =
      Pattern.compile("(keypad|terminal|container|chest|item|hint)_(\\d+)");
  private static final Pattern DOOR = Pattern.compile("door_(north|east|south|west)_(\\d+)");
  private static final float COLLIDER_EPSILON = 0.05f;
  private static final ObjectMapper MAPPER = JsonMapper.builder().build();

  /** Kind of level piece. */
  enum Kind {
    /** Room that holds the stations of one riddle. */
    ROOM,
    /** Passage between two rooms. */
    CORRIDOR,
    /** Alcove behind the exit door that holds the exit tile. */
    EXIT
  }

  /** Station slot kinds; assignment chests and items use separate slots. */
  enum SlotKind {
    KEYPAD,
    TERMINAL,
    CONTAINER,
    CHEST,
    ITEM,
    HINT
  }

  /** Outer wall side of a door slot. */
  enum Side {
    NORTH(0, -1),
    EAST(1, 0),
    SOUTH(0, 1),
    WEST(-1, 0);

    final int dx;
    final int dy;

    Side(final int dx, final int dy) {
      this.dx = dx;
      this.dy = dy;
    }

    Side opposite() {
      return values()[(ordinal() + 2) % 4];
    }
  }

  /**
   * Outer-wall tile where a neighbouring piece can connect.
   *
   * @param point wall tile
   * @param side wall side the slot faces
   */
  record DoorSlot(RoomPoint point, Side side) {}

  /**
   * Returns the number of slots of one kind.
   *
   * @param slotKind slot kind
   * @return slot count
   */
  int capacity(final SlotKind slotKind) {
    return slots.getOrDefault(slotKind, List.of()).size();
  }

  /**
   * Reads and checks one bundled template.
   *
   * @param resource classpath resource path
   * @return checked template
   */
  static RoomTemplate load(final String resource) {
    String name = resource.substring(resource.lastIndexOf('/') + 1).replace(".level", "");
    try (InputStream input = RoomTemplate.class.getClassLoader().getResourceAsStream(resource)) {
      if (input == null) {
        throw new IllegalStateException("missing room template " + resource);
      }
      return fromLevel(name, MAPPER.readTree(input));
    } catch (IOException exception) {
      throw new UncheckedIOException(exception);
    } catch (RuntimeException exception) {
      throw new IllegalStateException(
          "invalid room template " + name + ": " + exception.getMessage(), exception);
    }
  }

  private static RoomTemplate fromLevel(final String name, final JsonNode level) {
    Kind kind =
        switch (name.substring(0, Math.max(0, name.indexOf('_')))) {
          case "room" -> Kind.ROOM;
          case "corridor" -> Kind.CORRIDOR;
          case "exit" -> Kind.EXIT;
          default -> throw new IllegalArgumentException("unknown template kind");
        };
    if (level.required("version").asInt() < 3) {
      throw new IllegalArgumentException("templates use the JSON level format");
    }
    List<String> rows = new ArrayList<>();
    for (JsonNode row : level.required("layout")) {
      StringBuilder tiles = new StringBuilder();
      for (char tile : row.asString().toCharArray()) {
        tiles.append(
            switch (tile) {
              case 'S' -> ' ';
              case 'F' -> '.';
              case 'W' -> '#';
              case 'E' -> 'E';
              default -> throw new IllegalArgumentException("unsupported tile " + tile);
            });
      }
      rows.add(tiles.toString());
    }
    if (rows.isEmpty() || rows.stream().anyMatch(row -> row.length() != rows.getFirst().length())) {
      throw new IllegalArgumentException("layout rows must be non-empty and uniform");
    }
    int height = rows.size();

    Map<SlotKind, TreeMap<Integer, RoomPoint>> numbered = new EnumMap<>(SlotKind.class);
    TreeMap<String, DoorSlot> doors = new TreeMap<>();
    level
        .required("namedPoints")
        .properties()
        .forEach(
            entry -> {
              String pointName = entry.getKey();
              RoomPoint tile = tile(height, entry.getValue());
              Matcher slot = SLOT.matcher(pointName);
              Matcher door = DOOR.matcher(pointName);
              if (slot.matches()) {
                SlotKind slotKind = SlotKind.valueOf(slot.group(1).toUpperCase(Locale.ROOT));
                numbered
                    .computeIfAbsent(slotKind, ignored -> new TreeMap<>())
                    .put(Integer.parseInt(slot.group(2)), tile);
              } else if (door.matches()) {
                doors.put(
                    pointName,
                    new DoorSlot(tile, Side.valueOf(door.group(1).toUpperCase(Locale.ROOT))));
              } else {
                throw new IllegalArgumentException("unknown named point " + pointName);
              }
            });
    Map<SlotKind, List<RoomPoint>> slots = new EnumMap<>(SlotKind.class);
    numbered.forEach((slotKind, points) -> slots.put(slotKind, List.copyOf(points.values())));

    List<RoomDecoration> decorations = new ArrayList<>();
    Set<RoomPoint> blocked = new HashSet<>();
    for (JsonNode decoration : level.required("decorations")) {
      Deco deco = Deco.valueOf(decoration.required("type").asString());
      JsonNode position = decoration.required("position");
      float x = position.required("x").floatValue();
      float y = position.required("y").floatValue();
      decorations.add(
          new RoomDecoration(
              deco, tile(height, position), x - (float) Math.floor(x), y - (float) Math.floor(y)));
      blocked.addAll(colliderTiles(height, rows.getFirst().length(), deco, x, y));
    }

    return check(
        name, kind, rows, Map.copyOf(slots), List.copyOf(doors.values()), decorations, blocked);
  }

  private static RoomTemplate check(
      final String name,
      final Kind kind,
      final List<String> rows,
      final Map<SlotKind, List<RoomPoint>> slots,
      final List<DoorSlot> doors,
      final List<RoomDecoration> decorations,
      final Set<RoomPoint> blocked) {
    int width = rows.getFirst().length();
    for (int y = 0; y < rows.size(); y++) {
      for (int x = 0; x < width; x++) {
        char tile = rows.get(y).charAt(x);
        if ((tile == '.' || tile == 'E') && !enclosed(rows, x, y)) {
          throw new IllegalArgumentException(
              "floor at " + x + "," + y + " is not enclosed by walls");
        }
      }
    }
    List<RoomPoint> stations = slots.values().stream().flatMap(List::stream).toList();
    if (new HashSet<>(stations).size() != stations.size()) {
      throw new IllegalArgumentException("slots must use distinct tiles");
    }
    Set<RoomPoint> free = new HashSet<>();
    for (int y = 0; y < rows.size(); y++) {
      for (int x = 0; x < width; x++) {
        RoomPoint point = new RoomPoint(x, y);
        if (at(rows, point) == '.' && !blocked.contains(point) && !stations.contains(point)) {
          free.add(point);
        }
      }
    }
    for (RoomPoint station : stations) {
      char tile = at(rows, station);
      if (tile != '.' && tile != '#') {
        throw new IllegalArgumentException("slot " + station + " must use floor or a wall");
      }
    }
    List<RoomPoint> entries = new ArrayList<>();
    for (DoorSlot door : doors) {
      RoomPoint point = door.point();
      char inside = at(rows, offset(point, door.side().opposite()));
      char outside = at(rows, offset(point, door.side()));
      if (at(rows, point) != '#' || (inside != '.' && inside != 'E') || outside != ' ') {
        throw new IllegalArgumentException("door slot " + point + " must lie on an outer wall");
      }
      RoomPoint entry = offset(point, door.side().opposite());
      if (inside == '.' && !free.contains(entry) || stations.contains(point)) {
        throw new IllegalArgumentException("door slot " + point + " must open onto free floor");
      }
      entries.add(entry);
    }

    long exits =
        rows.stream().flatMap(row -> row.chars().boxed()).filter(tile -> tile == 'E').count();
    boolean stationsAllowed = kind == Kind.ROOM;
    int minimumDoors = kind == Kind.CORRIDOR ? 2 : 1;
    if (doors.size() < minimumDoors
        || (!stationsAllowed && !stations.isEmpty())
        || (kind == Kind.ROOM && slots.getOrDefault(SlotKind.HINT, List.of()).isEmpty())
        || exits != (kind == Kind.EXIT ? 1 : 0)) {
      throw new IllegalArgumentException("slots, doors, or exits do not match the template kind");
    }

    RoomPoint start =
        free.stream()
            .min(
                Comparator.comparingDouble(
                        (RoomPoint point) ->
                            Math.hypot(
                                point.x() - (width - 1) / 2.0, point.y() - (rows.size() - 1) / 2.0))
                    .thenComparingInt(RoomPoint::y)
                    .thenComparingInt(RoomPoint::x))
            .orElse(entries.getFirst());
    Set<RoomPoint> walkable = new HashSet<>(free);
    walkable.addAll(entries);
    Set<RoomPoint> reach = reachable(walkable, entries.getFirst());
    List<RoomPoint> required = new ArrayList<>(entries);
    required.add(start);
    for (RoomPoint point : required) {
      if (!reach.contains(point)) {
        throw new IllegalArgumentException(
            "tile " + point + " is cut off when every slot is occupied");
      }
    }
    for (RoomPoint station : stations) {
      if (Arrays.stream(Side.values()).noneMatch(side -> reach.contains(offset(station, side)))) {
        throw new IllegalArgumentException(
            "slot " + station + " is cut off when every slot is occupied");
      }
    }
    return new RoomTemplate(
        name, kind, List.copyOf(rows), slots, doors, List.copyOf(decorations), start);
  }

  private static Set<RoomPoint> reachable(final Set<RoomPoint> walkable, final RoomPoint start) {
    Set<RoomPoint> visited = new HashSet<>();
    ArrayDeque<RoomPoint> pending = new ArrayDeque<>(List.of(start));
    while (!pending.isEmpty()) {
      RoomPoint current = pending.removeFirst();
      if (walkable.contains(current) && visited.add(current)) {
        for (Side side : Side.values()) {
          RoomPoint next = offset(current, side);
          if (next != null) {
            pending.addLast(next);
          }
        }
      }
    }
    return visited;
  }

  private static boolean enclosed(final List<String> rows, final int x, final int y) {
    for (int dy = -1; dy <= 1; dy++) {
      for (int dx = -1; dx <= 1; dx++) {
        int nx = x + dx;
        int ny = y + dy;
        if (ny < 0 || ny >= rows.size() || nx < 0 || nx >= rows.get(ny).length()) {
          return false;
        }
        if (rows.get(ny).charAt(nx) == ' ') {
          return false;
        }
      }
    }
    return true;
  }

  private static char at(final List<String> rows, final RoomPoint point) {
    if (point == null || point.y() >= rows.size() || point.x() >= rows.get(point.y()).length()) {
      return ' ';
    }
    return rows.get(point.y()).charAt(point.x());
  }

  private static RoomPoint offset(final RoomPoint point, final Side side) {
    int x = point.x() + side.dx;
    int y = point.y() + side.dy;
    return x < 0 || y < 0 ? null : new RoomPoint(x, y);
  }

  /** Converts an engine point with an upward y axis into its top-to-bottom tile. */
  private static RoomPoint tile(final int height, final JsonNode point) {
    return new RoomPoint(
        (int) Math.floor(point.required("x").floatValue()),
        height - 1 - (int) Math.floor(point.required("y").floatValue()));
  }

  /** Returns the tiles that a decoration's default collider at an engine position blocks. */
  private static Set<RoomPoint> colliderTiles(
      final int height, final int width, final Deco deco, final float decoX, final float decoY) {
    Rectangle collider = deco.defaultCollider();
    Set<RoomPoint> tiles = new HashSet<>();
    if (collider == null) {
      return tiles;
    }
    float left = decoX + collider.x() + COLLIDER_EPSILON;
    float right = decoX + collider.x() + collider.width() - COLLIDER_EPSILON;
    float bottom = decoY + collider.y() + COLLIDER_EPSILON;
    float top = decoY + collider.y() + collider.height() - COLLIDER_EPSILON;
    for (int x = (int) Math.floor(left); x <= (int) Math.floor(right); x++) {
      for (int y = (int) Math.floor(bottom); y <= (int) Math.floor(top); y++) {
        int topY = height - 1 - y;
        if (x >= 0 && x < width && topY >= 0 && topY < height) {
          tiles.add(new RoomPoint(x, topY));
        }
      }
    }
    return tiles;
  }
}
