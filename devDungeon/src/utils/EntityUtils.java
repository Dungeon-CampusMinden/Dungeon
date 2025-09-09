package utils;

import contrib.components.HealthComponent;
import contrib.entities.MonsterBuilder;
import core.Entity;
import core.Game;
import core.level.elements.ILevel;
import core.level.elements.tile.ExitTile;
import core.level.utils.Coordinate;
import core.utils.IVoidFunction;
import core.utils.Point;
import entities.DevDungeonMonster;
import entities.TorchFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Logger;
import level.devlevel.TorchRiddleLevel;
import task.tasktype.Quiz;

/**
 * EntityUtils is a utility class that provides methods for spawning entities in the game. It
 * contains methods for spawning monsters, torches, and other entities.
 *
 * @see contrib.utils.EntityUtils
 */
public class EntityUtils {

  private static final Logger LOGGER = Logger.getLogger(EntityUtils.class.getName());

  /**
   * Spawns a bridge guard at the given position and adds it to the game. The bridge guard neutral
   * NPC that gives the player a series of {@link task.tasktype.Quiz quizzes} to solve. The bridge
   * guard is created using the {@link DevDungeonMonster#BRIDGE_GUARD BRIDGE_GUARD} monster type.
   * The bridge guard is then added to the game.
   *
   * @param pos The position where the bridge guard should be spawned.
   * @param quizzes The list of quizzes to give the player.
   * @param onFinished The action to perform when all quizzes have been solved.
   * @return The spawned bridge guard entity.
   * @see DevDungeonMonster#createBridgeGuard(Point, List, IVoidFunction) createBridgeGuard
   */
  public static Entity spawnBridgeGuard(Point pos, List<Quiz> quizzes, IVoidFunction onFinished) {
    Entity bridgeGuard = DevDungeonMonster.createBridgeGuard(pos, quizzes, onFinished);
    Game.add(bridgeGuard);
    return bridgeGuard;
  }

  /**
   * Spawns a torch at the given coordinate and adds it to the game. The torch is created using the
   * TorchFactory class and is then added to the game.
   *
   * @param torchPos The pos where the torch should be spawned.
   * @param lit The initial state of the torch. True if the torch should be lit, false otherwise.
   * @param isInteractable True if the torch should be interactable, false otherwise.
   * @param value The value of the torch. (Used for {@link TorchRiddleLevel Level 1}).
   * @return The spawned torch entity.
   */
  public static Entity spawnTorch(Point torchPos, boolean lit, boolean isInteractable, int value) {
    return spawnTorch(torchPos, lit, isInteractable, (e, e2) -> {}, value);
  }

  /**
   * Spawns a torch at the given coordinate and adds it to the game. The torch is created using the
   * TorchFactory class and is then added to the game.
   *
   * @param torchPos The pos where the torch should be spawned.
   * @param lit The initial state of the torch. True if the torch should be lit, false otherwise.
   * @param isInteractable True if the torch should be interactable, false otherwise.
   * @param onToggle The action to perform when the torch is toggled. (torch, whoTriggered)
   * @param value The value of the torch. (Used for {@link TorchRiddleLevel Level 1}).
   * @return The spawned torch entity.
   */
  public static Entity spawnTorch(
      Point torchPos,
      boolean lit,
      boolean isInteractable,
      BiConsumer<Entity, Entity> onToggle,
      int value) {
    Entity torch = TorchFactory.createTorch(torchPos, lit, isInteractable, onToggle, value);
    Game.add(torch);
    return torch;
  }

  /**
   * Spawns a anit light torch at the given coordinate and adds it to the game. The torch is created
   * using the TorchFactory class and is then added to the game.
   *
   * @param torchPos The pos where the torch should be spawned.
   * @param lit The initial state of the torch. True if the torch should be lit, false otherwise.
   * @param isInteractable True if the torch should be interactable, false otherwise.
   * @param onToggle The action to perform when the torch is toggled. (torch, whoTriggered)
   * @return The spawned torch entity.
   */
  public static Entity spawnAntiLightTorch(
      Point torchPos, boolean lit, boolean isInteractable, BiConsumer<Entity, Entity> onToggle) {
    Entity torch = TorchFactory.createAntiTorch(torchPos, lit, isInteractable, onToggle, 0);
    Game.add(torch);
    return torch;
  }

