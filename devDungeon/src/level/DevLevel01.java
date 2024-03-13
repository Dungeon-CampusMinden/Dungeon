package level;

import components.SignComponent;
import components.TorchComponent;
import contrib.components.HealthComponent;
import contrib.components.InventoryComponent;
import contrib.entities.MiscFactory;
import contrib.item.HealthPotionType;
import contrib.item.concreteItem.ItemPotionHealth;
import contrib.item.concreteItem.ItemResourceBerry;
import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;
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
import entities.SignFactory;
import java.util.*;
import level.utils.ITickable;
import level.utils.LevelUtils;
import utils.ArrayUtils;

/** The First Level (Torch Riddle) */
public class DevLevel01 extends DevDungeonLevel implements ITickable {

  // Riddle constants
  private static final int UPPER_RIDDLE_BOUND = 15;
  private static final int LOWER_RIDDLE_BOUND = 5;
  private static final int RIDDLE_REWARD = 5;

  // Spawn Points / Locations
  private final Coordinate[] torchPositions;
  private final Coordinate[] riddleRoomTorches;
  private final Coordinate[] riddleRoomBounds;
  private final Coordinate riddleCenter;
  private final Coordinate[] riddleRoomContent;
  private final Coordinate[] mobSpawns;
  private final Coordinate levelBossSpawn;
  private final Coordinate[] doorPositions;
  // Difficulty (Mob Count, Mob Types)
  private final int mobCount = 5;
  private final MonsterType[] mobTypes =
      new MonsterType[] {MonsterType.ORC_WARRIOR, MonsterType.ORC_SHAMAN};

  // Riddle fields
  private final Entity riddleSign;
  private int riddleSearchedSum;
  private boolean rewardGiven = false;

  public DevLevel01(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints);
    //// Spawns
    // First is riddle door
    this.doorPositions = new Coordinate[] {customPoints.getFirst()};
    // 2, 3 TOP_LEFT, BOTTOM_RIGHT of riddle room
    this.riddleRoomBounds = new Coordinate[] {customPoints.get(1), customPoints.get(2)};
    // Next 6 are torches
    this.torchPositions = customPoints.subList(3, 9).toArray(new Coordinate[0]);
    // Next 6+2 content of riddle room
    this.riddleRoomTorches = customPoints.subList(9, 15).toArray(new Coordinate[0]);
    this.riddleRoomContent = customPoints.subList(15, 17).toArray(new Coordinate[0]);
    // Next one is the center of the torch circle
    this.riddleCenter = customPoints.get(17);
    // Last entries are mob spawns
    this.mobSpawns = customPoints.subList(18, customPoints.size() - 1).toArray(new Coordinate[0]);
    this.levelBossSpawn = customPoints.getLast();

