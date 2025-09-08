package components;

import contrib.entities.MonsterBuilder;
import core.Component;
import core.level.elements.ILevel;
import core.utils.Point;

/**
 * A component that stores the spawn parameters of a mob spawner entity.
 *
 * <p>A mob spawner is an entity that can spawn monsters around it. The mob spawner has a set of
 * monster types that it can spawn, a maximum number of monsters that it can spawn, and a set of
 * spawn parameters (e.g., spawn delay, spawn radius).
 *
 * @see entities.MobSpawnerFactory MobSpawnerFactory
 * @see systems.MobSpawnerSystem MobSpawnerSystem
 */
public class MobSpawnerComponent implements Component {
  private final MonsterBuilder[] monsterTypes;
  private final int maxMobCount;
  private final int minSpawnDelay;
  private final int maxSpawnDelay;
  private final int minSpawnRadius;
  private final int maxSpawnRadius;

  private int currentSpawnDelay;

  /**
   * Constructs a new MobSpawnerComponent with the given parameters.
   *
   * @param monsterTypes The types of monsters that the mob spawner can spawn.
   * @param maxMobCount The maximum number of monsters allowed around the mob spawner.
   * @param minSpawnDelay The minimum delay between monster spawns.
   * @param maxSpawnDelay The maximum delay between monster spawns.
   * @param minSpawnRadius The minimum radius around the mob spawner where monsters can spawn.
   * @param maxSpawnRadius The maximum radius around the mob spawner where monsters can spawn.
   */
  public MobSpawnerComponent(
      MonsterBuilder[] monsterTypes,
      int maxMobCount,
      int minSpawnDelay,
      int maxSpawnDelay,
      int minSpawnRadius,
      int maxSpawnRadius) {
    this.monsterTypes = monsterTypes;
    this.maxMobCount = maxMobCount;
    this.minSpawnDelay = minSpawnDelay;
    this.maxSpawnDelay = maxSpawnDelay;
    this.minSpawnRadius = minSpawnRadius;
    this.maxSpawnRadius = maxSpawnRadius;
    setNextSpawnDelay();
  }

  /**
   * Spawns a random monster at the given position.
   *
   * @param position The position where the monster should be spawned.
   */
  public void spawnRandomMonster(Point position) {
    getRandomMonsterType().addToGame().build(position);

    setNextSpawnDelay();
  }

  private void setNextSpawnDelay() {
    this.currentSpawnDelay = ILevel.RANDOM.nextInt(maxSpawnDelay - minSpawnDelay) + minSpawnDelay;
  }

  /**
   * Returns a random MonsterType from the array of monster types.
   *
   * <p>This method uses the game's current level's random number generator to select a random index
   * from the array of monster types. The MonsterType at the selected index is then returned.
   *
   * @return A random MonsterType from the array of monster types.
   */
  private MonsterBuilder<?> getRandomMonsterType() {
    return monsterTypes[ILevel.RANDOM.nextInt(monsterTypes.length)];
  }

  /**
   * Returns the array of MonsterTypes that the mob spawner can spawn.
   *
   * @return The array of MonsterTypes that the mob spawner can spawn.
   */
  public MonsterBuilder<?>[] monsterTypes() {
    return monsterTypes;
  }

  /**
   * Returns the maximum number of monsters that can be around the mob spawner.
   *
   * @return The maximum number of monsters that can be around the mob spawner.
   */
  public int maxMobCount() {
    return maxMobCount;
  }

  /**
   * Returns the current delay between monster spawns.
   *
   * @return The current delay between monster spawns.
   */
  public int spawnDelay() {
    return currentSpawnDelay;
  }

  /**
   * Returns the maximum radius around the mob spawner where monsters can spawn.
   *
   * @return The maximum radius around the mob spawner where monsters can spawn.
   */
  public int maxSpawnRadius() {
    return maxSpawnRadius;
  }

  /**
   * Returns the minimum radius around the mob spawner where monsters can spawn.
   *
   * @return The minimum radius around the mob spawner where monsters can spawn.
   */
  public int minSpawnRadius() {
    return minSpawnRadius;
  }
}
