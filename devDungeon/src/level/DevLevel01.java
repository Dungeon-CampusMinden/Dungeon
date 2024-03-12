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
import java.util.List;
import level.utils.ITickable;
import utils.ArrayUtils;

public class DevLevel01 extends DevDungeonLevel implements ITickable {

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

    this.handleDoors();
  }

  private void handleDoors() {
    DoorTile door = (DoorTile) tileAt(this.doorPositions[0]);

    if (Game.entityStream()
            .filter(
                entity ->
                    entity.isPresent(TorchComponent.class)
                        && entity.fetch(TorchComponent.class).get().lit())
            .count()
        == 12) {
      revealRiddleRoom();
      door.open();
    } else {
      hideRiddleRoom();
      door.close();
    }
  }

  private void handleFirstTick() {
    ((ExitTile) endTile()).close();
    endTile().visible(false);
    this.spawnTorches();
    this.spawnMobs(this.mobCount, this.mobTypes);
    this.spawnChestsAndCauldrons();
  }

  private void spawnTorches() {
    for (int i = 0; i < this.torchPositions.length; i++) {
      Point torchPos = new Point(this.torchPositions[i].x + 0.5f, this.torchPositions[i].y + 0.25f);
      Point riddleTorchPos =
          new Point(this.riddleRoomTorches[i].x + 0.5f, this.riddleRoomTorches[i].y + 0.25f);
      EntityUtils.spawnTorch(riddleTorchPos, true, false);
      EntityUtils.spawnTorch(torchPos, false, true);
    }
  }

  /**
   * Spawns mobs in the game level. Selects mobCount - 1 random spawn points from mobSpawns array to
   * spawn a RANDOM monsters. The last mob is always a CHORT. If the CHORT dies, it opens the exit
   *
   * @param mobCount the number of mobs to spawn
   * @param monsterTypes all allowed monster types for this level
   * @throws IllegalArgumentException if mobCount is greater than mobSpawns.length
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
      System.out.println(this.mobSpawns.length);
      Entity chort = EntityUtils.spawnMonster(MonsterType.CHORT, this.levelBossSpawn);
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

    Game.add(chest);
  }

  private void revealRiddleRoom() {
    changeVisForRiddle(true);
  }

  private void hideRiddleRoom() {
    changeVisForRiddle(false);
  }

  private void changeVisForRiddle(boolean visible) {
    for (int x = this.riddleRoomBounds[0].x; x <= this.riddleRoomBounds[1].x; x++) {
      for (int y = this.riddleRoomBounds[1].y; y <= this.riddleRoomBounds[0].y; y++) {
        tileAt(new Coordinate(x, y)).visible(visible);
      }
    }
  }
}
