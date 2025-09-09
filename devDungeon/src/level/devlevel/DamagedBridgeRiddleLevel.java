package level.devlevel;

import contrib.components.InventoryComponent;
import contrib.entities.MiscFactory;
import contrib.item.HealthPotionType;
import contrib.item.concreteItem.ItemPotionHealth;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.elements.tile.PitTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.components.MissingComponentException;
import entities.DevDungeonMonster;
import java.util.*;
import level.DevDungeonLevel;
import level.devlevel.riddleHandler.DamagedBridgeRiddleHandler;
import utils.EntityUtils;

/** The Damaged Bridge Riddle Level. */
public class DamagedBridgeRiddleLevel extends DevDungeonLevel {

  // Difficulty (Mob Count, Mob Types)
  private static final int MOB_COUNT = 7;
  private static final DevDungeonMonster[] MONSTER_TYPES =
      new DevDungeonMonster[] {DevDungeonMonster.ORC_WARRIOR, DevDungeonMonster.ORC_SHAMAN};
  private static final DevDungeonMonster BOSS_TYPE = DevDungeonMonster.CHORT;

  // Spawn Points / Locations
  private final Coordinate bridgeMobSpawn;
  private final Tile[] secretWay;
  private final Coordinate[] mobSpawns;
  private final Coordinate levelBossSpawn;

  private final DamagedBridgeRiddleHandler riddleHandler;

  /**
   * Constructs the Damaged Bridge Riddle Level.
   *
   * @param layout The layout of the level.
   * @param designLabel The design label of the level.
   * @param customPoints The custom points of the level.
   */
  public DamagedBridgeRiddleLevel(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(
        layout,
        designLabel,
        customPoints,
        "The Damaged Bridge",
        "I heard that a powerful artifact is hidden nearby. Rumor says it's just beyond an old bridge. Let's see if we can find it.");
    this.riddleHandler = new DamagedBridgeRiddleHandler(customPoints, this);
    this.bridgeMobSpawn = customPoints.get(8);

    this.secretWay =
        Arrays.stream(getCoordinates(11, 17))
            .map(this::tileAt) // returns Optional<Tile>
            .flatMap(Optional::stream) // only keep present values
            .toArray(Tile[]::new);

    this.mobSpawns = getCoordinates(18, customPoints().size() - 2);
    this.levelBossSpawn = customPoints().getLast();
  }

  @Override
  protected void onFirstTick() {
    prepareBridge();

    // Prepare the secret way
    for (int i = 0; i < secretWay.length - 1; i++) {
      PitTile pitTile = (PitTile) secretWay[i];
      pitTile.timeToOpen(15 * 1000);
    }

    // Spawn all entities and it's content
    spawnChestsAndCauldrons();

    EntityUtils.spawnMobs(MOB_COUNT, MONSTER_TYPES, mobSpawns);
    EntityUtils.spawnBoss(BOSS_TYPE, levelBossSpawn);
    riddleHandler.onFirstTick();
  }

  @Override
  public void onTick() {
    riddleHandler.onTick();
  }

  private void prepareBridge() {
    DevDungeonMonster.BRIDGE_MOB.builder().addToGame().build(bridgeMobSpawn);
    List<PitTile> bridge =
        pitTiles().stream().filter(pit -> pit.coordinate().y() == bridgeMobSpawn.y()).toList();
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
    chest = MiscFactory.newChest(MiscFactory.FILL_CHEST.EMPTY);
    PositionComponent pc =
        chest
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(chest, PositionComponent.class));
    pc.position(secretWay[secretWay.length - 1].coordinate().toPoint());
    InventoryComponent ic =
        chest
            .fetch(InventoryComponent.class)
            .orElseThrow(() -> MissingComponentException.build(chest, InventoryComponent.class));
    ic.add(new ItemPotionHealth(HealthPotionType.GREATER));

    Game.add(chest);
  }
}
