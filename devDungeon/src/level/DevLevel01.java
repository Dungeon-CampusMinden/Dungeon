package level;

import components.TorchComponent;
import contrib.components.HealthComponent;
import contrib.entities.MiscFactory;
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
import level.utils.ITickable;
import utils.ArrayUtils;

public class DevLevel01 extends DevDungeonLevel implements ITickable {

  private static final int UPPER_RIDDLE_BOUND = 15;
  private static final int LOWER_RIDDLE_BOUND = 5;
  private final Coordinate[] torchPositions;
  private final Coordinate[] riddleRoomTorches;
  private final Coordinate[] riddleRoomBounds;
  private final Coordinate[] riddleRoomContent;
  private final Coordinate[] mobSpawns;
  private final Coordinate levelBossSpawn;
  private final Coordinate[] doorPositions;
  private final int mobCount = 5;
  private final MonsterType[] mobTypes =
      new MonsterType[] {MonsterType.ORC_WARRIOR, MonsterType.ORC_SHAMAN};
  private int searchedSum;
  private Entity riddleSign;

  public DevLevel01(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints);
    // First is riddle door
    this.doorPositions = new Coordinate[] {customPoints.getFirst()};
    // 2, 3 TOP_LEFT, BOTTOM_RIGHT of riddle room
    this.riddleRoomBounds = new Coordinate[] {customPoints.get(1), customPoints.get(2)};
    // Next 6 are torches
    this.torchPositions = customPoints.subList(3, 9).toArray(new Coordinate[0]);
    // Next 6+2 content of riddle room
    this.riddleRoomTorches = customPoints.subList(9, 15).toArray(new Coordinate[0]);
    this.riddleRoomContent = customPoints.subList(15, 17).toArray(new Coordinate[0]);
    // Last entries are mob spawns
    this.mobSpawns = customPoints.subList(17, customPoints.size() - 1).toArray(new Coordinate[0]);
    this.levelBossSpawn = customPoints.getLast();
  }

  @Override
  public void onTick(boolean isFirstTick) {
    if (isFirstTick) {
      this.handleFirstTick();
      this.doorTiles().forEach(DoorTile::close);
    }

    this.riddle();
  }

  private void riddle() {
    int sum = getSumOfTorches();

    if (sum == this.searchedSum) {
      solveRiddle();
    }
  }

  private void solveRiddle() {
    DoorTile door = (DoorTile) tileAt(this.doorPositions[0]);
    door.open();
    this.changeRiddleRoomVisibility(true);
  }

  private int getSumOfTorches() {
    return Game.entityStream()
        .filter(e -> e.isPresent(TorchComponent.class))
        .map(
            e ->
                e.fetch(TorchComponent.class)
                    .orElseThrow(() -> MissingComponentException.build(e, TorchComponent.class)))
        .mapToInt(TorchComponent::value)
        .sum();
  }

  private void handleFirstTick() {
    ((ExitTile) endTile()).close();
    endTile().visible(false);
    this.changeRiddleRoomVisibility(false);
    this.spawnTorches();
    this.spawnMobs(this.mobCount, this.mobTypes);
    this.spawnChestsAndCauldrons();
  }

  /**
   * Returns a random sum of from an array of elements
   *
   * @param numbers the array of elements
   * @return the sum of the random elements
   */
  private int getRandomSumOfNElements(List<Integer> numbers) {
    int amount = RANDOM.nextInt(2, numbers.size());
    List<Integer> randomNumbers =
        ArrayUtils.getRandomElements(numbers.toArray(new Integer[0]), amount);
    return randomNumbers.stream().mapToInt(Integer::intValue).sum();
  }

  private void spawnTorches() {
    this.spawnRiddleRoomTorches();
    this.spawnOutsideTorches();

    // Sign next to riddle door
    this.riddleSign =
        EntityUtils.spawnSign(
            "\n\nAlle Fackeln zusammen sollen\n'" + this.searchedSum + "'ergeben!",
            "Rätsel",
            new Point(this.doorPositions[0].x, this.doorPositions[0].y - 1));
  }

  private void spawnRiddleRoomTorches() {
    for (Coordinate riddleRoomTorch : this.riddleRoomTorches) {
      Point torchPos = new Point(riddleRoomTorch.x + 0.5f, riddleRoomTorch.y + 0.25f);
      EntityUtils.spawnTorch(torchPos, true, false, 0);
    }
  }

  private void spawnOutsideTorches() {
    List<Integer> torchNumbers = new ArrayList<>();
    for (Coordinate torchPosition : this.torchPositions) {
      Point torchPos = new Point(torchPosition.x + 0.5f, torchPosition.y + 0.25f);
      Entity torch =
          EntityUtils.spawnTorch(
              torchPos, false, true, RANDOM.nextInt(LOWER_RIDDLE_BOUND, UPPER_RIDDLE_BOUND));
      int torchNumber =
          torch
              .fetch(TorchComponent.class)
              .orElseThrow(() -> MissingComponentException.build(torch, TorchComponent.class))
              .value();
      EntityUtils.spawnSign(
          "\n\n\n" + torchNumber, "Fackel", new Point(torchPos.x + 1, torchPos.y));
      torchNumbers.add(torchNumber);
    }

    this.searchedSum = getRandomSumOfNElements(torchNumbers);
  }

  /**
   * Spawns mobs in the game level. Selects mobCount - 1 random spawn points from mobSpawns array to
   * spawn a RANDOM monsters. The last mob is always a CHORT. If the CHORT dies, it opens the exit
   *
   * @param mobCount the number of mobs to spawn
   * @param monsterTypes all allowed monster types for this level
   * @throws IllegalArgumentException if mobCount is greater than the length of mobSpawns
   */
  private void spawnMobs(int mobCount, MonsterType[] monsterTypes) {
    if (mobCount > this.mobSpawns.length) {
      throw new IllegalArgumentException("mobCount cannot be greater than mobSpawns.length");
    }

    List<Coordinate> randomSpawns = ArrayUtils.getRandomElements(this.mobSpawns, mobCount - 1);

    for (Coordinate mobPos : randomSpawns) {
      try {
        MonsterType randomType = monsterTypes[(int) (Math.random() * monsterTypes.length)];
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
                        ((ExitTile) endTile()).open();
                        endTile().visible(true);
                      }));
    } catch (RuntimeException e) {
      throw new RuntimeException("Failed to spawn monster: " + e.getMessage());
    }
  }

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

    // TODO: add items to chest
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

  private void changeRiddleRoomVisibility(boolean visible) {
    for (int x = this.riddleRoomBounds[0].x; x <= this.riddleRoomBounds[1].x; x++) {
      for (int y = this.riddleRoomBounds[1].y; y <= this.riddleRoomBounds[0].y; y++) {
        tileAt(new Coordinate(x, y)).visible(visible);
      }
    }
  }
}
