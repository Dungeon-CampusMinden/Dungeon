package level.level2;

import contrib.components.InventoryComponent;
import contrib.entities.MiscFactory;
import contrib.item.HealthPotionType;
import contrib.item.concreteItem.ItemPotionHealth;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.TileLevel;
import core.level.elements.tile.DoorTile;
import core.level.elements.tile.PitTile;
import core.level.utils.Coordinate;
import core.utils.components.MissingComponentException;
import entities.EntityUtils;
import entities.MonsterType;
import item.concreteItem.ItemPotionRegenerationPotion;
import item.concreteItem.ItemPotionSpeedPotion;
import java.util.List;
import level.utils.LevelUtils;

/** The Second Level (Damaged Bridge Riddle) */
public class DevLevel02Riddle {

  private final TileLevel level;
  private final Coordinate[] riddleRoomBounds;
  private final DoorTile riddleEntrance;
  private final Coordinate riddleEntranceSign;
  private final Coordinate[] riddlePitBounds;
  private final Coordinate riddleChestSpawn;
  private final DoorTile riddleExit;
  private final Coordinate bridgeMobSpawn;
  private final Coordinate speedPotionChest;
  private final Tile[] secretWay;

  public DevLevel02Riddle(List<Coordinate> customPoints, TileLevel level) {
    this.riddleRoomBounds = new Coordinate[] {customPoints.get(0), customPoints.get(1)};
    this.riddleEntrance = (DoorTile) level.tileAt(customPoints.get(2));
    this.riddleEntranceSign = customPoints.get(3);
    this.riddlePitBounds = new Coordinate[] {customPoints.get(4), customPoints.get(5)};
    this.riddleChestSpawn = customPoints.get(6);
    this.riddleExit = (DoorTile) level.tileAt(customPoints.get(7));
    this.bridgeMobSpawn = customPoints.get(8);
    this.speedPotionChest = customPoints.get(9);
    this.secretWay =
        new Tile[] {
          level.tileAt(customPoints.get(10)),
          level.tileAt(customPoints.get(11)),
          level.tileAt(customPoints.get(12)),
          level.tileAt(customPoints.get(13)),
          level.tileAt(customPoints.get(14)),
          level.tileAt(customPoints.get(15)),
          level.tileAt(customPoints.get(16)),
        };

    this.level = level;
  }

  public void onTick(boolean isFirstTick) {
    if (isFirstTick) {
      this.handleFirstTick();
    }

    if (this.isHeroInRiddleRoom()) {
      LevelUtils.changeVisibilityForArea(this.riddleRoomBounds[0], this.riddleRoomBounds[1], true);
      this.riddleExit.open();
    } else {
      LevelUtils.changeVisibilityForArea(this.riddleRoomBounds[0], this.riddleRoomBounds[1], false);
      this.riddleExit.close();
    }
  }

  private boolean isHeroInRiddleRoom() {
    Entity hero = Game.hero().orElse(null);
    if (hero == null) {
      return false;
    }
    PositionComponent pc =
        hero.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, PositionComponent.class));
    Tile heroTile = this.level.tileAt(pc.position().toCoordinate());
    if (heroTile == null) {
      return false;
    }

    if (this.riddleEntrance.equals(heroTile) || this.riddleExit.equals(heroTile)) {
      return true;
    }

    return LevelUtils.isTileWithinArea(
        heroTile, this.riddleRoomBounds[0], this.riddleRoomBounds[1]);
  }

  private void handleFirstTick() {
    this.riddleEntrance.open();
    this.spawnSigns();
    this.spawnChest();
    this.preparePits();
    this.prepareBridge();
  }

  private void prepareBridge() {
    EntityUtils.spawnMonster(MonsterType.ORC_WARRIOR, this.bridgeMobSpawn);
    List<PitTile> bridge =
        this.level.pitTiles().stream()
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

  private void preparePits() {
    int timeToOpen = 500;
    for (int x = this.riddlePitBounds[1].x; x >= this.riddlePitBounds[0].x; x--) {
      for (int y = this.riddlePitBounds[0].y; y <= this.riddlePitBounds[1].y; y++) {
        Tile pitTile = this.level.tileAt(new Coordinate(x, y));
        if (!(pitTile instanceof PitTile)) {
          throw new RuntimeException("Tile at " + x + ", " + y + " is not a pit tile");
        }
        ((PitTile) pitTile).timeToOpen(timeToOpen);
      }
      if (timeToOpen > 50) {
        timeToOpen -= 100;
      } else {
        timeToOpen = 50;
      }
    }

    for (int i = 0; i < this.secretWay.length - 1; i++) {
      PitTile pitTile = (PitTile) this.secretWay[i];
      pitTile.timeToOpen(15 * 1000);
    }
  }

  private void spawnSigns() {
    EntityUtils.spawnSign(
        """
                    The bridge looks damaged.
                    You could try to run across it, but it looks too dangerous.
                    You need to get faster to safely cross it.
                    Maybe theres a Speed Potion somewhere?""",
        "Riddle: The damaged Bridge",
        this.riddleEntranceSign.toCenteredPoint());
  }

  private void spawnChest() {
    Entity speedPotionChest;
    try {
      speedPotionChest = MiscFactory.newChest(MiscFactory.FILL_CHEST.EMPTY);
    } catch (Exception e) {
      throw new RuntimeException("Failed to create speed potion chest");
    }
    PositionComponent pc =
        speedPotionChest
            .fetch(PositionComponent.class)
            .orElseThrow(
                () -> MissingComponentException.build(speedPotionChest, PositionComponent.class));

    pc.position(this.speedPotionChest.toCenteredPoint());

    InventoryComponent ic =
        speedPotionChest
            .fetch(InventoryComponent.class)
            .orElseThrow(
                () -> MissingComponentException.build(speedPotionChest, InventoryComponent.class));
    ic.add(new ItemPotionSpeedPotion());
    Game.add(speedPotionChest);

    Entity riddleChest;
    try {
      riddleChest = MiscFactory.newChest(MiscFactory.FILL_CHEST.EMPTY);
    } catch (Exception e) {
      throw new RuntimeException("Failed to create riddle chest");
    }
    pc =
        riddleChest
            .fetch(PositionComponent.class)
            .orElseThrow(
                () -> MissingComponentException.build(riddleChest, PositionComponent.class));

    pc.position(this.riddleChestSpawn.toCenteredPoint());

    ic =
        riddleChest
            .fetch(InventoryComponent.class)
            .orElseThrow(
                () -> MissingComponentException.build(riddleChest, InventoryComponent.class));
    ic.add(new ItemPotionSpeedPotion());
    ic.add(new ItemPotionRegenerationPotion());
    Game.add(riddleChest);

    Entity chest;
    try {
      chest = MiscFactory.newChest(MiscFactory.FILL_CHEST.EMPTY);
    } catch (Exception e) {
      throw new RuntimeException("Failed to create speed potion chest");
    }
    pc =
        chest
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(chest, PositionComponent.class));
    pc.position(this.secretWay[this.secretWay.length - 1].coordinate().toCenteredPoint());
    ic =
        chest
            .fetch(InventoryComponent.class)
            .orElseThrow(() -> MissingComponentException.build(chest, InventoryComponent.class));
    ic.add(new ItemPotionHealth(HealthPotionType.GREATER));

    Game.add(chest);
  }
}
