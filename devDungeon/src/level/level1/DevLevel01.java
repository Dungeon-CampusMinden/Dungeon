package level.level1;

import components.TorchComponent;
import contrib.components.HealthComponent;
import contrib.components.InventoryComponent;
import contrib.entities.MiscFactory;
import contrib.item.HealthPotionType;
import contrib.item.concreteItem.ItemPotionHealth;
import contrib.item.concreteItem.ItemResourceBerry;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.elements.tile.DoorTile;
import core.level.elements.tile.ExitTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import entities.EntityUtils;
import entities.MonsterType;
import java.util.*;
import level.DevDungeonLevel;
import level.utils.ITickable;
import level.utils.LevelUtils;
import utils.ArrayUtils;

/** The First Level (Torch Riddle) */
public class DevLevel01 extends DevDungeonLevel implements ITickable {

  // Difficulty (Mob Count, Mob Types)
  private static final int MOB_COUNT = 5;
  private static final MonsterType[] MONSTER_TYPES =
      new MonsterType[] {MonsterType.ORC_WARRIOR, MonsterType.ORC_SHAMAN};

  // Spawn Points / Locations
  private final Coordinate[] torchPositions;
  private final Coordinate[] riddleRoomTorches;
  private final Coordinate[] riddleRoomBounds;
  private final Coordinate[] riddleRoomContent;
  private final Coordinate[] mobSpawns;
  private final Coordinate levelBossSpawn;
  private final DevLevel01Riddle riddleHandler;

  public DevLevel01(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints);
    this.riddleHandler = new DevLevel01Riddle(customPoints, this);

