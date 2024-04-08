package systems;

import components.MobSpawnerComponent;
import contrib.components.AIComponent;
import core.Entity;
import core.Game;
import core.System;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.utils.Coordinate;
import core.level.utils.LevelUtils;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A system that handles the spawning of monsters around mob spawner entities.
 *
 * <p>A mob spawner is an entity that can spawn monsters around it. The mob spawner has a set of
 * monster types that it can spawn, a maximum number of monsters that it can spawn, and a set of
 * spawn parameters (e.g., spawn delay, spawn radius).
 *
 * @see components.MobSpawnerComponent MobSpawnerComponent
 * @see entities.MobSpawnerFactory MobSpawnerFactory
 */
public class MobSpawnerSystem extends System {

  private final Map<MobSpawnerComponent, Long> lastSpawnTimes = new HashMap<>();

  /**
   * Constructs a new MobSpawnerSystem.
   *
   * <p>The MobSpawnerSystem requires entities with {@link PositionComponent} and {@link
   * MobSpawnerComponent} components.
   *
   * <p>The MobSpawnerSystem will spawn monsters around mob spawner entities based on their spawn
   * parameters.
   *
   * @see PositionComponent
   * @see MobSpawnerComponent
   */
  public MobSpawnerSystem() {
    super(PositionComponent.class, MobSpawnerComponent.class);
  }

  @Override
  public void execute() {
    this.entityStream().forEach(this::spawnMonsterIfPossible);
  }

  /**
   * Spawns a monster around a mob spawner entity if possible.
   *
   * <p>A monster can be spawned around a mob spawner entity if the following conditions are met:
   *
   * <ul>
   *   <li>The time since the last spawn is greater than the spawn delay of the mob spawner.
   *   <li>The number of monsters around the mob spawner is less than the maximum number of monsters
   *       allowed.
   * </ul>
   *
   * <p>If the conditions are met, a monster will be spawned around the mob spawner entity. The
   * monster will be spawned at a random location within the spawn radius of the mob spawner. The
   * spawn location will be chosen from a list of accessible tiles around the mob spawner entity.
   *
   * @param entity The mob spawner entity.
   */
  private void spawnMonsterIfPossible(Entity entity) {
    PositionComponent position =
        entity
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));
    MobSpawnerComponent mobSpawner =
        entity
            .fetch(MobSpawnerComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, MobSpawnerComponent.class));

    if (this.canSpawnMonster(position.position(), mobSpawner)) {
      this.spawnMonster(position, mobSpawner);
    }
  }

  /**
   * Spawns a monster around a mob spawner entity.
   *
   * @param position The position of the mob spawner entity.
   * @param mobSpawner The mob spawner component of the mob spawner entity.
   * @throws IllegalStateException If no possible spawn locations are found for the mob spawner.
   */
  private void spawnMonster(PositionComponent position, MobSpawnerComponent mobSpawner) {
    List<Tile> possibleSpawns =
        LevelUtils.accessibleTilesInRange(position.position(), mobSpawner.maxSpawnRadius());
    possibleSpawns =
        this.filterTilesWithinMinRadius(
            position.position().toCoordinate(), mobSpawner.minSpawnRadius(), possibleSpawns);

    if (possibleSpawns.isEmpty()) {
      throw new IllegalStateException("No possible spawn locations found for mob spawner");
    }

    Tile spawnTile = possibleSpawns.get(Game.currentLevel().RANDOM.nextInt(possibleSpawns.size()));
    mobSpawner.spawnRandomMonster(spawnTile.coordinate().toCenteredPoint());
    this.lastSpawnTimes.put(mobSpawner, java.lang.System.currentTimeMillis());
  }

  /**
   * Filters the list of possible spawn locations to only include locations that are outside the
   * minimum spawn radius of the mob spawner.
   *
   * @param coords The coordinates of the mob spawner.
   * @param minRadius The minimum spawn radius of the mob spawner.
   * @param possibleSpawns The list of possible spawn locations.
   * @return A list of possible spawn locations that are outside the minimum spawn radius of the mob
   *     spawner.
   */
  private List<Tile> filterTilesWithinMinRadius(
      Coordinate coords, int minRadius, List<Tile> possibleSpawns) {
    return possibleSpawns.stream()
        .filter(tile -> tile.coordinate().distance(coords) > minRadius)
        .toList();
  }

  /**
   * Checks if a monster can be spawned around a mob spawner entity. A monster can be spawned around
   * a mob spawner entity if the following conditions are met: - The time since the last spawn is
   * greater than the spawn delay of the mob spawner. - The number of monsters around the mob
   * spawner is less than the maximum number of monsters allowed.
   *
   * @param position The position of the mob spawner entity.
   * @param mobSpawner The mob spawner component of the mob spawner entity.
   * @return True if a monster can be spawned around the mob spawner entity, false otherwise.
   * @see MobSpawnerComponent
   */
  private boolean canSpawnMonster(Point position, MobSpawnerComponent mobSpawner) {
    if (!this.lastSpawnTimes.containsKey(mobSpawner)) {
      this.lastSpawnTimes.put(mobSpawner, java.lang.System.currentTimeMillis());
    }

    long lastSpawnTime = this.lastSpawnTimes.get(mobSpawner);
    long currentTime = java.lang.System.currentTimeMillis();
    long timeSinceLastSpawn = currentTime - lastSpawnTime;

    if (timeSinceLastSpawn < mobSpawner.spawnDelay()) {
      return false;
    }

    List<Entity> entitiesAround = this.getEntitiesAround(position, mobSpawner.maxSpawnRadius() * 2);
    if (entitiesAround.size() >= mobSpawner.maxMobCount()) {
      this.lastSpawnTimes.put(mobSpawner, java.lang.System.currentTimeMillis());
      return false;
    }

    return true;
  }

  /**
   * Returns a list of entities around a given position within a given radius.
   *
   * @param position The position around which to search for entities.
   * @param radius The radius around the position to search for entities.
   * @return A list of entities around the given position within the given radius.
   */
  private List<Entity> getEntitiesAround(Point position, int radius) {
    return Game.entityStream(Collections.singleton(AIComponent.class)) // mobs
        .filter(
            entity -> {
              PositionComponent entityPosition =
                  entity
                      .fetch(PositionComponent.class)
                      .orElseThrow(
                          () -> MissingComponentException.build(entity, PositionComponent.class));
              return entityPosition.position().distance(position) <= radius;
            })
        .toList();
  }
}