  /**
   * Spawns a mob spawner at the given position and adds it to the game. The mob spawner is created
   * using the {@link entities.MobSpawnerFactory MobSpawnerFactory} class and is then added to the
   * game.
   *
   * @param pos The position where the mob spawner should be spawned.
   * @param monsterTypes An array of MonsterType that the mob spawner can spawn.
   * @param maxMobCount The maximum number of mobs that the mob spawner can spawn.
   * @return The spawned mob spawner entity.
   */
  public static Entity spawnMobSpawner(
      Coordinate pos, MonsterBuilder<?>[] monsterTypes, int maxMobCount) {
    Entity mobSpawner = entities.MobSpawnerFactory.createMobSpawner(pos, monsterTypes, maxMobCount);
    Game.add(mobSpawner);
    return mobSpawner;
  }

  /**
   * This method is used to spawn a specified number of monsters in the game at random positions.
   * The monsters are spawned at random positions from the mobSpawns array.
   *
   * @param mobCount The number of monsters to be spawned. This number cannot be greater than the
   *     length of mobSpawns.
   * @param monsterTypes An array of MonsterType that the monsters can be. A random type is chosen
   *     for each monster.
   * @param mobSpawns An array of Coordinates where the monsters can be spawned. Random coordinates
   *     are chosen from this array.
   * @return A list of the spawned entities. The last entity in the list is the level boss monster.
   * @throws IllegalArgumentException if mobCount is greater than the length of mobSpawns.
   * @throws RuntimeException if an error occurs while spawning a monster.
   * @see #spawnBoss(DevDungeonMonster, Coordinate)
   */
  public static List<Entity> spawnMobs(
      int mobCount, DevDungeonMonster[] monsterTypes, Coordinate[] mobSpawns) {
    if (mobCount > mobSpawns.length) {
      throw new IllegalArgumentException("mobCount cannot be greater than mobSpawns.length");
    }

    // Gets a list of random spawn points from the mobSpawns array.
    List<Coordinate> randomSpawns = ArrayUtils.getRandomElements(mobSpawns, mobCount - 1);
    List<Entity> spawnedMobs = new ArrayList<>();
    // Spawns the monsters at the random spawn points.
    for (Coordinate mobPos : randomSpawns) {
      // Choose a random monster type from the monsterTypes array.
      DevDungeonMonster randomType = monsterTypes[ILevel.RANDOM.nextInt(monsterTypes.length)];
      spawnedMobs.add(randomType.builder().addToGame().build(mobPos));
    }

    return spawnedMobs;
  }

  /**
   * This method is used to spawn a boss monster in the game at a specified position. When the boss
   * monster dies, the exit of the current level is opened and the onBossDeath action is performed.
   *
   * @param bossType The type of the level boss monster.
   * @param levelBossSpawn The Coordinate where the bossType monster (level boss) is to be spawned.
   * @param onBossDeath The action to perform when the level boss monster dies.
   * @return The spawned boss monster entity.
   * @throws RuntimeException if an error occurs while spawning a monster.
   */
  public static Entity spawnBoss(
      DevDungeonMonster bossType, Coordinate levelBossSpawn, Consumer<Entity> onBossDeath) {
    try {
      Entity bossMob = bossType.builder().addToGame().build(levelBossSpawn);
      if (bossMob == null) {
        throw new RuntimeException("Failed to spawn level boss monster");
      }
      // When the level boss monster dies, open the exit of the current level.
      bossMob
          .fetch(HealthComponent.class)
          .ifPresent(
              hc ->
                  hc.onDeath(
                      (e) -> {
                        Game.endTiles().forEach(ExitTile::open); // open exit when chort dies
                        onBossDeath.accept(e);
                        Game.remove(e);
                      }));
      return bossMob;
    } catch (RuntimeException e) {
      throw new RuntimeException("Failed to spawn level boss monster: " + e.getMessage());
    }
  }

  /**
   * This method is used to spawn a boss monster in the game at a specified position. When the boss
   * monster dies, the exit of the current level is opened.
   *
   * @param bossType The type of the level boss monster.
   * @param levelBossSpawn The Coordinate where the bossType monster (level boss) is to be spawned.
   * @return The spawned boss monster entity.
   * @throws RuntimeException if an error occurs while spawning a monster.
   */
  public static Entity spawnBoss(DevDungeonMonster bossType, Coordinate levelBossSpawn) {
    return spawnBoss(bossType, levelBossSpawn, (e) -> {});
  }
}
