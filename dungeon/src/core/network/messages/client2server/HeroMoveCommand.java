package core.network.messages.client2server;

import static contrib.entities.HeroFactory.ENABLE_MOUSE_MOVEMENT;

import contrib.components.PathComponent;
import core.Game;
import core.components.VelocityComponent;
import core.utils.Direction;
import core.utils.Vector2;
import core.utils.components.MissingComponentException;

/**
 * Record representing a hero movement command.
 *
 * @param direction The direction of movement.
 */
public record HeroMoveCommand(Direction direction) implements ClientMessage {
  @Override
  public void process() {
    Game.hero()
        .ifPresent(
            hero -> {
              VelocityComponent vc =
                  hero.fetch(VelocityComponent.class)
                      .orElseThrow(
                          () -> MissingComponentException.build(hero, VelocityComponent.class));

              Vector2 newVelocity = vc.currentVelocity();
              if (direction.x() != 0) {
                newVelocity = Vector2.of(direction.scale(vc.velocity()).x(), newVelocity.y());
              }
              if (direction.y() != 0) {
                newVelocity = Vector2.of(newVelocity.x(), direction.scale(vc.velocity()).y());
              }
              vc.currentVelocity(newVelocity);

              // Abort any path finding on own movement
              if (ENABLE_MOUSE_MOVEMENT) {
                hero.fetch(PathComponent.class).ifPresent(PathComponent::clear);
              }
            });
  }
}
