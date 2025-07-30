package core.systems;

import core.Entity;
import core.Game;
import core.System;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.utils.Vector2;
import core.utils.components.MissingComponentException;

public class FrictionSystem extends System {

  public FrictionSystem() {
    super(VelocityComponent.class, PositionComponent.class);
  }

  @Override
  public void execute() {
    filteredEntityStream().map(this::buildDataObject).forEach(this::applyFriction);
  }

  private void applyFriction(FSData data) {
    float friction = Game.tileAT(data.pc.position()).friction();
    Vector2 force = data.vc().currentVelocity().scale(friction).inverse();
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
