package entities;

import contrib.components.CollideComponent;
import contrib.components.SpikyComponent;
import contrib.utils.components.health.DamageType;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.utils.Coordinate;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;

public class BlocklyMonsterFactory {
  private static final IPath HEDGEHOG = new SimpleIPath("character/monster/big_daemon");

  public static Entity hedgehog(Coordinate coordinate, String name) {
    Entity entity = new Entity(name);
    PositionComponent pc = new PositionComponent();
    pc.position(Game.tileAT(coordinate));
    entity.add(pc);
    entity.add(new CollideComponent());
    entity.add(new SpikyComponent(100, DamageType.PHYSICAL, 0));
    try {
      entity.add(new DrawComponent(HEDGEHOG));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return entity;
  }
}
