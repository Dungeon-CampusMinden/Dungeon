package level.devlevel;

import contrib.components.InventoryComponent;
import contrib.entities.MiscFactory;
import contrib.item.HealthPotionType;
import contrib.item.concreteItem.ItemPotionHealth;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.elements.tile.DoorTile;
import core.level.elements.tile.ExitTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.components.MissingComponentException;
import entities.MonsterType;
import java.util.*;
import level.DevDungeonLevel;
import level.devlevel.riddleHandler.IllusionRiddleHandler;
import level.utils.ITickable;
import systems.EffectScheduler;
import systems.FogOfWarSystem;
import utils.EntityUtils;

/** The Damaged Bridge Riddle Level */
public class IllusionRiddleLevel extends DevDungeonLevel implements ITickable {

  // Difficulty (Mob Count, Mob Types)
  private static final int MOB_COUNT = 7;
  private static final MonsterType[] MONSTER_TYPES =
      new MonsterType[] {MonsterType.ORC_WARRIOR, MonsterType.ORC_SHAMAN};
  private static final MonsterType BOSS_TYPE = MonsterType.CHORT;

  // Spawn Points / Locations
  private final Coordinate[] mobSpawns;
  private final Coordinate levelBossSpawn;

  private final IllusionRiddleHandler riddleHandler;

  public IllusionRiddleLevel(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints);
    ((FogOfWarSystem) Game.systems().get(FogOfWarSystem.class)).active(true);

    this.riddleHandler = new IllusionRiddleHandler(customPoints, this);
    this.mobSpawns = new Coordinate[0];
    this.levelBossSpawn = new Coordinate(0, 0);
  }

  @Override
  public void onTick(boolean isFirstTick) {
    if (isFirstTick) {
      ((ExitTile) this.endTile()).close(); // close exit at start (to force defeating the boss)
      this.doorTiles().forEach(DoorTile::close);
      this.pitTiles()
          .forEach(
              pit -> {
                pit.timeToOpen(50);
                pit.close();
              });
      this.handleFirstTick();
    }

    this.riddleHandler.onTick(isFirstTick);
  }

  private void handleFirstTick() {

    // Spawn all entities and it's content
    this.spawnChestsAndCauldrons();

    /*EntityUtils.spawnMobs(
    MOB_COUNT,
    MONSTER_TYPES,
    this.mobSpawns,
    BOSS_TYPE,
    this.levelBossSpawn,
    (boss) -> {
      ((FogOfWarSystem) Game.systems().get(FogOfWarSystem.class)).active(false);
    });*/
    EffectScheduler.getInstance()
        .scheduleAction(
            () -> {
              EntityUtils.teleportHeroTo(this.randomTilePoint());
            },
            1000L * 5);
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
    // pc.position();
    InventoryComponent ic =
        chest
            .fetch(InventoryComponent.class)
            .orElseThrow(() -> MissingComponentException.build(chest, InventoryComponent.class));
    ic.add(new ItemPotionHealth(HealthPotionType.GREATER));

    Game.add(chest);
  }
}
