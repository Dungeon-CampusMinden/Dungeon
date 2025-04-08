package entities;

import contrib.components.HealthComponent;
import core.Entity;
import core.components.PositionComponent;
import core.level.utils.Coordinate;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.function.Consumer;

public class BlocklyMonsterFactory {
  private static final IPath KNIGHT = new SimpleIPath("character/knight");

  public static Entity hedgehog(Coordinate coordinate) {
    BlocklyMonster.BlocklyMonsterBuilder builder = BlocklyMonster.HEDGEHOG.builder();
    builder.spawnPoint(coordinate.toCenteredPoint());
    builder.range(0);
    return builder.build().orElseThrow();
  }

  public static Entity guard(
      Coordinate coordinate, PositionComponent.Direction viewDirection, int range) {
    BlocklyMonster.BlocklyMonsterBuilder builder = BlocklyMonster.GUARD.builder();
    builder.spawnPoint(coordinate.toCenteredPoint());
    builder.range(range);
    builder.viewDirection(viewDirection);
    return builder.build().orElseThrow();
  }

  public static Entity knight(
      Coordinate coordinate, PositionComponent.Direction viewDirection, Consumer<Entity> onDeath) {
    BlocklyMonster.BlocklyMonsterBuilder builder = BlocklyMonster.BLACK_KNIGHT.builder();
    builder.spawnPoint(coordinate.toCenteredPoint());
    builder.viewDirection(viewDirection);
    builder.range(0);
    Entity knight = builder.build().orElseThrow();
    knight.fetch(HealthComponent.class).orElseThrow().onDeath(onDeath);
    return knight;
  }
}
