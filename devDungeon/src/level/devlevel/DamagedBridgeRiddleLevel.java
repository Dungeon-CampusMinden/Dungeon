package level.devlevel;

import contrib.components.InventoryComponent;
import contrib.entities.DialogFactory;
import contrib.entities.MiscFactory;
import contrib.item.HealthPotionType;
import contrib.item.concreteItem.ItemPotionHealth;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.elements.tile.DoorTile;
import core.level.elements.tile.ExitTile;
import core.level.elements.tile.PitTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.components.MissingComponentException;
import entities.MonsterType;
import java.util.*;
import level.DevDungeonLevel;
import level.devlevel.riddleHandler.DamagedBridgeRiddleHandler;
import level.utils.ITickable;
import starter.DevDungeon;
import utils.EntityUtils;

/** The Damaged Bridge Riddle Level */
public class DamagedBridgeRiddleLevel extends DevDungeonLevel implements ITickable {

  // Difficulty (Mob Count, Mob Types)
  private static final int MOB_COUNT = 7;
  private static final MonsterType[] MONSTER_TYPES =
      new MonsterType[] {MonsterType.ORC_WARRIOR, MonsterType.ORC_SHAMAN};
  private static final MonsterType BOSS_TYPE = MonsterType.CHORT;

  // Spawn Points / Locations
  private final Coordinate bridgeMobSpawn;
  private final Tile[] secretWay;
  private final Coordinate[] mobSpawns;
  private final Coordinate levelBossSpawn;

  private final DamagedBridgeRiddleHandler riddleHandler;

  public DamagedBridgeRiddleLevel(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints);
    this.riddleHandler = new DamagedBridgeRiddleHandler(customPoints, this);
    this.bridgeMobSpawn = customPoints.get(8);

    this.secretWay =
        Arrays.stream(this.getCoordinates(11, 17)).map(this::tileAt).toArray(Tile[]::new);
    this.mobSpawns = this.getCoordinates(18, this.customPoints().size() - 2);
    this.levelBossSpawn = this.customPoints().getLast();
  }

  @Override
  public void onTick(boolean isFirstTick) {
    if (isFirstTick) {
      DialogFactory.showTextPopup(
          "I heard that a powerful artifact is hidden nearby. Rumor says it's just beyond an old bridge. Let's see if we can find it.",
          "Level " + DevDungeon.DUNGEON_LOADER.currentLevelIndex() + ": The Damaged Bridge");
      ((ExitTile) this.endTile()).close(); // close exit at start (to force defeating the boss)
      this.doorTiles().forEach(DoorTile::close);
      this.pitTiles()
          .forEach(
              pit -> {
                pit.timeToOpen(50L * Game.currentLevel().RANDOM.nextInt(1, 5));
                pit.close();
              });
      this.handleFirstTick();
    }

    this.riddleHandler.onTick(isFirstTick);
  }

  private void handleFirstTick() {

    this.prepareBridge();

    // Prepare the secret way
    for (int i = 0; i < this.secretWay.length - 1; i++) {
      PitTile pitTile = (PitTile) this.secretWay[i];
      pitTile.timeToOpen(15 * 1000);
    }

    // Spawn all entities and it's content
    this.spawnChestsAndCauldrons();

    EntityUtils.spawnMobs(MOB_COUNT, MONSTER_TYPES, this.mobSpawns);
    EntityUtils.spawnBoss(BOSS_TYPE, this.levelBossSpawn);
  }

  private void prepareBridge() {
    EntityUtils.spawnMonster(MonsterType.BRIDGE_MOB, this.bridgeMobSpawn);
    List<PitTile> bridge =
        this.pitTiles().stream()
            .filter(pit -> pit.coordinate().y == this.bridgeMobSpawn.y)
            .toList();
    int timeToOpen = 500;
    for (PitTile pitTile : bridge) {
      pitTile.timeToOpen(timeToOpen);
      if (timeToOpen >= 300) { // force after 3 pits to be 50
        timeToOpen -= 100;
      } else {
        timeToOpen = 50;
      }
    }
  }

  /**
   * Spawns the chests and cauldrons in the riddle room.
   *
   * @throws RuntimeException if any of the entities could not be created
   */
  private void spawnChestsAndCauldrons() {
    Entity chest;
    try {
      chest = MiscFactory.newChest(MiscFactory.FILL_CHEST.EMPTY);
    } catch (Exception e) {
      throw new RuntimeException("Failed to create speed potion chest");
    }
    PositionComponent pc =
        chest
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(chest, PositionComponent.class));
    pc.position(this.secretWay[this.secretWay.length - 1].coordinate().toCenteredPoint());
    InventoryComponent ic =
        chest
            .fetch(InventoryComponent.class)
            .orElseThrow(() -> MissingComponentException.build(chest, InventoryComponent.class));
    ic.add(new ItemPotionHealth(HealthPotionType.GREATER));

    Game.add(chest);
  }
}
