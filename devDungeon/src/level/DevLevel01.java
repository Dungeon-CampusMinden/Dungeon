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
  private final Coordinate[] doorPositions;
  private final int MOB_COUNT = 5;

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
    this.mobSpawns = customPoints.subList(17, customPoints.size()).toArray(new Coordinate[0]);
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
    DoorTile door = (DoorTile) tileAt(doorPositions[0]);

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
    this.spawnMobs(MOB_COUNT);
    this.spawnChestsAndCauldrons();
  }

  private void spawnTorches() {
    for (int i = 0; i < torchPositions.length; i++) {
      Point torchPos = new Point(torchPositions[i].x + 0.5f, torchPositions[i].y + 0.25f);
      Point riddleTorchPos =
          new Point(riddleRoomTorches[i].x + 0.5f, riddleRoomTorches[i].y + 0.25f);
      EntityUtils.spawnTorch(riddleTorchPos, true, false);
      EntityUtils.spawnTorch(torchPos, false, true);
    }
  }

  /**
   * Spawns mobs in the game level. Selects mobCount - 1 random spawn points from mobSpawns array to
   * spawn ORC_WARRIOR monsters. Spawns a CHORT monster at the last position in the mobSpawns array.
   * If the CHORT monster dies, it opens the exit tile and makes it visible.
   *
   * @param mobCount the number of mobs to spawn
   */
  private void spawnMobs(int mobCount) {
    if (mobCount > mobSpawns.length) {
      throw new IllegalArgumentException("mobCount cannot be greater than mobSpawns.length");
    }

    List<Coordinate> randomSpawns = ArrayUtils.getRandomElements(mobSpawns, mobCount - 1);

    for (Coordinate mobPos : randomSpawns) {
      try {
        EntityUtils.spawnMonster(MonsterType.ORC_WARRIOR, mobPos);
      } catch (RuntimeException e) {
        throw new RuntimeException("Failed to spawn monster: " + e.getMessage());
      }
    }

    // Last Mob is stronger
    try {
      Entity chort = EntityUtils.spawnMonster(MonsterType.CHORT, mobSpawns[mobSpawns.length - 1]);
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
    for (int x = riddleRoomBounds[0].x; x <= riddleRoomBounds[1].x; x++) {
      for (int y = riddleRoomBounds[1].y; y <= riddleRoomBounds[0].y; y++) {
        tileAt(new Coordinate(x, y)).visible(visible);
      }
    }
  }
}
