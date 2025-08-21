package core.systems;

import contrib.components.FlyComponent;
import core.Entity;
import core.Game;
import core.System;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.Tile;
import core.utils.Vector2;
import core.utils.components.MissingComponentException;

/**
 * System that applies friction forces to entities.
 *
 * <p>This system processes all entities that have both a {@link VelocityComponent} and a {@link
 * PositionComponent}. It calculates the friction force based on the friction value of the tile at
 * the entity’s current position, then applies this force opposite to the entity’s current velocity.
 */
public class FrictionSystem extends System {

  private static final float DEFAULT_FRICTION = 0.0f;

  /** Create a new FrictionSystem. */
  public FrictionSystem() {
    super(VelocityComponent.class, PositionComponent.class);
  }

  @Override
  public void execute() {
    filteredEntityStream()
        .filter(entity -> !entity.isPresent(FlyComponent.class))
        .map(this::buildDataObject)
        .forEach(this::applyFriction);
  }

  private void applyFriction(FSData data) {
    float friction = Game.tileAt(data.pc.position()).map(Tile::friction).orElse(DEFAULT_FRICTION);
    Vector2 force = data.vc().currentVelocity().scale(friction).inverse();
    if (force.isZero()) force = Vector2.ZERO;
    data.vc.applyForce("Friction", force);
  }

  private FSData buildDataObject(Entity e) {
    VelocityComponent vc =
        e.fetch(VelocityComponent.class)
            .orElseThrow(() -> MissingComponentException.build(e, VelocityComponent.class));

    PositionComponent pc =
        e.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(e, PositionComponent.class));

    return new FSData(e, vc, pc);
  }

  private record FSData(Entity e, VelocityComponent vc, PositionComponent pc) {}
}
