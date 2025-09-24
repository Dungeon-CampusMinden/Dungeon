package systems;

import components.LasergridComponent;
import contrib.components.CollideComponent;
import contrib.components.SpikyComponent;
import contrib.utils.components.health.DamageType;
import core.Entity;
import core.System;
import core.components.DrawComponent;

public class LaserSystem extends System {

  public LaserSystem() {
    super(LasergridComponent.class);
  }

  @Override
  public void execute() {
    filteredEntityStream().map(this::buildDataObject).forEach(this::applyLaserLogic);
  }

  private LaserSystemData buildDataObject(Entity entity) {
    LasergridComponent laser =
        entity
            .fetch(LasergridComponent.class)
            .orElseThrow(() -> new IllegalStateException("LasergridComponent fehlt!"));

    DrawComponent draw = entity.fetch(DrawComponent.class).orElse(null);
    SpikyComponent spiky = entity.fetch(SpikyComponent.class).orElse(null);

    return new LaserSystemData(entity, laser, draw, spiky);
  }

  private void applyLaserLogic(LaserSystemData data) {
    if (data.lasergrid().isActive()) {
      // Laser ist AN
      if (data.draw() != null) {
        data.draw().sendSignal("activate_laser_grid");
      }
      if (data.spiky() == null) {
        data.entity().add(new CollideComponent());
        data.entity().add(new SpikyComponent(9999, DamageType.PHYSICAL, 10));
      }
    } else {
      // Laser ist AUS
      if (data.draw() != null) {
        data.draw().sendSignal("deactivate_laser_grid");
      }
      if (data.spiky() != null) {
        data.entity().remove(SpikyComponent.class);
        data.entity().remove(CollideComponent.class);
      }
    }
  }

  private record LaserSystemData(
      Entity entity, LasergridComponent lasergrid, DrawComponent draw, SpikyComponent spiky) {}
}
