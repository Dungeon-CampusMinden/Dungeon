package core.level;

import contrib.entities.*;
import contrib.item.Item;
import contrib.item.concreteItem.*;
import core.Game;
import core.level.elements.tile.DoorTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Direction;
import core.utils.Point;
import java.util.*;

/** This level can be used for testing new implementations of items, skills, monsters and more. */
public class HudLevel extends DungeonLevel {
  private DoorTile door1;
  private final DesignLabel designLabel;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param namedPoints The custom points of the level.
   */
  public HudLevel(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "HUD-Level");
    this.designLabel = designLabel;
  }

  @Override
  protected void onFirstTick() {
    door1 = (DoorTile) Game.tileAt(new Coordinate(13, 8)).orElse(null);
    // door1.close();
    Item bow = new ItemWoodenBow();
    Item arrow1 = new ItemWoodenArrow(10);
    Item arrow2 = new ItemWoodenArrow(10);
    Item hammer = new ItemHammer();
    Point bowSpawnPoint = new Point(5, 5);
    Point arrowSpawnPoint1 = new Point(7, 5);
    Point arrowSpawnPoint2 = new Point(9, 5);
    Point hammerSpawnPoint = new Point(5, 7);
    bow.drop(bowSpawnPoint);
    arrow1.drop(arrowSpawnPoint1);
    arrow2.drop(arrowSpawnPoint2);
    hammer.drop(hammerSpawnPoint);

    // Game.add(MiscFactory.newHeartPickup(new Point(11, 5), 5));
    // Game.add(MiscFactory.newFairyPickup(new Point(13, 5)));
    Item fairyOnTheGround = new ItemFairy();
    Item heartOnTheGround = new ItemHeart(5);
    fairyOnTheGround.drop(new Point(13, 5));
    heartOnTheGround.drop(new Point(11, 5));

    Point keySpawnPoint = new Point(5, 3);
    Point bigKeySpawnPoint = new Point(7, 3);
    Item smallKey = new ItemKey();
    Item bigKey = new ItemBigKey();
    smallKey.drop(keySpawnPoint);
    bigKey.drop(bigKeySpawnPoint);

    Set<Item> items = new HashSet<>();
    Item stuff = new ItemPotionHealth();
    Item stuff2 = new ItemResourceEgg();
    items.add(stuff);
    items.add(stuff2);

    Set<Item> stoneItems = new HashSet<>();
    Item keyInStone = new ItemKey();
    Item fairy = new ItemFairy();
    Item heartInStone = new ItemHeart(5);
    Item bigKeyInStone = new ItemBigKey();
    Item potionInStone = new ItemPotionHealth();
    Item eggInStone = new ItemResourceEgg();
    // stoneItems.add(fairy);
    stoneItems.add(eggInStone);
    stoneItems.add(keyInStone);
    // stoneItems.add(heartInStone);
    stoneItems.add(bigKeyInStone);
    stoneItems.add(potionInStone);

    Set<Item> vaseItems = new HashSet<>();
    Item heart = new ItemHeart(5);
    vaseItems.add(heart);

    try {
      // Entity monster = DungeonMonster.GOBLIN.builder().build(new Point(1, 1));
      // monster.add(new SpikyComponent(10, DamageType.PHYSICAL, 10));
      // monster.add(new PositionComponent());
      // Game.add(monster);
      // Game.add(MonsterFactory.randomMonster());
      Game.add(MiscFactory.newLockedChest(items, new Point(9, 3), ItemKey.class));
      Game.add(MiscFactory.createDoorBlocker(door1, ItemBigKey.class));
      Game.add(EntityFactory.newStone(new Point(7, 7), stoneItems));
      Game.add(EntityFactory.newVase(new Point(9, 7), vaseItems));
      Game.add(EntityFactory.newStone(new Point(7, 9)));
      Game.add(EntityFactory.newVase(new Point(9, 9)));
      Game.add(EntityFactory.newStone(new Point(11, 9), 0.5f));
      Game.add(EntityFactory.newVase(new Point(11, 7), 0.5f));
      /*
      Game.add(
          EntityFactory.newMovingArrowSentry(
              new Point(16.0f, 8.0f), new Point(16.0f, 2.0f), Direction.RIGHT, 500, 5));

      Game.add(
          EntityFactory.newMovingArrowWallSentry(
              new Point(17.0f, 0.0f), new Point(22.0f, 0.0f), Direction.UP, 500, 5));

      Game.add(
          EntityFactory.newStationaryArrowWallSentry(
              new Point(22.0f, 11.0f), Direction.DOWN, 500, 5));

      Game.add(
          EntityFactory.newStationaryArrowSentry(new Point(25.0f, 10.0f), Direction.DOWN, 500, 5));


       */
      // ---------------------------------------------------------------------------------------

      Game.add(
          EntityFactory.newMovingFireballSentry(
              new Point(16.0f, 8.0f), new Point(16.0f, 2.0f), Direction.RIGHT, 500, 5));

      Game.add(
          EntityFactory.newMovingFireballWallSentry(
              new Point(17.0f, 0.0f), new Point(22.0f, 0.0f), Direction.UP, 500, 5));

      Game.add(
          EntityFactory.newStationaryFireballWallSentry(
              new Point(22.0f, 11.0f), Direction.DOWN, 500, 5));

      Game.add(
          EntityFactory.newStationaryFireballSentry(
              new Point(25.0f, 10.0f), Direction.DOWN, 500, 5));

    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }

  @Override
  protected void onTick() {}

  public Optional<DesignLabel> designLabel() {
    return Optional.ofNullable(designLabel);
  }
}