    this.riddleRoomBounds = new Coordinate[] {customPoints.get(1), customPoints.get(2)};
    this.torchPositions = customPoints.subList(3, 9).toArray(new Coordinate[0]);
    this.riddleRoomTorches = customPoints.subList(9, 15).toArray(new Coordinate[0]);
    this.riddleRoomContent = customPoints.subList(15, 17).toArray(new Coordinate[0]);
    this.mobSpawns = customPoints.subList(18, customPoints.size() - 1).toArray(new Coordinate[0]);
    this.levelBossSpawn = customPoints.getLast();
  }

  @Override
  public void onTick(boolean isFirstTick) {
    if (isFirstTick) {
      this.handleFirstTick();
      this.doorTiles().forEach(DoorTile::close);
    }

    this.riddleHandler.onTick(isFirstTick);
  }

  private void handleFirstTick() {
    ((ExitTile) endTile()).close(); // close exit at start (to force defeating the chort)

    // Hide Riddle Room at start
    LevelUtils.ChangeVisibilityForArea(this.riddleRoomBounds[0], this.riddleRoomBounds[1], false);

    // Spawn all entities and it's content
    this.spawnTorches();
    this.spawnMobs(MOB_COUNT, MONSTER_TYPES);
    this.spawnChestsAndCauldrons();
  }

  private void spawnTorches() {
    this.spawnRiddleRoomTorches();
    this.spawnOutsideTorches();
  }

  /**
   * Spawns the torches inside the riddle room. These torches are only for decoration and are not
   * interactable or have any values.
   */
  private void spawnRiddleRoomTorches() {
    for (Coordinate riddleRoomTorch : this.riddleRoomTorches) {
      Point torchPos = new Point(riddleRoomTorch.x + 0.5f, riddleRoomTorch.y + 0.25f);
      EntityUtils.spawnTorch(torchPos, true, false, 0);
    }
  }

  /**
   * Spawns the torches outside the riddle room. These torches have values and are used to solve the
   * riddle. Each torch has a sign next to it, displaying its value. It also updates the search
   * value on the sign next to the door.
   *
   * @see DevLevel01Riddle#setRiddleSolution(List)
   */
  private void spawnOutsideTorches() {
    List<Integer> torchNumbers = new ArrayList<>();
    Coordinate[] positions = this.torchPositions;
    for (int i = 0; i < positions.length; i++) {
      Coordinate torchPosition = positions[i];
      Point torchPos = new Point(torchPosition.x + 0.5f, torchPosition.y + 0.25f);
      Entity torch =
          EntityUtils.spawnTorch(
              torchPos,
              i == 0,
              true,
              RANDOM.nextInt(
                  DevLevel01Riddle.LOWER_RIDDLE_BOUND, DevLevel01Riddle.UPPER_RIDDLE_BOUND));
      int torchNumber =
          torch
              .fetch(TorchComponent.class)
              .orElseThrow(() -> MissingComponentException.build(torch, TorchComponent.class))
              .value();
      EntityUtils.spawnSign("\n\n\n" + torchNumber, "Torch", new Point(torchPos.x + 1, torchPos.y));
      torchNumbers.add(torchNumber);
    }

    this.riddleHandler.setRiddleSolution(torchNumbers);
  }

  // Other Entities

  /**
   * Spawns mobs in the game level. Selects mobCount - 1 random spawn points from mobSpawns array to
   * spawn a RANDOM monsters. The last mob is always a CHORT. If the CHORT dies, it opens the exit
   *
   * @param mobCount the number of mobs to spawn
   * @param monsterTypes all allowed monster types for this level
   * @throws IllegalArgumentException if mobCount is greater than the length of mobSpawns
   * @throws RuntimeException if any of the monsters could not be created
   */
  private void spawnMobs(int mobCount, MonsterType[] monsterTypes) {
    if (mobCount > this.mobSpawns.length) {
      throw new IllegalArgumentException("mobCount cannot be greater than mobSpawns.length");
    }

    List<Coordinate> randomSpawns = ArrayUtils.getRandomElements(this.mobSpawns, mobCount - 1);

    for (Coordinate mobPos : randomSpawns) {
      try {
        MonsterType randomType = monsterTypes[RANDOM.nextInt(monsterTypes.length)];
        EntityUtils.spawnMonster(randomType, mobPos);
      } catch (RuntimeException e) {
        throw new RuntimeException("Failed to spawn monster: " + e.getMessage());
      }
    }

    // Last Mob is stronger
    try {
      Entity chort = EntityUtils.spawnMonster(MonsterType.CHORT, this.levelBossSpawn);
      if (chort == null) {
        throw new RuntimeException();
      }
      chort
          .fetch(HealthComponent.class)
          .ifPresent(
              hc ->
                  hc.onDeath(
                      (e) -> {
                        ((ExitTile) endTile()).open(); // open exit when chort dies
                      }));
    } catch (RuntimeException e) {
      throw new RuntimeException("Failed to spawn monster: " + e.getMessage());
    }
  }

  /**
   * Spawns the chests and cauldrons in the riddle room.
   *
   * @throws RuntimeException if any of the entities could not be created
   */
  private void spawnChestsAndCauldrons() {
    // Chest
    Entity chest;
    try {
      chest = MiscFactory.newChest(MiscFactory.FILL_CHEST.EMPTY);
    } catch (Exception e) {
      throw new RuntimeException("Failed to create chest");
    }
    PositionComponent pc =
        chest
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(chest, PositionComponent.class));

    pc.position(
        new Point(
            this.riddleRoomContent[0].toPoint().x + 0.5f,
            this.riddleRoomContent[0].toPoint().y + 0.5f));

    InventoryComponent ic =
        chest
            .fetch(InventoryComponent.class)
            .orElseThrow(() -> MissingComponentException.build(chest, InventoryComponent.class));
    ic.add(new ItemResourceBerry());
    ic.add(new ItemPotionHealth(HealthPotionType.WEAK));

    Game.add(chest);

    // Cauldron
    Entity cauldron;
    try {
      cauldron = MiscFactory.newCraftingCauldron();
    } catch (Exception e) {
      throw new RuntimeException("Failed to create cauldron");
    }
    pc =
        cauldron
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(cauldron, PositionComponent.class));
    pc.position(
        new Point(
            this.riddleRoomContent[1].toPoint().x + 0.5f,
            this.riddleRoomContent[1].toPoint().y + 0.5f));
    Game.add(cauldron);
  }
}
