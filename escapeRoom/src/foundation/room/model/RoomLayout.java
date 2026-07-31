package foundation.room.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Immutable engine-neutral room layout with rows ordered from top to bottom.
 *
 * <p>Rows use {@code #} for walls, {@code .} for ordinary floor, {@code D} for the common door, and
 * {@code E} for the common exit.
 *
 * @param rows uniform top-to-bottom tile rows
 * @param startPoint shared deterministic start for every player
 * @param riddlePlacements canonical authored riddle order with explicit hint points
 * @param doorPoint common door coordinate
 * @param exitPoint common exit coordinate
 */
public record RoomLayout(
    List<String> rows,
    RoomPoint startPoint,
    List<RiddlePlacement> riddlePlacements,
    RoomPoint doorPoint,
    RoomPoint exitPoint) {
  /** Creates and validates one connected, collision-free room layout. */
  public RoomLayout {
    rows = List.copyOf(Objects.requireNonNull(rows, "rows"));
    Objects.requireNonNull(startPoint, "startPoint");
    riddlePlacements =
        RoomModelChecks.copyUnique(
            riddlePlacements, RiddlePlacement::riddleId, "riddle placements");
    Objects.requireNonNull(doorPoint, "doorPoint");
    Objects.requireNonNull(exitPoint, "exitPoint");
    validateRows(rows);

    Set<RoomPoint> roomPositions = new HashSet<>();
    requireTile(rows, startPoint, '.');
    requireUnique(roomPositions, startPoint);
    for (RiddlePlacement placement : riddlePlacements) {
      for (ComponentPlacement component : placement.components()) {
        requireTile(rows, component.point(), '.');
        requireUnique(roomPositions, component.point());
      }
      if (placement.hintPoint().isPresent()) {
        RoomPoint hintPoint = placement.hintPoint().orElseThrow();
        requireTile(rows, hintPoint, '.');
        requireUnique(roomPositions, hintPoint);
      }
    }
    requireTile(rows, doorPoint, 'D');
    Set<RoomPoint> occupied = new HashSet<>(roomPositions);
    requireUnique(occupied, doorPoint);
    requireTile(rows, exitPoint, 'E');
    requireUnique(occupied, exitPoint);
    Set<RoomPoint> roomReachable = reachable(rows, startPoint, Set.of('.', 'E'));
    if (!roomReachable.containsAll(roomPositions)) {
      throw new IllegalArgumentException(
          "the start, all riddles, and all hints must be connected while the door is closed");
    }
    if (neighbors(doorPoint).stream().noneMatch(roomReachable::contains)) {
      throw new IllegalArgumentException("the door must be reachable from inside the room");
    }
    if (roomReachable.contains(exitPoint)) {
      throw new IllegalArgumentException("the closed door must be the only path to the exit");
    }
    if (!reachable(rows, startPoint, Set.of('.', 'D', 'E')).contains(exitPoint)) {
      throw new IllegalArgumentException("the exit must be reachable through the door");
    }
  }

  private static void validateRows(final List<String> rows) {
    if (rows.isEmpty() || rows.getFirst().isEmpty()) {
      throw new IllegalArgumentException("room rows must not be empty");
    }
    int width = rows.getFirst().length();
    long doorCount = 0;
    long exitCount = 0;
    for (String row : rows) {
      Objects.requireNonNull(row, "row");
      if (row.length() != width || !row.matches("[.#DE]+")) {
        throw new IllegalArgumentException("room rows must be uniform and use supported tiles");
      }
      doorCount += row.chars().filter(tile -> tile == 'D').count();
      exitCount += row.chars().filter(tile -> tile == 'E').count();
    }
    if (doorCount != 1 || exitCount != 1) {
      throw new IllegalArgumentException("room rows must contain exactly one door and one exit");
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
      final List<String> rows, final RoomPoint start, final Set<Character> walkableTiles) {
    Set<RoomPoint> visited = new HashSet<>();
    ArrayDeque<RoomPoint> pending = new ArrayDeque<>();
    pending.add(start);
    while (!pending.isEmpty()) {
      RoomPoint current = pending.removeFirst();
      if (!visited.add(current)) {
        continue;
      }
      for (RoomPoint next : neighbors(current)) {
        int x = next.x();
        int y = next.y();
        if (x >= 0 && y >= 0 && y < rows.size() && x < rows.get(y).length()) {
          if (walkableTiles.contains(tile(rows, next)) && !visited.contains(next)) {
            pending.addLast(next);
          }
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
