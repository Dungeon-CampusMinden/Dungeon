package entities;

import contrib.components.HealthComponent;
import core.Entity;
import core.components.PositionComponent;
import core.level.utils.Coordinate;
import java.util.function.Consumer;

/**
 * Factory class for creating predefined BlocklyMonster entities.
 *
 * <p>This class provides convenience methods to instantiate different types of monsters used in
 * Blockly-based scenarios with specified properties such as spawn location, view direction, range,
 * and optional on-death behavior.
 */
public class BlocklyMonsterFactory {

  /**
   * Creates a hedgehog monster entity at the specified coordinate.
   *
   * <p>The hedgehog has a range of 0 and is centered on the tile.
   *
   * @param coordinate The coordinate where the hedgehog should spawn.
   * @return A fully configured hedgehog entity.
   * @throws java.util.NoSuchElementException if building the monster fails.
   */
  public static Entity hedgehog(Coordinate coordinate) {
    BlocklyMonster.BlocklyMonsterBuilder builder = BlocklyMonster.HEDGEHOG.builder();
    builder.spawnPoint(coordinate.toCenteredPoint());
    builder.range(0);
    return builder.build().orElseThrow();
  }

  /**
   * Creates a guard monster entity at the specified coordinate with the given view direction and
   * range.
   *
   * @param coordinate The spawn coordinate of the guard.
   * @param viewDirection The initial view direction of the guard.
   * @param range The movement or detection range of the guard.
   * @return A fully configured guard entity.
   * @throws java.util.NoSuchElementException if building the monster fails.
   */
  public static Entity guard(
      Coordinate coordinate, PositionComponent.Direction viewDirection, int range) {
    BlocklyMonster.BlocklyMonsterBuilder builder = BlocklyMonster.GUARD.builder();
    builder.spawnPoint(coordinate.toCenteredPoint());
    builder.range(range);
    builder.viewDirection(viewDirection);
    return builder.build().orElseThrow();
  }

  /**
   * Creates a knight monster entity at the specified coordinate with the given view direction.
   *
   * <p>An optional consumer can be provided that will be triggered upon the knight's death.
   *
   * @param coordinate The spawn coordinate of the knight.
   * @param viewDirection The initial view direction of the knight.
   * @param onDeath A consumer to be executed when the knight dies.
   * @return A fully configured knight entity with a death handler.
   * @throws java.util.NoSuchElementException if building the monster fails.
   */
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
