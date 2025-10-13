package entities;

import client.Client;
import coderunner.BlocklyCodeRunner;
import coderunner.BlocklyCommands;
import coderunner.Direction;
import contrib.entities.EntityFactory;
import contrib.entities.HeroFactory;
import core.Entity;
import core.Game;
import core.components.InputComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.configuration.KeyboardConfig;
import core.utils.Vector2;
import core.utils.components.MissingComponentException;
import java.io.IOException;
import java.util.function.Consumer;
import level.BlocklyLevel;

/**
 * This class is used to create a hero entity with tank controls. The hero can only move in the
 * direction it is facing. The tank controls are implemented by removing the original movement
 * controls and adding new controls for moving forward and turning left or right.
 */
public class HeroTankControlledFactory {

  static {
    HeroFactory.heroDeath(
        entity -> {
          Client.restart();
        });
  }

  /**
   * Creates a new hero with tank controls. The hero can only move in the direction it is facing.
   *
   * @param tankControlls True if the Tanke Controlls should be maped to the default movement keys
   * @return the hero entity
   * @throws IOException if there is an error creating the hero
   */
  public static Entity blocklyHero(boolean tankControlls) throws IOException {
    Entity hero = EntityFactory.newHero();
    InputComponent ic = hero.fetch(InputComponent.class).orElse(new InputComponent());

    // Remove any original movement controls
    ic.removeCallbacks();
    HeroFactory.registerCloseUI(ic);

    if (tankControlls) {
      // Add tank controls
      ic.registerCallback(
          KeyboardConfig.MOVEMENT_UP.value(),
          HeroTankControlledFactory::moveEntityInFacingDirection);

      // Add rotation controls
      ic.registerCallback(
          KeyboardConfig.MOVEMENT_LEFT.value(),
          (entity) -> BlocklyCommands.rotate(Direction.LEFT),
          false);
      ic.registerCallback(
          KeyboardConfig.MOVEMENT_RIGHT.value(),
          (entity) -> BlocklyCommands.rotate(Direction.RIGHT),
          false);
    }

    ic.registerCallback(
        KeyboardConfig.PAUSE.value(),
        new Consumer<Entity>() {
          @Override
          public void accept(Entity entity) {
            if (!BlocklyCodeRunner.instance().isCodeRunning())
              Game.currentLevel().ifPresent(level -> ((BlocklyLevel) level).showPopups());
          }
        },
        false);

    return hero;
  }

  private static void moveEntityInFacingDirection(Entity entity) {
    PositionComponent pc =
        entity
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));
    core.utils.Direction direction = pc.viewDirection();
    VelocityComponent vc =
        entity
            .fetch(VelocityComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, VelocityComponent.class));

    Vector2 newVelocity = Vector2.ZERO;
    switch (direction) {
      case UP -> newVelocity = Vector2.of(0, Client.MOVEMENT_FORCE.y());
      case DOWN -> newVelocity = Vector2.of(0, -Client.MOVEMENT_FORCE.y());
      case LEFT -> newVelocity = Vector2.of(-Client.MOVEMENT_FORCE.x(), 0);
      case RIGHT -> newVelocity = Vector2.of(Client.MOVEMENT_FORCE.x(), 0);
    }
    vc.applyForce("MOVEMENT", newVelocity);
  }
}
