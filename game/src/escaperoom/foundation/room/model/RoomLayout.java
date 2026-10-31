package escaperoom.foundation.room.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Immutable engine-neutral level layout with rows ordered from top to bottom.
 *
 * <p>Rows use {@code ' '} for empty space outside the rooms, {@code #} for walls, {@code .} for
 * ordinary floor, {@code D} for doors, and {@code E} for the common exit. A station may stand on a
 * floor tile or hang on a wall tile next to reachable floor.
 *
 * @param rows uniform top-to-bottom tile rows
 * @param startPoint shared deterministic start for every player
 * @param riddlePlacements canonical authored riddle order with explicit hint points
 * @param riddleDoors doors that open once their riddle leaves the locked state
 * @param doorPoint common exit door coordinate
 * @param exitPoint common exit coordinate
 * @param decorations static decorations
 */
public record RoomLayout(
    List<String> rows,
    RoomPoint startPoint,
    List<RiddlePlacement> riddlePlacements,
    List<RiddleDoor> riddleDoors,
    RoomPoint doorPoint,
    RoomPoint exitPoint,
    List<RoomDecoration> decorations) {
  /** Creates and validates one connected, collision-free layout. */
  public RoomLayout {
    rows = List.copyOf(Objects.requireNonNull(rows, "rows"));
    Objects.requireNonNull(startPoint, "startPoint");
    riddlePlacements =
        RoomModelChecks.copyUnique(
            riddlePlacements, RiddlePlacement::riddleId, "riddle placements");
    riddleDoors = List.copyOf(Objects.requireNonNull(riddleDoors, "riddleDoors"));
    Objects.requireNonNull(doorPoint, "doorPoint");
    Objects.requireNonNull(exitPoint, "exitPoint");
    decorations = List.copyOf(Objects.requireNonNull(decorations, "decorations"));
    validateRows(rows, riddleDoors.size() + 1);

    Set<RoomPoint> occupied = new HashSet<>();
    requireTile(rows, startPoint, '.');
    requireUnique(occupied, startPoint);
    for (RoomPoint station : stations(riddlePlacements).toList()) {
      char tile = tile(rows, station);
      if (tile != '.' && tile != '#') {
        throw new IllegalArgumentException("station must stand on floor or wall: " + station);
      }
      requireUnique(occupied, station);
    }
    for (RiddleDoor door : riddleDoors) {
      requireTile(rows, door.point(), 'D');
      requireUnique(occupied, door.point());
    }
    requireTile(rows, doorPoint, 'D');
    requireUnique(occupied, doorPoint);
    requireTile(rows, exitPoint, 'E');
    requireUnique(occupied, exitPoint);
    List<String> tiles = rows;
    decorations.forEach(decoration -> tile(tiles, decoration.tile()));

    Set<RoomPoint> riddleDoorPoints =
        riddleDoors.stream().map(RiddleDoor::point).collect(Collectors.toSet());
    Set<RoomPoint> allDoorPoints = new HashSet<>(riddleDoorPoints);
    allDoorPoints.add(doorPoint);
    Set<RoomPoint> allOpen = reachable(rows, startPoint, allDoorPoints);
    if (!stations(riddlePlacements).allMatch(station -> accessible(tiles, station, allOpen))) {
      throw new IllegalArgumentException("every station must be reachable once all doors open");
    }
    if (!allOpen.contains(exitPoint)) {
      throw new IllegalArgumentException("the exit must be reachable through the exit door");
    }
    if (reachable(rows, startPoint, riddleDoorPoints).contains(exitPoint)) {
      throw new IllegalArgumentException("the closed exit door must be the only path to the exit");
    }
  }

  private static Stream<RoomPoint> stations(final List<RiddlePlacement> placements) {
    return placements.stream()
        .flatMap(
            placement ->
                Stream.concat(
                    placement.components().stream().map(ComponentPlacement::point),
                    placement.hintPoint().stream()));
  }

  private static boolean accessible(
      final List<String> rows, final RoomPoint station, final Set<RoomPoint> reach) {
    return reach.contains(station)
        || (tile(rows, station) == '#' && neighbors(station).stream().anyMatch(reach::contains));
  }

  private static void validateRows(final List<String> rows, final int expectedDoors) {
    if (rows.isEmpty() || rows.getFirst().isEmpty()) {
      throw new IllegalArgumentException("room rows must not be empty");
    }
    int width = rows.getFirst().length();
    long doorCount = 0;
    long exitCount = 0;
    for (String row : rows) {
      Objects.requireNonNull(row, "row");
      if (row.length() != width || !row.matches("[ .#DE]+")) {
        throw new IllegalArgumentException("room rows must be uniform and use supported tiles");
      }
      doorCount += row.chars().filter(tile -> tile == 'D').count();
      exitCount += row.chars().filter(tile -> tile == 'E').count();
    }
    if (doorCount != expectedDoors || exitCount != 1) {
      throw new IllegalArgumentException(
          "room rows must contain exactly the declared doors and one exit");
    }
  }

  private static void requireUnique(final Set<RoomPoint> occupied, final RoomPoint point) {
    Objects.requireNonNull(point, "point");
    if (!occupied.add(point)) {
      throw new IllegalArgumentException("room placements must not collide: " + point);
    }
  }

  private static void requireTile(
      final List<String> rows, final RoomPoint point, final char expected) {
    if (tile(rows, point) != expected) {
      throw new IllegalArgumentException("unexpected tile at " + point + "; expected " + expected);
    }
  }

  private static char tile(final List<String> rows, final RoomPoint point) {
    Objects.requireNonNull(point, "point");
    if (point.y() >= rows.size() || point.x() >= rows.get(point.y()).length()) {
      throw new IllegalArgumentException("point lies outside room layout: " + point);
    }
    return rows.get(point.y()).charAt(point.x());
  }

  private static Set<RoomPoint> reachable(
      final List<String> rows, final RoomPoint start, final Set<RoomPoint> openDoors) {
    Set<RoomPoint> visited = new HashSet<>();
    ArrayDeque<RoomPoint> pending = new ArrayDeque<>();
    pending.add(start);
    while (!pending.isEmpty()) {
      RoomPoint current = pending.removeFirst();
      if (!visited.add(current)) {
        continue;
      }
      for (RoomPoint next : neighbors(current)) {
        if (next.y() >= rows.size() || next.x() >= rows.get(next.y()).length()) {
          continue;
        }
        char tile = tile(rows, next);
        boolean walkable = tile == '.' || tile == 'E' || openDoors.contains(next);
        if (walkable && !visited.contains(next)) {
          pending.addLast(next);
        }
      }
    }
    return Set.copyOf(visited);
  }

  private static List<RoomPoint> neighbors(final RoomPoint point) {
    List<RoomPoint> result = new ArrayList<>(4);
    result.add(new RoomPoint(point.x() + 1, point.y()));
    if (point.x() > 0) {
      result.add(new RoomPoint(point.x() - 1, point.y()));
    }
    result.add(new RoomPoint(point.x(), point.y() + 1));
    if (point.y() > 0) {
      result.add(new RoomPoint(point.x(), point.y() - 1));
    }
    return List.copyOf(result);
  }
}