    //// Sign next to Riddle Door
    this.riddleSign =
        SignFactory.createSign(
            "",
            "Rätsel",
            new Point(this.doorPositions[0].x - 1 + 0.5f, this.doorPositions[0].y - 1 + 0.5f),
            (e1, e2) ->
                updateRiddleSign(
                    getSumOfLitTorches())); // Updates content based on random riddle values
  }

  @Override
  public void onTick(boolean isFirstTick) {
    if (isFirstTick) {
      this.handleFirstTick();
      this.doorTiles().forEach(DoorTile::close);
    }

    this.riddle();
  }

  private void handleFirstTick() {
    ((ExitTile) endTile()).close();
    endTile().visible(false);
    Game.add(this.riddleSign);
    LevelUtils.ChangeVisibilityForArea(this.riddleRoomBounds[0], this.riddleRoomBounds[1], false);
    this.spawnTorches();
    this.spawnMobs(this.mobCount, this.mobTypes);
    this.spawnChestsAndCauldrons();
    updateRiddleSign(getSumOfLitTorches());
  }

  // Torch Methods

  private void spawnTorches() {
    this.spawnRiddleRoomTorches();
    this.spawnOutsideTorches();
  }

  private void spawnRiddleRoomTorches() {
    for (Coordinate riddleRoomTorch : this.riddleRoomTorches) {
      Point torchPos = new Point(riddleRoomTorch.x + 0.5f, riddleRoomTorch.y + 0.25f);
      EntityUtils.spawnTorch(torchPos, true, false, 0);
    }
  }

  private void spawnOutsideTorches() {
    List<Integer> torchNumbers = new ArrayList<>();
    Coordinate[] positions = this.torchPositions;
    for (int i = 0; i < positions.length; i++) {
      Coordinate torchPosition = positions[i];
      Point torchPos = new Point(torchPosition.x + 0.5f, torchPosition.y + 0.25f);
      Entity torch =
          EntityUtils.spawnTorch(
              torchPos, i == 0, true, RANDOM.nextInt(LOWER_RIDDLE_BOUND, UPPER_RIDDLE_BOUND));
      int torchNumber =
          torch
              .fetch(TorchComponent.class)
              .orElseThrow(() -> MissingComponentException.build(torch, TorchComponent.class))
              .value();
      EntityUtils.spawnSign(
          "\n\n\n" + torchNumber, "Fackel", new Point(torchPos.x + 1, torchPos.y));
      torchNumbers.add(torchNumber);
    }

    this.riddleSearchedSum = getRandomSumOfNElements(torchNumbers);
  }

  // Other Entities

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

  // Riddle relevant logic

  /**
   * Checks if the sum of the values of all lit torches equals the searched sum. If it does, the
   * riddle is solved.
   */
  private void riddle() {
    int sum = getSumOfLitTorches();

    if (sum == this.riddleSearchedSum) {
      solveRiddle();
      if (!this.rewardGiven && checkIfHeroIsInCenter()) {
        giveReward();
      }
    }
  }

  /**
   * Gives the reward to the hero if the riddle is solved. A popup message is displayed to inform
   * the hero about the reward. The reward is only given once, controlled by the rewardGiven flag.
   */
  private void giveReward() {
    SignFactory.showTextPopup(
        "Als Belohnung erhälst du dauerhaft " + RIDDLE_REWARD + " Lebenpunkte mehr!",
        "Rätsel gelöst!");
    Game.hero()
        .flatMap(hero -> hero.fetch(HealthComponent.class))
        .ifPresent(
            hc -> {
              hc.maximalHealthpoints(hc.maximalHealthpoints() + RIDDLE_REWARD);
              hc.receiveHit(new Damage(-RIDDLE_REWARD, DamageType.HEAL, null));
              this.rewardGiven = true;
            });

    if (this.rewardGiven) {
      // Once the reward is given, all torches are extinguished
      Game.entityStream()
          .filter(e -> e.isPresent(TorchComponent.class))
          .forEach(e -> e.fetch(TorchComponent.class).ifPresent(tc -> tc.lit(false)));
    }
  }

  /**
   * Checks if the hero is in the center of the riddle room.
   *
   * @return true if the hero is in the center of the riddle room, false otherwise.
   */
  private boolean checkIfHeroIsInCenter() {
    Optional<Entity> hero = Game.hero();
    return hero.filter(entity -> tileAtEntity(entity).equals(tileAt(this.riddleCenter)))
        .isPresent();
  }

  /**
   * Solves the riddle by opening the door and making the riddle room visible.
   *
   * @see LevelUtils#ChangeVisibilityForArea(Coordinate, Coordinate, boolean)
   */
  private void solveRiddle() {
    DoorTile door = (DoorTile) tileAt(this.doorPositions[0]);
    door.open();
    LevelUtils.ChangeVisibilityForArea(this.riddleRoomBounds[0], this.riddleRoomBounds[1], true);
  }

  /**
   * Calculates the sum of the values of all lit torches in the game.
   *
   * @return The sum of the values of all lit torches.
   */
  private int getSumOfLitTorches() {
    return Game.entityStream()
        .filter(e -> e.isPresent(TorchComponent.class) && e.fetch(TorchComponent.class).get().lit())
        .map(
            e ->
                e.fetch(TorchComponent.class)
                    .orElseThrow(() -> MissingComponentException.build(e, TorchComponent.class)))
        .mapToInt(TorchComponent::value)
        .sum();
  }

  /**
   * Updates the text of the riddle sign to display the current sum of the values of all lit
   * torches.
   *
   * @param currentSum The current sum of the values of all lit torches.
   */
  public void updateRiddleSign(int currentSum) {
    this.riddleSign
        .fetch(SignComponent.class)
        .orElseThrow(() -> MissingComponentException.build(this.riddleSign, SignComponent.class))
        .text(
            "\n\nAlle Fackeln zusammen sollen\n'"
                + this.riddleSearchedSum
                + "'ergeben! Du hast: '"
                + currentSum
                + "'");
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
}
