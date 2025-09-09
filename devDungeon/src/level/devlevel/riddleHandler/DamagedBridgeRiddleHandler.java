package level.devlevel.riddleHandler;

import contrib.components.HealthComponent;
import contrib.components.InventoryComponent;
import contrib.entities.MiscFactory;
import contrib.hud.DialogUtils;
import contrib.utils.EntityUtils;
import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.DungeonLevel;
import core.level.elements.tile.DoorTile;
import core.level.elements.tile.PitTile;
import core.level.utils.Coordinate;
import core.level.utils.LevelUtils;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import item.concreteItem.ItemPotionRegeneration;
import item.concreteItem.ItemPotionSpeed;
import java.util.List;

/**
 * The DamagedBridgeRiddleHandler class is used to handle the riddle of the damaged bridge. The
 * riddle consists of a damaged bridge that the hero has to cross. The hero can only cross the
 * bridge if they are fast enough. The riddle room contains a chest with a speed potion that the
 * hero can use to cross the bridge. If the hero crosses the bridge, they will receive a reward.
 */
public class DamagedBridgeRiddleHandler {

  // The reward for solving the riddle (max health points)
  private static final int RIDDLE_REWARD = 5;
  private final DungeonLevel level;
  private final Coordinate[] riddleRoomBounds; // TopLeft, BottomRight
  private final DoorTile riddleEntrance; // The entrance to the riddle room
  private final Coordinate riddleEntranceSign; // The sign next to the riddle entrance
  private final Coordinate[] riddlePitBounds; // TopLeft, BottomRight
  private final Coordinate
      riddleChestSpawn; // The spawn point of the reward chest for solving the riddle
  private final Coordinate
      riddleRewardSpawn; // The spawn point of the reward for solving the riddle
  private final DoorTile riddleExit; // The exit of the riddle room
  private final Coordinate speedPotionChest; // The spawn point of the speed potion chest
  private final Coordinate
      speedPotionChestHint; // The sign that hints towards the speed potion chest
  private boolean rewardGiven = false;

  /**
   * Constructs a new DamagedBridgeRiddleHandler with the given custom points and level.
   *
   * @param customPoints The custom points of the riddle room.
   * @param level The level of the riddle room.
   */
  public DamagedBridgeRiddleHandler(List<Coordinate> customPoints, DungeonLevel level) {
    this.riddleRoomBounds = new Coordinate[] {customPoints.get(0), customPoints.get(1)};
    this.riddleEntrance =
        level
            .tileAt(customPoints.get(2))
            .filter(DoorTile.class::isInstance)
            .map(DoorTile.class::cast)
            .orElse(null);
    this.riddleEntranceSign = customPoints.get(3);
    this.riddlePitBounds = new Coordinate[] {customPoints.get(4), customPoints.get(5)};
    this.riddleChestSpawn = customPoints.get(6);
    this.riddleRewardSpawn = new Coordinate(customPoints.get(6).x(), customPoints.get(6).y() - 1);
    this.riddleExit =
        level
            .tileAt(customPoints.get(7))
            .filter(DoorTile.class::isInstance)
            .map(DoorTile.class::cast)
            .orElse(null);
    this.speedPotionChest = customPoints.get(9);
    this.speedPotionChestHint = customPoints.get(10);

    this.level = level;
  }

  /** Handles the first tick of the riddle room. */
  public void onFirstTick() {
    riddleEntrance.open();
    spawnSigns();
    spawnChest();
    preparePits();
    level.tileAt(riddleRewardSpawn).ifPresent(t -> t.tintColor(0x22FF22FF));
  }

  /** Handles the tick of the riddle room. */
  public void onTick() {
    if (isHeroInRiddleRoom()) {
      LevelUtils.changeVisibilityForArea(riddleRoomBounds[0], riddleRoomBounds[1], true);
      riddleExit.open();

      Entity hero = Game.hero().orElse(null);
      if (hero == null) return;
      PositionComponent pc =
          hero.fetch(PositionComponent.class)
              .orElseThrow(() -> MissingComponentException.build(hero, PositionComponent.class));

      if (!rewardGiven && riddleRewardSpawn.equals(pc.coordinate())) {
        giveReward();
      }
    } else {
      LevelUtils.changeVisibilityForArea(riddleRoomBounds[0], riddleRoomBounds[1], false);
      riddleExit.close();
    }
  }

  private void giveReward() {
    DialogUtils.showTextPopup(
        "You will receive "
            + RIDDLE_REWARD
            + " additional maximum health points \nas a reward for solving this puzzle!",
        "Riddle solved");
    Game.hero()
        .flatMap(hero -> hero.fetch(HealthComponent.class))
        .ifPresent(
            hc -> {
              hc.maximalHealthpoints(hc.maximalHealthpoints() + RIDDLE_REWARD);
              hc.receiveHit(new Damage(-RIDDLE_REWARD, DamageType.HEAL, null));
              this.rewardGiven = true;
              level.tileAt(riddleRewardSpawn).ifPresent(t -> t.tintColor(-1));
            });
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
    Point heroPos = EntityUtils.getHeroPosition();
    if (heroPos == null) {
      return true; // if hero dies due to pit, still show riddle room
    }
    return LevelUtils.isHeroInArea(riddleRoomBounds[0], riddleRoomBounds[1])
        || level.tileAt(heroPos).map(t -> t == riddleEntrance).orElse(false)
        || level.tileAt(heroPos).map(t -> t == riddleExit).orElse(false);
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
    Coordinate topLeft = riddlePitBounds[0];
    Coordinate bottomRight = riddlePitBounds[1];
    int timeToOpen = 500;

    for (int x = bottomRight.x(); x >= topLeft.x(); x--) {
      final int currentTimeToOpen = timeToOpen; // final variable for lambda
      for (int y = bottomRight.y() + 1; y <= topLeft.y() - 1; y++) {
        level
            .tileAt(new Coordinate(x, y))
            .filter(PitTile.class::isInstance)
            .map(PitTile.class::cast)
            .ifPresent(p -> p.timeToOpen(currentTimeToOpen));
      }
      timeToOpen = Math.max(50, timeToOpen - 100);
    }

    int[] bordersYs = new int[] {topLeft.y(), bottomRight.y()};
    for (int y : bordersYs) {
      for (int x = topLeft.x(); x <= bottomRight.x(); x++) {
        level
            .tileAt(new Coordinate(x, y))
            .filter(PitTile.class::isInstance)
            .map(PitTile.class::cast)
            .ifPresent(PitTile::open);
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
        riddleEntranceSign.toPoint());
    EntityUtils.spawnSign(
        """
                    This looks interesting. Maybe there is
                    something hidden behind those sculptures?""",
        "Riddle: The damaged Bridge",
        speedPotionChestHint.toPoint());
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

    pc.position(this.speedPotionChest.toPoint());

    InventoryComponent ic =
        speedPotionChest
            .fetch(InventoryComponent.class)
            .orElseThrow(
                () -> MissingComponentException.build(speedPotionChest, InventoryComponent.class));
    ic.add(new ItemPotionSpeed());
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

    pc.position(riddleChestSpawn.toPoint());

    ic =
        riddleChest
            .fetch(InventoryComponent.class)
            .orElseThrow(
                () -> MissingComponentException.build(riddleChest, InventoryComponent.class));
    ic.add(new ItemPotionRegeneration());
    Game.add(riddleChest);
  }
}
