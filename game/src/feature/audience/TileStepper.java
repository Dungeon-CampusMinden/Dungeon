package feature.audience;

import engine.Entity;
import engine.Game;
import engine.components.PositionComponent;
import engine.components.VelocityComponent;
import engine.level.Tile;
import engine.utils.Direction;
import engine.utils.Point;
import engine.utils.Vector2;
import feature.collision.Collider;
import feature.collision.CollisionUtils;
import feature.components.CollideComponent;
import feature.interaction.InteractionComponent;
import feature.systems.PositionSync;
import feature.utils.EntityUtils;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Moves the hero exactly one tile per voted step and interacts with the tile in front of it, or
 * walks it to a chosen {@link AudienceTarget} along the shortest free route.
 *
 * <p>{@link #tick()} must run before the {@code VelocitySystem} in every frame: it steers the hero
 * toward the destination through its velocity, so the walking animation plays, and settles it
 * exactly on the destination tile. A route is a series of such steps.
 *
 * <p>The hero stands on a tile with the center of its hitbox on the center of the tile. For a
 * sprite of one tile that is the tile origin. A larger sprite, such as the rogue's, has its hitbox
 * away from its position, and centering the hitbox keeps it on the tile it counts as standing on.
 */
final class TileStepper {
  /** Tiles per second while stepping. */
  private static final float SPEED = 4f;

  // A hitbox has to reach this far into a tile to take it up, so rounding does not add a neighbor.
  private static final float OVERLAP = 0.01f;

  private static final List<Direction> DIRECTIONS =
      List.of(Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT);

  private final Entity hero;
  private Point target;
  // Steps of a route still to walk; onArrival runs at its end and is null without a route.
  private final Deque<Direction> path = new ArrayDeque<>();
  private Runnable onArrival;

  TileStepper(Entity hero) {
    this.hero = hero;
  }

  /**
   * Puts the hero on the tile the game placed it on, centered like after a step. The game places
   * heroes at a tile origin, which is off the tile for a hero with a larger sprite.
   */
  void snap() {
    PositionComponent position = hero.fetch(PositionComponent.class).orElseThrow();
    Game.tileAt(position.position()).ifPresent(this::place);
  }

  /**
   * Puts the hero on a tile right away; a step or route under way ends.
   *
   * @param tile the tile to stand on
   */
  void place(Tile tile) {
    target = null;
    path.clear();
    onArrival = null;
    hero.fetch(VelocityComponent.class)
        .ifPresent(
            velocity -> {
              velocity.clearForces();
              velocity.currentVelocity(Vector2.ZERO);
            });
    hero.fetch(PositionComponent.class).orElseThrow().position(positionOn(tile));
    PositionSync.syncPosition(hero);
  }

  /**
   * @return whether the hero is still walking, to the destination of the last step or along a route
   */
  boolean moving() {
    return target != null || onArrival != null;
  }

  /**
   * Turns the hero toward {@code direction} and starts walking one tile when that tile is free.
   *
   * @param direction voted direction
   * @return whether the hero started walking
   */
  boolean step(Direction direction) {
    PositionComponent position = hero.fetch(PositionComponent.class).orElseThrow();
    position.viewDirection(direction);
    Optional<Tile> current = Game.tileAtEntity(hero);
    if (current.isEmpty()) return false;
    Optional<Tile> next = Game.tileAt(current.get().position(), direction);
    if (next.isEmpty() || !next.get().isAccessible()) return false;
    Point start = positionOn(current.get());
    Point destination = positionOn(next.get());
    if (hero.isPresent(CollideComponent.class)
        && CollisionUtils.isCollidingWithOtherSolids(hero, destination)) {
      return false;
    }
    // Start centered on the current tile so every step ends exactly on the next tile.
    position.position(start);
    PositionSync.syncPosition(hero);
    target = destination;
    return true;
  }

  /**
   * Walks the hero to a target. At an approached target the hero turns to it and uses it.
   *
   * @param target an entity marked with {@link AudienceTarget}
   * @return whether the hero can get there
   */
  boolean goTo(Entity target) {
    Optional<Route> route = route(target, reachable());
    if (route.isEmpty()) return false;
    path.clear();
    path.addAll(route.get().steps());
    onArrival = () -> arrive(target, route.get().face());
    return true;
  }

  // Turns to an approached target and uses it; an entered target needs nothing more.
  private void arrive(Entity target, Direction face) {
    if (face == Direction.NONE) return;
    hero.fetch(PositionComponent.class).orElseThrow().viewDirection(face);
    target
        .fetch(InteractionComponent.class)
        .ifPresent(interaction -> interaction.triggerInteraction(target, hero));
  }

  /**
   * Finds the tiles the hero can walk to from where it stands. A tile counts when a step onto it
   * would work, so it must be accessible and leave the hero's hitbox clear of solid entities.
   *
   * @return result of {@link #reach}, or empty when the hero stands on no tile
   */
  Map<Point, Point> reachable() {
    return Game.tileAtEntity(hero)
        .map(tile -> reach(tile.position(), this::walkable))
        .orElse(Map.of());
  }

  private boolean walkable(Point tile) {
    if (!Game.tileAt(tile).map(Tile::isAccessible).orElse(false)) return false;
    return !hero.isPresent(CollideComponent.class)
        || !CollisionUtils.isCollidingWithOtherSolids(hero, positionOn(hero, tile));
  }

  /**
   * Plans the way to a target.
   *
   * @param target an entity to approach, or to enter when it has no {@code InteractionComponent}
   * @param reachable result of {@link #reachable()}
   * @return the route, or empty when the hero cannot get there
   */
  Optional<Route> route(Entity target, Map<Point, Point> reachable) {
    return route(reachable, tiles(target), !target.isPresent(InteractionComponent.class));
  }

  /**
   * Searches breadth first from {@code start} over walkable tiles.
   *
   * @param start tile the search starts on; it counts as walkable
   * @param walkable whether the hero can stand on a tile
   * @return every reached tile mapped to the tile the search came from, nearest first; {@code
   *     start} maps to itself
   */
  static Map<Point, Point> reach(Point start, Predicate<Point> walkable) {
    Map<Point, Point> from = new LinkedHashMap<>();
    from.put(start, start);
    Deque<Point> queue = new ArrayDeque<>(List.of(start));
    while (!queue.isEmpty()) {
      Point tile = queue.poll();
      for (Direction direction : DIRECTIONS) {
        Point next = tile.translate(direction);
        if (from.containsKey(next) || !walkable.test(next)) continue;
        from.put(next, tile);
        queue.add(next);
      }
    }
    return from;
  }

  /**
   * Picks the nearest reached tile from which the hero gets to a target, and the steps there.
   *
   * @param reached result of {@link #reach}
   * @param covered tiles the target takes up
   * @param enter whether the hero walks onto a covered tile instead of next to one
   * @return the route, or empty when no reached tile fits
   */
  static Optional<Route> route(Map<Point, Point> reached, Set<Point> covered, boolean enter) {
    for (Point tile : reached.keySet()) {
      Optional<Direction> face =
          enter
              ? Optional.of(Direction.NONE).filter(none -> covered.contains(tile))
              : facing(tile, covered);
      if (face.isPresent()) return Optional.of(new Route(steps(reached, tile), face.get()));
    }
    return Optional.empty();
  }

  // The direction from a tile next to the target toward it; empty for any other tile.
  private static Optional<Direction> facing(Point tile, Set<Point> covered) {
    if (covered.contains(tile)) return Optional.empty();
    return DIRECTIONS.stream()
        .filter(direction -> covered.contains(tile.translate(direction)))
        .findFirst();
  }

  // The steps from the start of the search to a reached tile.
  private static List<Direction> steps(Map<Point, Point> reached, Point end) {
    List<Direction> steps = new ArrayList<>();
    Point tile = end;
    while (!reached.get(tile).equals(tile)) {
      Point previous = reached.get(tile);
      Point next = tile;
      steps.addFirst(
          DIRECTIONS.stream()
              .filter(direction -> previous.translate(direction).equals(next))
              .findFirst()
              .orElseThrow());
      tile = previous;
    }
    return steps;
  }

  /**
   * Returns the tiles an entity takes up, as {@link #covers} decides it.
   *
   * @param entity an entity with a position
   * @return origins of the tiles
   */
  static Set<Point> tiles(Entity entity) {
    PositionSync.syncPosition(entity);
    Optional<CollideComponent> hitbox = entity.fetch(CollideComponent.class);
    if (hitbox.isEmpty()) {
      Point position = entity.fetch(PositionComponent.class).orElseThrow().position();
      return Set.of(new Point((float) Math.floor(position.x()), (float) Math.floor(position.y())));
    }
    Collider collider = hitbox.get().collider();
    Set<Point> tiles = new HashSet<>();
    for (int x = (int) Math.floor(collider.absoluteLeft()); x < collider.absoluteRight(); x++) {
      for (int y = (int) Math.floor(collider.absoluteBottom()); y < collider.absoluteTop(); y++) {
        Point tile = new Point(x, y);
        if (covers(entity, tile)) tiles.add(tile);
      }
    }
    return tiles;
  }

  /** Interacts with the entity {@link #facing()} returns. Nothing happens when there is none. */
  void interact() {
    facing()
        .ifPresent(
            entity ->
                entity
                    .fetch(InteractionComponent.class)
                    .orElseThrow()
                    .triggerInteraction(entity, hero));
  }

  /**
   * Finds the entity a vote for "Benutzen" would use right now.
   *
   * @return the nearest interactable entity that {@link #covers} the tile the hero faces, or empty
   *     when there is none
   */
  Optional<Entity> facing() {
    PositionComponent position = hero.fetch(PositionComponent.class).orElseThrow();
    Optional<Tile> current = Game.tileAtEntity(hero);
    if (current.isEmpty()) return Optional.empty();
    Optional<Tile> front = Game.tileAt(current.get().position(), position.viewDirection());
    if (front.isEmpty()) return Optional.empty();
    Point heroPosition = position.position();
    Point tile = front.get().position();
    return Game.entities(Set.of(PositionComponent.class, InteractionComponent.class))
        .filter(entity -> entity.id() != hero.id())
        .filter(entity -> covers(entity, tile))
        .min(
            Comparator.comparingDouble(
                entity ->
                    entity
                        .fetch(PositionComponent.class)
                        .map(other -> heroPosition.distanceSquared(other.position()))
                        .orElse(Double.MAX_VALUE)));
  }

  /**
   * Checks whether an entity takes up a tile. An entity with a hitbox takes up every tile its
   * hitbox reaches into, so the hero can use a board two tiles wide from in front of either. An
   * entity without a hitbox takes up the tile of its position. The size of a sprite does not count,
   * because the server does not know the size of single images.
   *
   * @param entity an entity with a position
   * @param tile origin of the tile
   * @return whether the entity takes up the tile
   */
  static boolean covers(Entity entity, Point tile) {
    PositionSync.syncPosition(entity);
    Optional<CollideComponent> hitbox = entity.fetch(CollideComponent.class);
    if (hitbox.isEmpty()) {
      Point position = entity.fetch(PositionComponent.class).orElseThrow().position();
      return Math.floor(position.x()) == tile.x() && Math.floor(position.y()) == tile.y();
    }
    Collider collider = hitbox.get().collider();
    return reaches(collider.absoluteLeft(), collider.absoluteRight(), tile.x())
        && reaches(collider.absoluteBottom(), collider.absoluteTop(), tile.y());
  }

  // Whether the span from start to end reaches into the tile starting at origin.
  private static boolean reaches(float start, float end, float origin) {
    return start < origin + 1 - OVERLAP && end > origin + OVERLAP;
  }

  /** Keeps the hero standing during a pause; the step goes on with the next {@link #tick()}. */
  void hold() {
    if (target == null) return;
    VelocityComponent velocity = hero.fetch(VelocityComponent.class).orElseThrow();
    velocity.clearForces();
    velocity.currentVelocity(Vector2.ZERO);
  }

  // The hero position that puts the center of its hitbox on the center of the tile.
  private Point positionOn(Tile tile) {
    return positionOn(hero, tile.position());
  }

  /**
   * Returns the position that puts the center of an entity's hitbox on the center of a tile.
   *
   * @param entity an entity with a position
   * @param tile origin of the tile
   * @return the position for the entity
   */
  static Point positionOn(Entity entity, Point tile) {
    PositionSync.syncPosition(entity);
    Point position = entity.fetch(PositionComponent.class).orElseThrow().position();
    Point center = EntityUtils.getPosition(entity);
    return new Point(
        tile.x() + 0.5f - (center.x() - position.x()),
        tile.y() + 0.5f - (center.y() - position.y()));
  }

  /**
   * Steers the hero toward the destination and settles it there, then takes the next step of a
   * route; call once per frame.
   */
  void tick() {
    if (target == null && onArrival != null) {
      if (path.isEmpty()) {
        Runnable arrived = onArrival;
        onArrival = null;
        arrived.run();
        return;
      }
      if (!step(path.poll())) {
        // Something blocks the way now; the audience decides again.
        path.clear();
        onArrival = null;
        return;
      }
    }
    if (target == null) return;
    VelocityComponent velocity = hero.fetch(VelocityComponent.class).orElseThrow();
    PositionComponent position = hero.fetch(PositionComponent.class).orElseThrow();
    velocity.clearForces();
    Point from = position.position();
    float delta = 1f / Game.frameRate();
    float distance = Point.calculateDistance(from, target);
    if (distance <= SPEED * delta) {
      velocity.currentVelocity(Vector2.ZERO);
      position.position(target);
      PositionSync.syncPosition(hero);
      target = null;
      return;
    }
    velocity.currentVelocity(from.vectorTo(target).normalize().scale(SPEED));
  }

  /**
   * Way to a target.
   *
   * @param steps steps to walk, in order
   * @param face direction toward the target at the end, or {@link Direction#NONE} for a target the
   *     hero walks onto
   */
  record Route(List<Direction> steps, Direction face) {}
}
