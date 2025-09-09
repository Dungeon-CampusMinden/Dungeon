package level.devlevel;

import components.TorchComponent;
import contrib.components.InventoryComponent;
import contrib.entities.MiscFactory;
import contrib.item.HealthPotionType;
import contrib.item.concreteItem.ItemPotionHealth;
import contrib.utils.EntityUtils;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.level.utils.LevelUtils;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.MissingComponentException;
import entities.DevDungeonMonster;
import item.concreteItem.ItemResourceBerry;
import java.util.*;
import level.DevDungeonLevel;
import level.devlevel.riddleHandler.TorchRiddleRiddleHandler;

/** The Torch Riddle Level. */
public class TorchRiddleLevel extends DevDungeonLevel {

  // Difficulty (Mob Count, Mob Types)
  private static final int MOB_COUNT = 12;
  private static final DevDungeonMonster[] MONSTER_TYPES =
      new DevDungeonMonster[] {DevDungeonMonster.ORC_WARRIOR, DevDungeonMonster.ORC_SHAMAN};
  private static final DevDungeonMonster BOSS_TYPE = DevDungeonMonster.ZOMBIE;

  // Spawn Points / Locations
  private final Coordinate[] torchPositions;
  private final Coordinate[] riddleRoomTorches;
  private final Coordinate[] riddleRoomBounds;
  private final Coordinate[] riddleRoomContent;
  private final Coordinate[] mobSpawns;
  private final Coordinate levelBossSpawn;
  private final TorchRiddleRiddleHandler riddleHandler;

  /**
   * Constructs the Torch Riddle Level.
   *
   * @param layout The layout of the level.
   * @param designLabel The design label of the level.
   * @param customPoints The custom points of the level.
   */
  public TorchRiddleLevel(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(
        layout,
        designLabel,
        customPoints,
        "The Torch Riddle",
        "Welcome to the Torch Riddle! This is an ancient test, rewarding rewards to those who look closer. You may find the Riddle Door to proceed. Best of luck!");
    this.riddleHandler = new TorchRiddleRiddleHandler(customPoints, this);

    this.riddleRoomBounds = new Coordinate[] {customPoints.get(1), customPoints.get(2)};
    this.torchPositions = getCoordinates(3, 8);
    this.riddleRoomTorches = getCoordinates(9, 14);
    this.riddleRoomContent = getCoordinates(15, 16);
    this.mobSpawns = getCoordinates(18, customPoints.size() - 2);
    this.levelBossSpawn = customPoints.getLast();
  }

  @Override
  protected void onFirstTick() {
    handleFirstTick();
    riddleHandler.onFirstTick();
  }

  @Override
  protected void onTick() {
    riddleHandler.onTick();
  }

  private void handleFirstTick() {
    // Hide Riddle Room at start
    LevelUtils.changeVisibilityForArea(riddleRoomBounds[0], riddleRoomBounds[1], false);

    // Spawn all entities and it's content
    spawnTorches();
    utils.EntityUtils.spawnMobs(MOB_COUNT, MONSTER_TYPES, mobSpawns);
    utils.EntityUtils.spawnBoss(BOSS_TYPE, levelBossSpawn);
    spawnChestsAndCauldrons();
  }

  private void spawnTorches() {
    spawnRiddleRoomTorches();
    spawnOutsideTorches();
  }

  /**
   * Spawns the torches inside the riddle room. These torches are only for decoration and are not
   * interactable or have any values.
   */
  private void spawnRiddleRoomTorches() {
    for (Coordinate riddleRoomTorch : riddleRoomTorches) {
      Point torchPos = riddleRoomTorch.toPoint().translate(Vector2.of(0, 0.25f));
      utils.EntityUtils.spawnTorch(torchPos, true, false, 0);
    }
  }

  /**
   * Spawns the torches outside the riddle room. These torches have values and are used to solve the
   * riddle. Each torch has a sign next to it, displaying its value. It also updates the search
   * value on the sign next to the door.
   *
   * @see TorchRiddleRiddleHandler#setRiddleSolution(List)
   */
  private void spawnOutsideTorches() {
    List<Integer> torchNumbers = new ArrayList<>();
    Coordinate[] positions = torchPositions;
    for (int i = 0; i < positions.length; i++) {
      Coordinate torchPosition = positions[i];
      Point torchPos = torchPosition.toPoint();
      Entity torch =
          utils.EntityUtils.spawnTorch(
              torchPos,
              i == 0,
              true,
              RANDOM.nextInt(
                  TorchRiddleRiddleHandler.LOWER_RIDDLE_BOUND,
                  TorchRiddleRiddleHandler.UPPER_RIDDLE_BOUND));
      int torchNumber =
          torch
              .fetch(TorchComponent.class)
              .orElseThrow(() -> MissingComponentException.build(torch, TorchComponent.class))
              .value();
      EntityUtils.spawnSign(
          "\n\n\n" + torchNumber, "Torch", new Point(torchPos.x() + 1, torchPos.y()));
      torchNumbers.add(torchNumber);
    }

    riddleHandler.setRiddleSolution(torchNumbers);
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

    pc.position(riddleRoomContent[0].toPoint());

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
    pc.position(riddleRoomContent[1].toPoint());
    Game.add(cauldron);
  }
}
