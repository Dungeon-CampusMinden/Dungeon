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
    float damp = Math.max(0.0f, 1.0f - friction);
    // If we hit a wall, damp the raw velocity; otherwise damp the movement velocity
    Vector2 toDampen = data.vc().currentVelocity();
    float newVX = toDampen.x() * damp;
    if (Math.abs(newVX) < 0.01f) newVX = 0.0f;
    float newVY = toDampen.y() * damp;
    if (Math.abs(newVY) < 0.01f) newVY = 0.0f;

    // TODO replace with force
    data.vc.currentVelocity(Vector2.of(newVX, newVY));
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
