package core.network.messages.client2server;

import static contrib.entities.HeroFactory.ENABLE_MOUSE_MOVEMENT;

import java.util.Optional;

import contrib.components.PathComponent;
import contrib.entities.HeroFactory;
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
              hero
                      .fetch(VelocityComponent.class)
                      .orElseThrow(
                          () -> MissingComponentException.build(hero, VelocityComponent.class));

              Optional<Vector2> existingForceOpt = vc.force(HeroFactory.MOVEMENT_ID);
              Vector2 newForce = HeroFactory.defaultHeroSpeed().scale(direction);

              Vector2 updatedForce =
                  existingForceOpt.map(existing -> existing.add(newForce)).orElse(newForce);

              if (updatedForce.lengthSquared() > 0) {
                updatedForce = updatedForce.normalize().scale(HeroFactory.defaultHeroSpeed().length());
                vc.applyForce(HeroFactory.MOVEMENT_ID, updatedForce);
              }

              if (ENABLE_MOUSE_MOVEMENT) {
                hero.fetch(PathComponent.class).ifPresent(PathComponent::clear);
              }
            });
  }
}
