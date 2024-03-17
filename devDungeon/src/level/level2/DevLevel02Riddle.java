package level.level2;

import contrib.components.InventoryComponent;
import contrib.entities.MiscFactory;
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
import entities.SignFactory;
import item.concreteItem.ItemPotionRegenerationPotion;
import item.concreteItem.ItemPotionSpeedPotion;
import java.util.List;
import level.utils.LevelUtils;
import utils.BurningFireballSkill;

/** The Second Level (Damaged Bridge Riddle) */
public class DevLevel02Riddle {

  private final TileLevel level;
  private final Coordinate[] riddleRoomBounds;
  private final DoorTile riddleEntrance;
  private final Coordinate riddleEntranceSign;
  private final Coordinate[] riddlePitBounds;
  private final Coordinate riddleChestSpawn;
  private final Coordinate riddleRewardSpawn;
  private final DoorTile riddleExit;
  private final Coordinate speedPotionChest;
  private final Coordinate speedPotionChestHint;
  private boolean rewardGiven = false;

  public DevLevel02Riddle(List<Coordinate> customPoints, TileLevel level) {
    this.riddleRoomBounds = new Coordinate[] {customPoints.get(0), customPoints.get(1)};
    this.riddleEntrance = (DoorTile) level.tileAt(customPoints.get(2));
    this.riddleEntranceSign = customPoints.get(3);
    this.riddlePitBounds = new Coordinate[] {customPoints.get(4), customPoints.get(5)};
    this.riddleChestSpawn = customPoints.get(6);
    this.riddleRewardSpawn = new Coordinate(customPoints.get(6).x, customPoints.get(6).y - 1);
    this.riddleExit = (DoorTile) level.tileAt(customPoints.get(7));
    this.speedPotionChest = customPoints.get(9);
    this.speedPotionChestHint = customPoints.get(10);

    this.level = level;
  }

  public void onTick(boolean isFirstTick) {
    if (isFirstTick) {
      this.handleFirstTick();
    }

    if (this.isHeroInRiddleRoom()) {
      LevelUtils.changeVisibilityForArea(this.riddleRoomBounds[0], this.riddleRoomBounds[1], true);
      this.riddleExit.open();

      Entity hero = Game.hero().orElse(null);
      if (hero == null) return;
      PositionComponent pc =
          hero.fetch(PositionComponent.class)
              .orElseThrow(() -> MissingComponentException.build(hero, PositionComponent.class));

      if (!this.rewardGiven && this.riddleRewardSpawn.equals(pc.position().toCoordinate())) {
        this.giveReward();
      }
    } else {
      LevelUtils.changeVisibilityForArea(this.riddleRoomBounds[0], this.riddleRoomBounds[1], false);
      this.riddleExit.close();
    }
  }

  private void giveReward() {
    SignFactory.showTextPopup(
        "You will receive the new burning fireball skill\nas a reward for solving this puzzle!",
        "Riddle solved");
    BurningFireballSkill.UNLOCKED = true;
    this.rewardGiven = true;
  }

  /**
   * Checks if the hero is in the riddle room.
   *
   * <p>This method is used to determine if the hero is currently located in the riddle room. The
   * method checks if the hero is on the entrance or exit tile of the riddle room or if the hero's
   * tile is within the bounds of the riddle room. If the hero is null (which can happen if the hero
   * died in a pit), the method returns true so the hero can still see the riddle room.
   *
   * @return true if the hero is in the riddle room, false otherwise.
   */
  private boolean isHeroInRiddleRoom() {
    Entity hero = Game.hero().orElse(null);
    if (hero == null) {
      return true; // Only if hero died in a pit, he still should be able to see the riddle room
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
    this.level.tileAt(this.riddleRewardSpawn).tintColor(0x22FF22FF);
  }

  /**
   * To make pits more obvious, the pits are prepared in a way that the time to open is decreasing
   * from the bottom right to the top left. This way the player can see the pits opening in a
   * sequence.
   *
   * <p>Also, the time to open is decreasing from 500 to 50. After 5 pits, the time to open is
   * forced to be 50. (After testing a hero can still cross at about (65ms))
   */
  private void preparePits() {
    Coordinate topLeft = this.riddlePitBounds[0];
    Coordinate bottomRight = this.riddlePitBounds[1];
    int timeToOpen = 500;

    for (int x = bottomRight.x; x >= topLeft.x; x--) {
      for (int y = bottomRight.y + 1; y <= topLeft.y - 1; y++) {
        PitTile pitTile = (PitTile) this.level.tileAt(new Coordinate(x, y));
        pitTile.timeToOpen(timeToOpen);
      }
      timeToOpen = Math.max(50, timeToOpen - 100); // force after 5 pits to be 50
    }

    int[] bordersYs = new int[] {topLeft.y, bottomRight.y};
    for (int y : bordersYs) {
      for (int x = topLeft.x; x <= bottomRight.x; x++) {
        PitTile pitTile = (PitTile) this.level.tileAt(new Coordinate(x, y));
        pitTile.open();
      }
    }
  }

  private void spawnSigns() {
    EntityUtils.spawnSign(
        """
                    The bridge seems to be damaged.
                    You could try to run across it, but it looks too dangerous.
                    If you were faster, you could cross it.
                    Maybe theres a Speed Potion somewhere nearby?""",
        "Riddle: The damaged Bridge",
        this.riddleEntranceSign.toCenteredPoint());
    EntityUtils.spawnSign(
        """
                    This looks interesting. Maybe there is
                    something hidden behind those sculptures?""",
        "Riddle: The damaged Bridge",
        this.speedPotionChestHint.toCenteredPoint());
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
    ic.add(new ItemPotionRegenerationPotion());
    Game.add(riddleChest);
  }
}
