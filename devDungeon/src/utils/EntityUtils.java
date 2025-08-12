package utils;

import contrib.components.HealthComponent;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.elements.ILevel;
import core.level.elements.tile.ExitTile;
import core.level.utils.Coordinate;
import core.utils.IVoidFunction;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import entities.MonsterType;
import entities.TorchFactory;
import java.io.IOException;
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
   * Spawns a monster of the given type at the given position and adds it to the game. The Position
   * is cast to a Tile and the monster is spawned at the center of the tile.
   *
   * @param monsterType the type of monster to spawn
   * @param position the position to spawn the monster; the tile at the given point must be
   *     accessible else the monster will not be spawned
   * @return the spawned monster
   * @throws MissingComponentException if the monster does not have a PositionComponent
   * @throws RuntimeException if an error occurs while spawning the monster
   * @see Game#add(Entity)
   * @see MonsterType
   */
  public static Entity spawnMonster(MonsterType monsterType, Point position) {
    Tile tile = Game.tileAT(position).orElse(null);
    if (tile == null || !tile.isAccessible()) {
      LOGGER.warning(
          "Cannot spawn monster at "
              + position
              + " because the tile is not accessible or does not exist");
      return null;
    }
    return spawnMonster(monsterType, tile.coordinate());
  }

  /**
   * Spawns a monster of the given type at the given coordinate and adds it to the game.
   *
   * @param monsterType the type of monster to spawn
   * @param coordinate the coordinate to spawn the monster; the tile at the given coordinate must be
   *     accessible else the monster will not be spawned
   * @return the spawned monster
   * @throws MissingComponentException if the monster does not have a PositionComponent
   * @throws RuntimeException if an error occurs while spawning the monster
   * @see Game#add(Entity)
   * @see MonsterType
   */
  public static Entity spawnMonster(MonsterType monsterType, Coordinate coordinate) {
    Tile tile = Game.tileAT(coordinate).orElse(null);
    if (tile == null || !tile.isAccessible()) {
      LOGGER.warning(
          "Cannot spawn monster at "
              + coordinate
              + " because the tile is not accessible or does not exist");
      return null;
    }
    try {
      Entity newMob = monsterType.buildMonster();
      PositionComponent positionComponent =
          newMob
              .fetch(PositionComponent.class)
              .orElseThrow(() -> MissingComponentException.build(newMob, PositionComponent.class));
      positionComponent.position(tile.coordinate().toCenteredPoint());
      Game.add(newMob);
      return newMob;
    } catch (IOException e) {
      throw new RuntimeException("Error spawning monster", e);
    }
  }

  /**
   * Spawns a bridge guard at the given position and adds it to the game. The bridge guard neutral
   * NPC that gives the player a series of {@link task.tasktype.Quiz quizzes} to solve. The bridge
   * guard is created using the {@link entities.MonsterType#BRIDGE_GUARD BRIDGE_GUARD} monster type.
   * The bridge guard is then added to the game.
   *
   * @param pos The position where the bridge guard should be spawned.
   * @param quizzes The list of quizzes to give the player.
   * @param onFinished The action to perform when all quizzes have been solved.
   * @return The spawned bridge guard entity.
   * @see MonsterType#createBridgeGuard(Point, List, IVoidFunction) createBridgeGuard
   */
  public static Entity spawnBridgeGuard(Point pos, List<Quiz> quizzes, IVoidFunction onFinished) {
    Entity bridgeGuard = MonsterType.createBridgeGuard(pos, quizzes, onFinished);
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
      Coordinate pos, MonsterType[] monsterTypes, int maxMobCount) {
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
   * @see #spawnBoss(MonsterType, Coordinate, Consumer) spawnBoss
   */
  public static List<Entity> spawnMobs(
      int mobCount, MonsterType[] monsterTypes, Coordinate[] mobSpawns) {
    if (mobCount > mobSpawns.length) {
      throw new IllegalArgumentException("mobCount cannot be greater than mobSpawns.length");
    }

    // Gets a list of random spawn points from the mobSpawns array.
    List<Coordinate> randomSpawns = ArrayUtils.getRandomElements(mobSpawns, mobCount - 1);
    List<Entity> spawnedMobs = new ArrayList<>();
    // Spawns the monsters at the random spawn points.
    for (Coordinate mobPos : randomSpawns) {
      try {
        // Choose a random monster type from the monsterTypes array.
        MonsterType randomType = monsterTypes[ILevel.RANDOM.nextInt(monsterTypes.length)];
        // Spawn the monster at the current spawn point.
        spawnedMobs.add(EntityUtils.spawnMonster(randomType, mobPos));
      } catch (RuntimeException e) {
        throw new RuntimeException("Failed to spawn monster: " + e.getMessage());
      }
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
      MonsterType bossType, Coordinate levelBossSpawn, Consumer<Entity> onBossDeath) {
    try {
      Entity bossMob = EntityUtils.spawnMonster(bossType, levelBossSpawn);
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
                        ((ExitTile) Game.currentLevel().endTile().orElseThrow())
                            .open(); // open exit when chort dies
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
  public static Entity spawnBoss(MonsterType bossType, Coordinate levelBossSpawn) {
    return spawnBoss(bossType, levelBossSpawn, (e) -> {});
  }
}
