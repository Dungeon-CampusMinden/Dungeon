package rooms.morphco.level;

import engine.Component;
import engine.Entity;
import engine.Game;
import engine.components.VelocityComponent;
import engine.utils.Direction;
import engine.utils.Rectangle;
import engine.utils.Vector2;
import feature.components.CollideComponent;
import feature.components.UIComponent;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Carries the players along the conveyor belts of the halls, on the server.
 *
 * <p>A belt pushes a player whose feet are on it in the direction of its arrows, with less force
 * than walking: the player rides along, walks against the belt more slowly and can step off at any
 * time. Walls, machines and closed doors stop a pushed player as they stop a walking one. A belt
 * pushes every tick, broken or repaired; only the picture of a broken belt jerks. A player with an
 * open dialog stays where they are, so they close it where they opened it.
 */
final class Conveyors {

  private static final String FORCE = "conveyor";

  // A third of the walking force: standing on a belt moves a player about two tiles a second.
  private static final float PUSH = 1.6f;

  /**
   * The belt of a {@code belt} object.
   *
   * @param direction direction of its arrows
   * @param surface walkable surface between the rails, in world coordinates
   */
  record Belt(Direction direction, Rectangle surface) implements Component {}

  private Conveyors() {}

  static void push() {
    Set<Integer> reading = playersInDialogs();
    Game.levelEntities(Set.of(Belt.class)).forEach(belt -> push(belt, reading));
  }

  private static Set<Integer> playersInDialogs() {
    return Game.entities()
        .flatMap(entity -> entity.fetch(UIComponent.class).stream())
        .filter(UIComponent::modal)
        .flatMapToInt(ui -> IntStream.of(ui.targetEntityIds()))
        .boxed()
        .collect(Collectors.toSet());
  }

  private static void push(Entity entity, Set<Integer> reading) {
    Belt belt = entity.fetch(Belt.class).orElseThrow();
    Vector2 force = belt.direction().scale(PUSH);
    Game.allPlayers()
        .filter(player -> !reading.contains(player.id()))
        .filter(player ->
            belt.surface()
                .contains(
                    player
                        .fetch(CollideComponent.class)
                        .orElseThrow()
                        .collider()
                        .absoluteBottomCenter()))
        .forEach(
            player -> player.fetch(VelocityComponent.class).orElseThrow().applyForce(FORCE, force));
  }
}
