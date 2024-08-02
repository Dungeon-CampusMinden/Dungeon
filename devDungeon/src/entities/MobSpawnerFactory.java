package entities;

import components.MobSpawnerComponent;
import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.utils.Coordinate;
import core.utils.components.draw.Animation;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;

/**
 * A factory class for creating mob spawner entities.
 *
 * <p>A mob spawner is an entity that can spawn monsters around it. The mob spawner has a set of
 * monster types that it can spawn, a maximum number of monsters that it can spawn, and a set of
 * spawn parameters (e.g., spawn delay, spawn radius).
 *
 * <p>The mob spawner entity is created with a position and a set of monster types that it can
 * spawn. The mob spawner will spawn monsters around it based on its spawn parameters.
 *
 * <p>The mob spawner entity has a {@link MobSpawnerComponent} that stores the spawn parameters of
 * the mob spawner.
 */
public class MobSpawnerFactory {

  private static final int MIN_SPAWN_DELAY = 5000;
  private static final int MAX_SPAWN_DELAY = 20000;
  private static final int MIN_SPAWN_RADIUS = 0;
  private static final int MAX_SPAWN_RADIUS = 5;

  private static final IPath SPAWNER_TEXTURE = new SimpleIPath("objects/spawner/tent_0.png");

  /**
   * Creates a mob spawner entity at a given position.
   *
   * @param pos The position where the mob spawner will be created.
   * @param monsterTypes The types of monsters that the mob spawner can spawn.
   * @param maxMobCount The maximum number of monsters allowed around the mob spawner. (The mob
   *     spawner will not spawn more monsters if this number is reached.)
   * @return The created mob spawner entity.
   */
  public static Entity createMobSpawner(
      Coordinate pos, MonsterType[] monsterTypes, int maxMobCount) {
    Entity mobSpawner = new Entity("mobSpawner");

    mobSpawner.add(new PositionComponent(pos.toCenteredPoint()));
    mobSpawner.add(new DrawComponent(Animation.fromSingleImage(SPAWNER_TEXTURE)));
    mobSpawner.add(
        new MobSpawnerComponent(
            monsterTypes,
            maxMobCount,
            MIN_SPAWN_DELAY,
            MAX_SPAWN_DELAY,
            MIN_SPAWN_RADIUS,
            MAX_SPAWN_RADIUS));

    return mobSpawner;
  }
}
