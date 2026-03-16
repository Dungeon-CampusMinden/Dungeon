package entities;

import client.Client;
import contrib.entities.EntityFactory;
import core.Entity;
import core.Game;
import core.components.InputComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.configuration.KeyboardConfig;
import core.utils.Direction;
import core.utils.Vector2;
import core.utils.components.MissingComponentException;
import level.BlocklyLevel;

import java.io.IOException;
import java.util.Objects;

/**
 * This class is used to create a player entity with tank controls. The player can only move in the
 * direction it is facing. The tank controls are implemented by removing the original movement
 * controls and adding new controls for moving forward and turning left or right.
 */
public class HeroTankControlledFactory {

  /**
   * Creates a new player with tank controls. The player can only move in the direction it is
   * facing.
   *
   * @param tankControlls True if the Tanke Controlls should be maped to the default movement keys
   * @return the player entity
   * @throws IOException if there is an error creating the player
   */
  public static Entity blocklyHero(boolean tankControlls) {
    Entity hero = EntityFactory.newHero(innerHero -> Client.restart());
    InputComponent ic = hero.fetch(InputComponent.class).orElse(new InputComponent());

    // Remove any original movement controls except CLOSE_UI
    ic.removeCallbacksIf(
        (entry ->
            !Objects.equals(
                entry.getKey(), contrib.configuration.KeyboardConfig.CLOSE_UI.value())));

    if (tankControlls) {
      // Add tank controls
      ic.registerCallback(
          KeyboardConfig.MOVEMENT_UP.value(),
          HeroTankControlledFactory::moveEntityInFacingDirection);

      // Add rotation controls
      ic.registerCallback(
          KeyboardConfig.MOVEMENT_LEFT.value(), (entity) -> rotatePlayer(Direction.LEFT), false);
      ic.registerCallback(
          KeyboardConfig.MOVEMENT_RIGHT.value(), (entity) -> rotatePlayer(Direction.RIGHT), false);
    }

    ic.registerCallback(
        KeyboardConfig.PAUSE.value(),
        ignored -> {
          Game.currentLevel().ifPresent(level -> ((BlocklyLevel) level).showPopups());
        },
        false);

    return hero;
  }

  private static void moveEntityInFacingDirection(Entity entity) {
    PositionComponent pc =
        entity
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));
    VelocityComponent vc =
        entity
            .fetch(VelocityComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, VelocityComponent.class));

    Vector2 newVelocity = pc.viewDirection().scale(Client.MOVEMENT_FORCE);
    vc.applyForce("MOVEMENT", newVelocity);
  }

  private static void rotatePlayer(Direction direction) {
    Game.player()
        .flatMap(hero -> hero.fetch(PositionComponent.class))
        .ifPresent(pc -> pc.viewDirection(pc.viewDirection().applyRelative(direction)));
  }
}
