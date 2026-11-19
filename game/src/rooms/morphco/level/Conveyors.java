package rooms.morphco.level;

import engine.Component;
import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.VelocityComponent;
import engine.utils.Direction;
import engine.utils.Rectangle;
import engine.utils.Vector2;
import feature.components.CollideComponent;
import java.util.Set;

/**
 * Carries the players along the conveyor belts of the halls, on the server.
 *
 * <p>A belt pushes a player whose feet are on it in the direction of its arrows, with less force
 * than walking: the player rides along, walks against the belt more slowly and can step off at any
 * time. Walls, machines and closed doors stop a pushed player as they stop a walking one. A
 * repaired belt pushes steadily, a broken one jerks, like its picture.
 */
final class Conveyors {

  private static final String FORCE = "conveyor";

  // A third of the walking force: standing on a running belt moves a player two tiles a second.
  private static final float PUSH = 1.6f;

  // A broken belt pushes for the first part of every second.
  private static final int JERK_TICKS = Game.TICKS_PER_SECOND / 4;

  /**
   * The belt of a {@code belt} object.
   *
   * @param direction direction of its arrows
   * @param surface walkable surface between the rails, in world coordinates
   */
  record Belt(Direction direction, Rectangle surface) implements Component {}

  private Conveyors() {}

  static void push() {
    boolean jerk = Game.currentTick() % Game.TICKS_PER_SECOND < JERK_TICKS;
    Game.levelEntities(Set.of(Belt.class)).forEach(entity -> push(entity, jerk));
  }

  private static void push(Entity entity, boolean jerk) {
    String state =
        entity.fetch(DrawComponent.class).orElseThrow().stateMachine().getCurrentStateName();
    if (!state.equals(MorphCoProps.ON) && !jerk) return;
    Belt belt = entity.fetch(Belt.class).orElseThrow();
    Vector2 force = belt.direction().scale(PUSH);
    Game.allPlayers()
        .filter(
            player ->
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
