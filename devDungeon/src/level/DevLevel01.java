package level;

import components.TorchComponent;
import contrib.components.InteractionComponent;
import contrib.entities.MiscFactory;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.elements.tile.DoorTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import entities.EntityUtils;
import entities.MonsterType;
import java.util.List;
import level.utils.ITickable;

public class DevLevel01 extends DevDungeonLevel implements ITickable {

  private final Coordinate[] torchPositions;
  private final Coordinate[] riddleRoomTorches;
  private final Coordinate[] riddleRoomBounds;
  private Coordinate[] riddleRoomContent;
  // Entity spawn points
  private Coordinate[] mobSpawns;
  private Coordinate[] doorPositions;
  private Coordinate lastHeroCoords = new Coordinate(0, 0);

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
    // this.riddleRoomContent = customPoints.subList(15, 17).toArray(new Coordinate[0]);
    // Last entries are mob spawns
    // this.mobSpawns = customPoints.subList(17, customPoints.size()).toArray(new Coordinate[0]);
  }

  @Override
  public void onTick(boolean isFirstTick) {
    if (isFirstTick) {
      this.handleFirstTick();
      this.doorTiles().forEach(DoorTile::close);
    }
    if (lastHeroCoords != null && !lastHeroCoords.equals(EntityUtils.getHeroCoords())) {
      // Only handle on hero move
    }

    this.handleDoors();
    this.lastHeroCoords = EntityUtils.getHeroCoords();
  }

  private void handleDoors() {}

  private void handleFirstTick() {
    this.spawnTorches();
    // this.spawnMobs();
    // this.spawnChestsAndCauldrons();
    // this.hideRiddleRoom();
  }

  private void spawnTorches() {
    for (int i = 0; i < torchPositions.length; i++) {
      Point torchPos = new Point(torchPositions[i].x + 0.5f, torchPositions[i].y + 0.25f);
      Point riddleTorchPos =
          new Point(riddleRoomTorches[i].x + 0.5f, riddleRoomTorches[i].y + 0.25f);
      Entity riddleTorch = EntityUtils.spawnTorch(riddleTorchPos, false, false);
      EntityUtils.spawnTorch(
          torchPos,
          false,
          true,
          (entity, who) -> {
            riddleTorch.fetch(TorchComponent.class).ifPresent(TorchComponent::toggle);
            riddleTorch
                .fetch(DrawComponent.class)
                .ifPresent(
                    drawComponent -> {
                      if (entity.fetch(TorchComponent.class).get().lit()) {
                        drawComponent.currentAnimation("on");
                      } else {
                        drawComponent.currentAnimation("off");
                      }
                    });
          });
    }
  }

  private void spawnMobs() {
    for (Coordinate mobPos : mobSpawns) {
      EntityUtils.spawnMonster(MonsterType.TUTORIAL, mobPos);
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

    pc.position(this.riddleRoomContent[0].toPoint());

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
    pc.position(this.riddleRoomContent[1].toPoint());
    Game.add(cauldron);

    Game.add(chest);
  }

  private void hideRiddleRoom() {
    for (int x = riddleRoomBounds[0].x; x <= riddleRoomBounds[1].x; x++) {
      for (int y = riddleRoomBounds[1].y; y <= riddleRoomBounds[0].y; y++) {
        tileAt(new Coordinate(x, y)).visible(false);
      }
    }
  }
}
